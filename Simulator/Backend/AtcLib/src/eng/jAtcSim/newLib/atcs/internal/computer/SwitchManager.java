package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.functionalInterfaces.Consumer;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

class SwitchManager {

  private static final int SECONDS_BEFORE_REPEAT_SWITCH_REQUEST = 30;
  private final ComputerAtc parent;
  private final IList<Squawk> incomingPlanes = new EList<>();
  private final IList<SwitchInfo> outgoingPlanes = new EList<>();
  private final Consumer<Message> messageSenderConsumer;
  private final Producer<IReadOnlyList<Message>> delayedMessagesProducer;

  public SwitchManager(ComputerAtc parent,
                       Consumer<Message> messageSenderConsumer,
                       Producer<IReadOnlyList<Message>> delayedMessagesProducer
  ) {
    this.parent = parent;
    this.messageSenderConsumer = messageSenderConsumer;
    this.delayedMessagesProducer = delayedMessagesProducer;
  }

  public void elapseSecond() {
    this.elapCheckAndProcessPlanesReadyToSwitch();
    this.elapRepeatOldSwitchRequests();
  }

  public void processGoodDayFromPlane(GoodDayNotification goodDayNotification) {
    SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
    IAirplane plane = Context.Internal.getPlane(goodDayNotification.getCallsign());
    lst.add(new RadarContactConfirmationNotification());
    if (this.incomingPlanes.isAny(q -> q.equals(plane.getSqwk()))) {
      this.incomingPlanes.remove(plane.getSqwk());
    } else {
      AtcId atcId = Context.Internal.getAtc(AtcType.app).getAtcId();
      lst.add(new ContactCommand(atcId));
    }
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAirplane(plane.getCallsign()),
            lst);
    this.messageSenderConsumer.invoke(msg);
  }

  public void processPlaneSwitchMessage(PlaneSwitchRequest planeSwitchRequest, AtcId sender) {
    SwitchInfo outgoingSwitchInfo = this.outgoingPlanes.tryGetFirst(q ->
            q.getSqwk().equals(planeSwitchRequest.getSquawk()) && q.getAtcId().equals(sender));
    if (outgoingSwitchInfo != null)
      this.processOutgoingPlaneSwitchMessage(outgoingSwitchInfo, planeSwitchRequest, sender);
    else
      this.processIncomingPlaneSwitchMessage(planeSwitchRequest, sender);
  }

  private void elapCheckAndProcessPlanesReadyToSwitch() {
    IReadOnlyList<IAirplane> planes = parent.getPlanesUnderControl();
    for (IAirplane airplane : planes) {
      if (this.outgoingPlanes.isAny(q -> q.getSqwk().equals(airplane.getSqwk())))
        continue;

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
    IReadOnlyList<SwitchInfo> awaitings = this.outgoingPlanes.where(
            q -> q.getLastRequest().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

    for (SwitchInfo si : awaitings) {
      if (this.delayedMessagesProducer.invoke().isAny(q -> isPlaneSwitchRelated(q, si.getSqwk())))
        continue; // if message about this plane is delayed and waiting to process
      Message m = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(si.getAtcId()),
              new PlaneSwitchRequest(si.getSqwk(), true));
      messageSenderConsumer.invoke(m);
      si.setLastRequest(now.toStamp());
    }
  }

  private void elapRequestNewSwitch(IAirplane airplane, AtcId targetAtcId) {
    SwitchInfo si = new SwitchInfo(airplane.getSqwk(), targetAtcId);
    this.outgoingPlanes.add(si);
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(targetAtcId),
            new PlaneSwitchRequest(airplane.getSqwk(), false));
    messageSenderConsumer.invoke(msg);
  }

  private void sendConfirmMessage(PlaneSwitchRequest planeSwitchRequest, AtcId sender) {
    AtcConfirmation confirmation = new AtcConfirmation(planeSwitchRequest);
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(sender),
            confirmation
    );
    messageSenderConsumer.invoke(msg);
  }

  private void sendRejectMessage(PlaneSwitchRequest psr, String reason, AtcId targetAtcId) {
    AtcRejection confirmation = new AtcRejection(psr, reason);
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(targetAtcId),
            confirmation
    );
    messageSenderConsumer.invoke(msg);
  }

  private void processOutgoingPlaneSwitchMessage(SwitchInfo si, PlaneSwitchRequest psr, AtcId sender) {
    if (psr.getRouting() != null) {
      // the other ATC tries to change plane routing, we can check in and reject it if required
      IAirplane plane = Context.Internal.getPlane(si.getSqwk());
      if (!parent.acceptsNewRouting(plane, psr.getRouting())) {
        this.sendRejectMessage(psr, sf("Updated routing %s not accepted.", psr.getRouting().toString()), sender);
      } else {
        parent.processConfirmedOutgoingPlaneSwitch(si.getSqwk());
      }
    } else
      parent.processConfirmedOutgoingPlaneSwitch(si.getSqwk());
    this.outgoingPlanes.remove(si);
  }

  private void processIncomingPlaneSwitchMessage(PlaneSwitchRequest planeSwitchRequest, AtcId sender) {
    IAirplane plane = Context.Internal.getPlane(planeSwitchRequest.getSquawk());
    RequestResult planeAcceptance = parent.canIAcceptPlaneIncomingFromAnotherAtc(plane);
    if (planeAcceptance.isAccepted) {
      this.incomingPlanes.add(plane.getSqwk());
      sendConfirmMessage(planeSwitchRequest, sender);
    } else {
      sendRejectMessage(planeSwitchRequest, planeAcceptance.message, sender);
    }
  }

}
