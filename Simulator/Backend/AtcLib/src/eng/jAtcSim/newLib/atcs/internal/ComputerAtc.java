package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.user2atc.PlaneSwitchRequest;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  public static class RequestResult {
    public final boolean isAccepted;
    public final String message;

    public RequestResult(boolean isAccepted, String message) {
      this.isAccepted = isAccepted;
      this.message = message;
    }
  }

  private final DelayedList<Message> speechDelayer = new DelayedList<>(
      Global.MINIMUM_ATC_SPEECH_DELAY_SECONDS, Global.MAXIMUM_ATC_SPEECH_DELAY_SECONDS);

  public ComputerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  public void elapseSecond() {

    IList<Message> msgs = MessagingAcc.getMessenger().getMessagesByListener(
        Participant.createAtc(this.getAtcId()), true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    switchConfirmedPlanesIfReady();
    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();

  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }

  private void acceptSwitch(Callsign callsign, AtcId targetAtcId, PlaneSwitchRequest psr) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
    Message nm = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new AtcConfirmation(psr));
    sendMessage(nm);
  }

  protected abstract boolean acceptsNewRouting(Callsign callsign, SwitchRoutingRequest srr);

  private void applySwitchHangOff(Callsign callsign) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().applyConfirmedSwitch(this.getAtcId(), callsign);
    AtcId newTargetAtc = prm.getResponsibleAtc(callsign);
    Message msg = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAirplane(callsign),
        new SpeechList<>(
            new ContactCommand(newTargetAtc)));
    MessagingAcc.getMessenger().send(msg);
  }

  protected abstract RequestResult canIAcceptPlane(Callsign callsign);

  /**
   * Checks for planes ready to switch and switch them.
   */
  private void checkAndProcessPlanesReadyToSwitch() {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    IReadOnlyList<Callsign> myPlanes = prm.forAtc().getPlanes(this.getAtcId());
    for (Callsign myPlane : myPlanes) {
      if (prm.forAtc().isUnderSwitchRequest(myPlane, this.getAtcId(), null))
        continue;

      AtcId targetAtcId = getTargetAtcIfPlaneIsReadyToSwitch(myPlane);
      if (targetAtcId != null) {
        this.requestNewSwitch(myPlane, targetAtcId);
      }
    }
  }

  private void confirmGoodDayNotificationIfRequired(Callsign callsign, SpeechList<IFromPlaneSpeech> spchs) {
    IList<GoodDayNotification> gdns = spchs.whereItemClassIs(GoodDayNotification.class, false);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList<IForPlaneSpeech> lst = new SpeechList<>();
      lst.add(new RadarContactConfirmationNotification());
      if (InternalAcc.getPrm().forAtc().getResponsibleAtc(callsign).equals(this.getAtcId())) {
        AtcId atcId = InternalAcc.getAtc(AtcType.app).getAtcId();
        lst.add(new ContactCommand(atcId));
      }
      Message msg = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAirplane(callsign),
          lst);
      sendMessage(msg);
    }
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    if (m.getContent() instanceof PlaneSwitchRequest) {
      processPlaneSwitchMessage(m);
    } else {
      processNonPlaneSwitchMessageFromAtc(m);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        super.getRecorder().write(m); // incoming item

        if (m.getSource().getType() == Participant.eType.airplane) {
          // messages from planes
          Callsign callsign = new Callsign(m.getSource().getId());
          SpeechList<IFromPlaneSpeech> spchs = m.getContent();

          if (spchs.containsType(GoodDayNotification.class))
            confirmGoodDayNotificationIfRequired(callsign, spchs);
          processMessagesFromPlane(callsign, spchs);
        } else if (m.getSource().getType() == Participant.eType.atc) {
          elapseSecondProcessMessageFromAtc(m);
        }
      } catch (Exception ex) {
        throw new EApplicationException(sf(
            "Failed to process a message for Atc. Atc: %s. Message from %s. Message itself: %s.",
            this.getAtcId().getName(),
            m.getSource().getId(),
            m.toString()), ex);
      }
    }
  }

  /**
   * Returns target atc if plane is ready for switch.
   *
   * @param callsign Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  protected abstract AtcId getTargetAtcIfPlaneIsReadyToSwitch(Callsign callsign);

  protected abstract void processMessagesFromPlane(Callsign callsign, SpeechList<IFromPlaneSpeech> spchs);

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  private void processPlaneSwitchMessage(Message m) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    PlaneSwitchRequest psm = m.getContent();
    Callsign callsign = InternalAcc.getCallsignFromSquawk(psm.getSquawk());
    EAssert.isTrue(m.getSource().getType() == Participant.eType.atc);
    AtcId targetAtcId = InternalAcc.getAtc(m.getSource().getId()).getAtcId();
    if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), targetAtcId)) {
      // other ATC confirms our request, plane is going to hang off
      SwitchRoutingRequest srr = prm.forAtc().getRoutingForSwitchRequest(this.getAtcId(), callsign);
      if (srr != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (acceptsNewRouting(callsign, srr) == false)
          rejectChangedRouting(callsign, targetAtcId, psm);
        else
          prm.forAtc().confirmRerouting(this.getAtcId(), callsign);
      }
    } else if (prm.forAtc().isUnderSwitchRequest(callsign, null, this.getAtcId())) {
      // other ATC offers us a plane
      RequestResult planeAcceptance = canIAcceptPlane(callsign);
      if (planeAcceptance.isAccepted) {
        acceptSwitch(callsign, targetAtcId, psm);
      } else {
        rejectSwitch(callsign, targetAtcId, planeAcceptance, psm);
      }
    }
  }

  private void rejectChangedRouting(Callsign callsign, AtcId targetAtcId, PlaneSwitchRequest psr) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().resetSwitchRequest(this.getAtcId(), callsign);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new AtcRejection(psr, "New routing rejected."));
    sendMessage(m);
  }

  private void rejectSwitch(Callsign callsign, AtcId targetAtcId, RequestResult planeAcceptance, PlaneSwitchRequest psr) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().rejectSwitchRequest(callsign, this.getAtcId());
    Message nm = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new AtcRejection(psr, planeAcceptance.message));
    sendMessage(nm);
  }

  private void repeatOldSwitchRequests() {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    IReadOnlyList<Callsign> awaitings = prm.forAtc().getSwitchRequestsToRepeatByAtc(this.getAtcId());
    for (Callsign callsign : awaitings) {
      if (speechDelayer
          .isAny(q -> q.getContent() instanceof PlaneSwitchRequest && ((PlaneSwitchRequest) q.getContent()).getSquawk().equals(callsign)))
        continue; // if message about this plane is delayed and waiting to process
      Squawk sqwk = InternalAcc.getSquawkFromCallsign(callsign);
      Message m = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAtc(InternalAcc.getAtc(AtcType.app).getAtcId()),
          PlaneSwitchRequest.createFromComputer(sqwk, true));
      MessagingAcc.getMessenger().send(m);
      super.getRecorder().write(m);
    }
  }

  protected void requestNewSwitch(Callsign callsign, AtcId targetAtcId) {
    PlaneResponsibilityManager prm = InternalAcc.getPrm();
    prm.forAtc().createSwitchRequest(this.getAtcId(), targetAtcId, callsign);
    Squawk sqwk = InternalAcc.getSquawkFromCallsign(callsign);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        PlaneSwitchRequest.createFromComputer(sqwk, false));
    sendMessage(m);
  }

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }

  protected abstract boolean shouldBeSwitched(Callsign plane);

  private void switchConfirmedPlanesIfReady() {
    IReadOnlyList<Callsign> planes = InternalAcc.getPrm().forAtc().getConfirmedSwitchesByAtc(this.getAtcId(), true);
    for (Callsign plane : planes) {
      if (this.shouldBeSwitched(plane))
        this.applySwitchHangOff(plane);
    }
  }
}
