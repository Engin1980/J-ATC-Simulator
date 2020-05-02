package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.DelayedList;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.RadarContactConfirmationNotification;
import eng.jAtcSim.newLib.speeches.atc2atc.PlaneSwitchMessage;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class ComputerAtc extends Atc {

  private static int MINIMUM_ATC_SPEECH_DELAY_SECONDS = 2;
  private static int MAXIMUM_ATC_SPEECH_DELAY_SECONDS = 10;


  public static class RequestResult {
    public final boolean isAccepted;
    public final String message;

    public RequestResult(boolean isAccepted, String message) {
      this.isAccepted = isAccepted;
      this.message = message;
    }
  }

  private final DelayedList<Message> speechDelayer = new DelayedList<>(
      MINIMUM_ATC_SPEECH_DELAY_SECONDS, MAXIMUM_ATC_SPEECH_DELAY_SECONDS);

  public ComputerAtc(eng.jAtcSim.newLib.area.Atc template) {
    super(template);
  }

  public void elapseSecond() {

    IList<Message> msgs = MessagingAcc.getMessenger().getMessagesByListener(this, true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    switchConfirmedPlanesIfReady();
    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();

  }

  private void switchConfirmedPlanesIfReady() {
    IReadOnlyList<Callsign> planes = XAcc.getPrm().forAtc().getConfirmedSwitchesByAtc(this.getAtcId(), true);
    for (Callsign plane : planes) {
      if (this.shouldBeSwitched(plane))
        this.applySwitchHangOff(plane);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        recorder.write(m); // incoming item

        if (m.getSource().getType() == Participant.eType.airplane) {
          // messages from planes
          Callsign callsign = new Callsign(m.getSource().getId());
          SpeechList spchs = m.getContent();

          if (spchs.containsType(GoodDayNotification.class))
            confirmGoodDayNotificationIfRequired(callsign, spchs);
          processMessagesFromPlane(callsign, spchs);
        } else if (m.getSource().getType() == Participant.eType.atc) {
          elapseSecondProcessMessageFromAtc(m);
        }
      } catch (Exception ex) {
        throw new EApplicationException(sf(
            "Failed to process a message for Atc. Atc: %s. Message from %s. Message itself: %s.",
            this.getName(),
            m.getSource().getId(),
            m.toString()), ex);
      }
    }
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    if (m.getContent() instanceof PlaneSwitchMessage) {
      processPlaneSwitchMessage(m);
    } else {
      processNonPlaneSwitchMessageFromAtc(m);
    }
  }

  protected abstract boolean acceptsNewRouting(Callsign callsign, SwitchRoutingRequest srr);

  private void processPlaneSwitchMessage(Message m) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    PlaneSwitchMessage psm = m.getContent();
    Callsign callsign = psm.getCallsign();
    EAssert.isTrue(m.getSource().getType() == Participant.eType.atc);
    AtcId targetAtcId = XAcc.getAtc(m.getSource().getId()).getAtcId();
    if (prm.forAtc().isUnderSwitchRequest(callsign, this.getAtcId(), targetAtcId)) {
      // other ATC confirms our request, plane is going to hang off
      SwitchRoutingRequest srr =prm.forAtc().getRoutingForSwitchRequest(this.getAtcId(), callsign);
      if (srr != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (acceptsNewRouting(callsign, srr) == false)
          rejectChangedRouting(callsign, targetAtcId);
        else
          prm.forAtc().confirmRerouting(this.getAtcId(), callsign);
      }
    } else if (prm.forAtc().isUnderSwitchRequest(callsign, null, this.getAtcId())) {
      // other ATC offers us a plane
      RequestResult planeAcceptance = canIAcceptPlane(callsign);
      if (planeAcceptance.isAccepted) {
        acceptSwitch(callsign, targetAtcId);
      } else {
        rejectSwitch(callsign, targetAtcId, planeAcceptance);
      }
    }
  }

  private void rejectChangedRouting(Callsign callsign, AtcId targetAtcId) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    prm.forAtc().resetSwitchRequest(this.getAtcId(), callsign);
    IAirplane plane = XAcc.getPlane(callsign);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new StringMessageContent(plane.getSqwk() + "{" + plane.getCallsign() + "} routing change rejected."));
    sendMessage(m);
  }

  private void rejectSwitch(Callsign callsign, AtcId targetAtcId, RequestResult planeAcceptance) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    prm.forAtc().rejectSwitchRequest(callsign, this.getAtcId());
    Message nm = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new PlaneSwitchMessage(callsign, PlaneSwitchMessage.eMessageType.rejection, planeAcceptance.message));
    sendMessage(nm);
  }

  private void acceptSwitch(Callsign callsign, AtcId targetAtcId) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    prm.forAtc().confirmSwitchRequest(callsign, this.getAtcId(), null);
    Message nm = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new PlaneSwitchMessage(callsign, PlaneSwitchMessage.eMessageType.confirmation));
    sendMessage(nm);
  }

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract boolean shouldBeSwitched(Callsign plane);

  protected abstract RequestResult canIAcceptPlane(Callsign callsign);

  private void confirmGoodDayNotificationIfRequired(Callsign callsign, SpeechList spchs) {
    IList<GoodDayNotification> gdns = spchs.where(q -> q instanceof GoodDayNotification);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList lst = new SpeechList();
      lst.add(new RadarContactConfirmationNotification());
      if (XAcc.getPrm().forAtc().getResponsibleAtc(callsign).equals(this.getAtcId())) {
        AtcId atcId = XAcc.getAtc(AtcType.app).getAtcId();
        lst.add(new ContactCommand(atcId));
      }
      Message msg = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAirplane(callsign),
          lst);
      sendMessage(msg);
    }
  }

  protected abstract void processMessagesFromPlane(Callsign callsign, SpeechList spchs);

  /**
   * Checks for planes ready to switch and switch them.
   */
  private void checkAndProcessPlanesReadyToSwitch() {
    PlaneResponsibilityManager prm = XAcc.getPrm();
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

  /**
   * Returns target atc if plane is ready for switch.
   *
   * @param callsign Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  protected abstract AtcId getTargetAtcIfPlaneIsReadyToSwitch(Callsign callsign);

  private void repeatOldSwitchRequests() {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    IReadOnlyList<Callsign> awaitings = prm.forAtc().getSwitchRequestsToRepeatByAtc(this.getAtcId());
    for (Callsign callsign : awaitings) {
      if (speechDelayer
          .isAny(q -> q.getContent() instanceof PlaneSwitchMessage && ((PlaneSwitchMessage) q.getContent()).getCallsign().equals(callsign)))
        continue; // if message about this plane is delayed and waiting to process
      Message m = new Message(
          Participant.createAtc(this.getAtcId()),
          Participant.createAtc(XAcc.getAtc(AtcType.app).getAtcId()),
          new PlaneSwitchMessage(callsign, PlaneSwitchMessage.eMessageType.request, "(repeated)"));
      MessagingAcc.getMessenger().send(m);
      recorder.write(m);
    }
  }

  protected void requestNewSwitch(Callsign callsign, AtcId targetAtcId) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    prm.forAtc().createSwitchRequest(this.getAtcId(), targetAtcId, callsign);
    Message m = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAtc(targetAtcId),
        new PlaneSwitchMessage(callsign, PlaneSwitchMessage.eMessageType.request));
    sendMessage(m);
  }

  private void applySwitchHangOff(Callsign callsign) {
    PlaneResponsibilityManager prm = XAcc.getPrm();
    prm.forAtc().applyConfirmedSwitch(this.getAtcId(), callsign);
    AtcId newTargetAtc = prm.getResponsibleAtc(callsign);
    Message msg = new Message(
        Participant.createAtc(this.getAtcId()),
        Participant.createAirplane(callsign),
        new SpeechList<>(
            new ContactCommand(newTargetAtc)));
    MessagingAcc.getMessenger().send(msg);
  }

//  @Override
//  protected void _save(XElement elm) {
//  }
//
//  @Override
//  protected void _load(XElement elm) {
//  }


  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }
}