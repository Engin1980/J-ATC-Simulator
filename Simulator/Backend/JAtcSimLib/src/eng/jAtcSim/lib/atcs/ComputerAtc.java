package eng.jAtcSim.lib.atcs;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplane4Atc;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRO;
import eng.jAtcSim.lib.atcs.planeResponsibility.SwitchRoutingRequest;
import eng.jAtcSim.lib.global.DelayedList;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

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

  public ComputerAtc(AtcTemplate template) {
    super(template);
  }

  public void elapseSecond() {

    IList<Message> msgs = Acc.messenger().getMessagesByListener(this, true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    switchConfirmedPlanesIfReady();
    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();

  }

  private void switchConfirmedPlanesIfReady() {
    IReadOnlyList<IAirplane4Atc> planes = getPrm().getConfirmedSwitchesByAtc(this, true);
    for (IAirplane4Atc plane : planes) {
      if (this.shouldBeSwitched(plane))
        this.applySwitchHangOff(plane);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        recorder.write(m); // incoming item

        if (m.isSourceOfType(IAirplane4Atc.class)) {
          // messages from planes
          IAirplane4Atc p = m.getSource();
          SpeechList spchs = m.getContent();

          if (spchs.containsType(GoodDayNotification.class))
            confirmGoodDayNotificationIfRequired(p, spchs);
          processMessagesFromPlane(p, spchs);
        } else if (m.getSource() instanceof Atc) {
          elapseSecondProcessMessageFromAtc(m);
        }
      } catch (Exception ex) {
        throw new EApplicationException(sf(
            "Failed to process a message for Atc. Atc: %s. Message from %s. Message itself: %s.",
            this.getName(),
            m.getSource().getName(),
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
      SwitchRoutingRequest srr = getPrm().getRoutingForSwitchRequest(this,plane);
      if (srr != null){
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

  private void rejectChangedRouting(IAirplane4Atc plane, Atc targetAtc){
    getPrm().resetSwitchRequest(this, plane);
    Message m = new Message(this, targetAtc, new StringMessageContent( plane.getSqwk() + "{" + plane.getFlightModule().getCallsign() + "} routing change rejected."));
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

  protected abstract boolean shouldBeSwitched(IAirplane4Atc plane);

  protected abstract RequestResult canIAcceptPlane(IAirplane4Atc p);

  private void confirmGoodDayNotificationIfRequired(IAirplane4Atc p, SpeechList spchs) {
    IList<GoodDayNotification> gdns = spchs.where(q -> q instanceof GoodDayNotification);
    // todo implement directly into if without gdns variable
    gdns = gdns.where(q -> q.isRepeated() == false);
    if (gdns.isEmpty() == false) {
      SpeechList lst = new SpeechList();
      lst.add(new RadarContactConfirmationNotification());
      if (getPrm().getResponsibleAtc(p) != this) {
        lst.add(new ContactCommand(eType.app));
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
  @Nullable
  protected abstract Atc getTargetAtcIfPlaneIsReadyToSwitch(@NotNull IAirplane4Atc plane);

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

  private void applySwitchHangOff(IAirplane4Atc plane) {
    getPrm().applyConfirmedSwitch(this, plane);
    Atc newTargetAtc = getPrm().getResponsibleAtc(plane);
    Message msg = new Message(this, plane,
        new SpeechList<>(
            new ContactCommand(newTargetAtc.getType())));
    Acc.messenger().send(msg);
  }

  @Override
  protected void _save(XElement elm) {
  }

  @Override
  protected void _load(XElement elm) {
  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }
}
