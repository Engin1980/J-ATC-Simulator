package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.atcs.internal.IAtcSwitchManagerInterface;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

class SwitchManager implements IXPersistable {

  private static final int SECONDS_BEFORE_REPEAT_SWITCH_REQUEST = 30;
  @XIgnored private IAtcSwitchManagerInterface parent;
  private final ISet<Squawk> incomingPlanes = new ESet<>();
  private final IMap<Squawk, SwitchInfo> outgoingPlanes = new EMap<>();
  @XIgnored private Producer<IReadOnlyList<Message>> delayedMessagesProducer;

  @XConstructor
  @XmlConstructor
  SwitchManager() {
    PostContracts.register(this, () -> parent != null);
  }

  public void elapseSecond() {
    this.elapCheckAndProcessPlanesReadyToSwitch();
    this.elapRepeatOldSwitchRequests();
  }

  public boolean isResponsibleFor(Callsign callsign) {
    boolean ret = parent.getPlanesUnderControl().isAny(q -> q.getCallsign().equals(callsign));
    return ret;
  }

  public void processGoodDayFromPlane(GoodDayNotification goodDayNotification) {
    if (goodDayNotification.isRepeated()) return;
    SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
    IAirplane plane = Context.Internal.getPlane(goodDayNotification.getCallsign());
    lst.add(new RadarContactConfirmationNotification());
    boolean isAccepted;
    if (this.incomingPlanes.contains(plane.getSqwk())) {
      this.incomingPlanes.remove(plane.getSqwk());
      isAccepted = true;
    } else {
      AtcId atcId = Context.Internal.getAtc(AtcType.app).getAtcId();
      lst.add(new ContactCommand(atcId));
      isAccepted = false;
    }
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAirplane(plane.getCallsign()),
            lst);
    parent.sendMessage(msg);
    if (isAccepted) parent.onAfterIncomingPlaneGoodDayNotificationConfirmed(plane.getSqwk());
  }

  public void processPlaneSwitchMessage(PlaneSwitchRequest planeSwitchRequest, AtcId sender) {
    SwitchInfo si = this.outgoingPlanes.tryGet(planeSwitchRequest.getSquawk());
    if (si == null || si.getAtcId().equals(sender) == false)
      this.processIncomingPlaneSwitchMessage(planeSwitchRequest, sender);
    else
      this.processOutgoingPlaneSwitchMessage(si, planeSwitchRequest, sender);
  }

  void bind(IAtcSwitchManagerInterface parent,
            Producer<IReadOnlyList<Message>> delayedMessagesProducer) {
    EAssert.Argument.isNotNull(parent, "parent");
    this.parent = parent;
    this.delayedMessagesProducer = delayedMessagesProducer;
  }

  private void elapCheckAndProcessPlanesReadyToSwitch() {
    IReadOnlyList<IAirplane> planes = parent.getPlanesUnderControl();
    for (IAirplane airplane : planes) {
      if (this.outgoingPlanes.containsKey(airplane.getSqwk())) continue;

      if (parent.isPlaneReadyToSwitchToAnotherAtc(airplane)) {
        AtcId targetAtcId = parent.getAtcIdWhereIAmSwitchingPlanes();
        this.elapRequestNewSwitch(airplane, targetAtcId);
      }
    }
  }

  private boolean isPlaneSwitchRelated(Message m, Squawk otherSquawk) {
    return m.getContent() instanceof PlaneSwitchRequest &&
            ((PlaneSwitchRequest) m.getContent()).getSquawk().equals(otherSquawk);
  }

  private void elapRepeatOldSwitchRequests() {
    EDayTimeRun now = Context.getShared().getNow();
    IMap<Squawk, SwitchInfo> awaitings = this.outgoingPlanes.where(
            q -> q.getValue().getLastRequest().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

    for (Squawk squawk : awaitings.getKeys()) {
      if (this.delayedMessagesProducer.invoke().isAny(q -> isPlaneSwitchRelated(q, squawk)))
        continue; // if message about this plane is delayed and waiting to process
      SwitchInfo si = awaitings.get(squawk);
      Message msg = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(si.getAtcId()),
              new PlaneSwitchRequest(squawk, true));
      parent.sendMessage(msg);
      si.setLastRequest(now.toStamp());
    }
  }

  private void elapRequestNewSwitch(IAirplane airplane, AtcId targetAtcId) {
    SwitchInfo si = new SwitchInfo(targetAtcId);
    this.outgoingPlanes.set(airplane.getSqwk(), si);
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(targetAtcId),
            new PlaneSwitchRequest(airplane.getSqwk(), false));
    parent.sendMessage(msg);
  }

  private void processOutgoingPlaneSwitchMessage(SwitchInfo si, PlaneSwitchRequest psr, AtcId sender) {
    IAtcSpeech speech;
    IAirplane plane = Context.Internal.tryGetPlane(psr.getSquawk());
    if (plane == null) {
      speech = new AtcRejection(psr, sf("Squawk code '%s' not under my control.", psr.getSquawk().toString()));
    } else {
      if (psr.getRouting() != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (!parent.acceptsNewRouting(plane, psr.getRouting())) {
          speech = new AtcRejection(psr, sf("Updated routing %s not accepted.", psr.getRouting().toString()));
        } else {
          speech = new AtcConfirmation(psr);
        }
      } else
        speech = new AtcConfirmation(psr);
    }

    if (speech instanceof AtcConfirmation) {
      parent.onOutgoingPlaneSwitchCompleted(psr.getSquawk());
      this.outgoingPlanes.remove(psr.getSquawk());
    }

    parent.sendMessage(
            new Message(
                    Participant.createAtc(parent.getAtcId()),
                    Participant.createAtc(sender),
                    speech
            ));
  }

  private void processIncomingPlaneSwitchMessage(PlaneSwitchRequest planeSwitchRequest, AtcId sender) {
    IAtcSpeech msgContent;
    IAirplane plane = Context.Internal.tryGetPlane(planeSwitchRequest.getSquawk());
    if (plane == null) {
      msgContent = new AtcRejection(planeSwitchRequest, sf("Squawk code '%s' not under my control.", planeSwitchRequest.getSquawk().toString()));
    } else {
      RequestResult planeAcceptance = parent.canIAcceptPlaneIncomingFromAnotherAtc(plane);
      if (planeAcceptance.isAccepted) {
        this.incomingPlanes.add(plane.getSqwk());
        msgContent = new AtcConfirmation(planeSwitchRequest);
      } else {
        msgContent = new AtcRejection(planeSwitchRequest, planeAcceptance.message);
      }
    }

    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(sender),
            msgContent
    );
    parent.sendMessage(msg);
  }

}
