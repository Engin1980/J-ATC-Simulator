package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.messaging.StringMessageContent;
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

    IList<Message> msgs = LAcc.getMessenger().getMessagesByListener(this, true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    switchConfirmedPlanesIfReady();
    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();

  }

  private void switchConfirmedPlanesIfReady() {
    IReadOnlyList<Callsign> planes = LAcc.getPrm().forAtc().getConfirmedSwitchesByAtc(this.getAtcId(), true);
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

  protected abstract boolean acceptsNewRouting(IAirplane4Atc plane, SwitchRoutingRequest srr);

  private void processPlaneSwitchMessage(Message m) {
    IAirplaneRO planeRO = m.<PlaneSwitchMessage>getContent().plane;
    IAirplane4Atc plane = (IAirplane4Atc) planeRO; //TODO this is hack
    Atc targetAtc = m.getSource();
    if (getPrm().isUnderSwitchRequest(plane, this, targetAtc)) {
      // other ATC confirms our request, plane is going to hang off
      SwitchRoutingRequest srr = getPrm().getRoutingForSwitchRequest(this, plane);
      if (srr != null) {
        // the other ATC tries to change plane routing, we can check in and reject it if required
        if (acceptsNewRouting(plane, srr) == false)
          rejectChangedRouting(plane, targetAtc);
        else
          getPrm().confirmRerouting(this, plane);
      }
    } else if (getPrm().isUnderSwitchRequest(plane, null, this)) {
      // other ATC offers us a plane
      RequestResult planeAcceptance = canIAcceptPlane(plane);
      if (planeAcceptance.isAccepted) {
        acceptSwitch(plane, targetAtc);
      } else {
        rejectSwitch(plane, targetAtc, planeAcceptance);
      }
    }
  }

  private void rejectChangedRouting(IAirplane4Atc plane, Atc targetAtc) {
    getPrm().resetSwitchRequest(this, plane);
    Message m = new Message(this, targetAtc, new StringMessageContent(plane.getSqwk() + "{" + plane.getFlightModule().getCallsign() + "} routing change rejected."));
    sendMessage(m);
  }

  private void rejectSwitch(IAirplane4Atc plane, Atc targetAtc, RequestResult planeAcceptance) {
    getPrm().rejectSwitchRequest(plane, this);
    Message nm = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, PlaneSwitchMessage.eMessageType.rejection, planeAcceptance.message));
    sendMessage(nm);
  }

  private void acceptSwitch(IAirplane4Atc plane, Atc targetAtc) {
    getPrm().confirmSwitchRequest(plane, this, null);
    Message nm = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, PlaneSwitchMessage.eMessageType.confirmation));
    sendMessage(nm);
  }

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract boolean shouldBeSwitched(Callsign plane);

  protected abstract RequestResult canIAcceptPlane(IAirplane4Atc p);

  private void confirmGoodDayNotificationIfRequired(Callsign p, SpeechList spchs) {
    IList<GoodDayNotification> gdns = spchs.where(q -> q instanceof GoodDayNotification);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList lst = new SpeechList();
      lst.add(new RadarContactConfirmationNotification());
      if (LAcc.getPrm().forAtc().getResponsibleAtc(p) != this) {
        lst.add(new ContactCommand(AtcType.app));
      }
      Message msg = new Message(this, p, lst);
      sendMessage(msg);
    }
  }

  protected abstract void processMessagesFromPlane(IAirplane4Atc p, SpeechList spchs);

  /**
   * Checks for planes ready to switch and switch them.
   */
  private void checkAndProcessPlanesReadyToSwitch() {

    IReadOnlyList<IAirplane4Atc> myPlanes = getPrm().getPlanes(this);
    for (IAirplane4Atc myPlane : myPlanes) {
      if (getPrm().isUnderSwitchRequest(myPlane, this, null))
        continue;

      Atc targetAtc = getTargetAtcIfPlaneIsReadyToSwitch(myPlane);
      if (targetAtc != null) {
        this.requestNewSwitch(myPlane, targetAtc);
      }
    }
  }

  /**
   * Returns target atc if plane is ready for switch.
   *
   * @param plane Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  protected abstract Atc getTargetAtcIfPlaneIsReadyToSwitch(IAirplane4Atc plane);

  private void repeatOldSwitchRequests() {
    IReadOnlyList<IAirplane4Atc> awaitings = getPrm().getSwitchRequestsToRepeatByAtc(this);
    for (IAirplane4Atc p : awaitings) {
      if (speechDelayer.isAny(q -> q.getContent() instanceof PlaneSwitchMessage && ((PlaneSwitchMessage) q.getContent()).plane.equals(p)))
        continue; // if message about this plane is delayed and waiting to process
      Message m = new Message(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, PlaneSwitchMessage.eMessageType.request, "(repeated)"));
      Acc.messenger().send(m);
      recorder.write(m);
    }
  }

  protected void requestNewSwitch(IAirplane4Atc plane, Atc targetAtc) {
    getPrm().createSwitchRequest(this, targetAtc, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, PlaneSwitchMessage.eMessageType.request));
    sendMessage(m);
  }

  private void applySwitchHangOff(Callsign plane) {
    getPrm().applyConfirmedSwitch(this, plane);
    Atc newTargetAtc = getPrm().getResponsibleAtc(plane);
    Message msg = new Message(this, plane,
        new SpeechList<>(
            new ContactCommand(newTargetAtc.getType())));
    LAcc.getMessenger().send(msg);
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
