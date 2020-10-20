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

  public void processPlaneSwitchMessage(PlaneSwitchWrapper mw) {
    SwitchInfo outgoingSwitchInfo = this.outgoingPlanes.tryGetFirst(q ->
            q.getSqwk().equals(mw.getSquawk()) && q.getAtcId().equals(mw.getSource()));
    if (outgoingSwitchInfo != null)
      this.processOutgoingPlaneSwitchMessage(outgoingSwitchInfo, mw);
    else
      this.processIncomingPlaneSwitchMessage(mw);
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
    this.messageSenderConsumer.consume(msg);
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
    PlaneSwitchRequest ps = PlaneSwitchWrapper.tryGetBaseIfBasedOnPlaneSwitch(m);
    if (ps == null) return false;
    else return ps.getSquawk().equals(otherSquawk);
  }

  private void elapRepeatOldSwitchRequests() {
    EDayTimeRun now = Context.getShared().getNow();
    IReadOnlyList<SwitchInfo> awaitings = this.outgoingPlanes.where(
            q -> q.getLastRequest().getValue() + SECONDS_BEFORE_REPEAT_SWITCH_REQUEST < now.getValue());

    for (SwitchInfo si : awaitings) {
      if (this.delayedMessagesProducer.produce().isAny(q -> isPlaneSwitchRelated(q, si.getSqwk())))
        continue; // if message about this plane is delayed and waiting to process
      Message m = new Message(
              Participant.createAtc(parent.getAtcId()),
              Participant.createAtc(si.getAtcId()),
              new PlaneSwitchRequest(si.getSqwk(), true));
      messageSenderConsumer.consume(m);
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
    messageSenderConsumer.consume(msg);
  }

  private void sendConfirmMessage(PlaneSwitchWrapper mw) {
    AtcConfirmation confirmation = new AtcConfirmation(mw.getPlaneSwitch());
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(mw.getSource()),
            confirmation
    );
    messageSenderConsumer.consume(msg);
  }

  private void sendRejectMessage(PlaneSwitchWrapper mw, String reason) {
    AtcRejection confirmation = new AtcRejection(mw.getPlaneSwitch(), reason);
    Message msg = new Message(
            Participant.createAtc(parent.getAtcId()),
            Participant.createAtc(mw.getSource()),
            confirmation
    );
    messageSenderConsumer.consume(msg);
  }

  private void processOutgoingPlaneSwitchMessage(SwitchInfo si, PlaneSwitchWrapper mw) {
    if (mw.getRouting() != null) {
      // the other ATC tries to change plane routing, we can check in and reject it if required
      IAirplane plane = Context.Internal.getPlane(si.getSqwk());
      if (!parent.acceptsNewRouting(plane, mw.getRouting())) {
        this.sendRejectMessage(mw, sf("Updated routing %s not accepted.", mw.getRouting().toString()));
      } else {
        parent.processConfirmedOutgoingPlaneSwitch(si.getSqwk());
      }
    } else
      parent.processConfirmedOutgoingPlaneSwitch(si.getSqwk());
    this.outgoingPlanes.remove(si);
  }

  private void processIncomingPlaneSwitchMessage(PlaneSwitchWrapper mw) {
    IAirplane plane = Context.Internal.getPlane(mw.getSquawk());
    RequestResult planeAcceptance = parent.canIAcceptPlaneIncomingFromAnotherAtc(plane);
    if (planeAcceptance.isAccepted) {
      this.incomingPlanes.add(plane.getSqwk());
      sendConfirmMessage(mw);
    } else {
      sendRejectMessage(mw, planeAcceptance.message);
    }
  }

}
