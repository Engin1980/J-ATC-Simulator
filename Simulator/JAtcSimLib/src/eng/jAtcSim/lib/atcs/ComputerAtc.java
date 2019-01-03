package eng.jAtcSim.lib.atcs;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.DelayedList;
import eng.jAtcSim.lib.global.Global;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.serialization.LoadSave;
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

    IList<Message> msgs = Acc.messenger().getByTarget(this, true);
    speechDelayer.add(msgs);

    msgs = speechDelayer.getAndElapse();
    elapseSecondProcessMessagesForAtc(msgs);

    switchConfirmedPlanesIfReady();
    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();

  }

  private void switchConfirmedPlanesIfReady() {
    IReadOnlyList<Airplane> planes = getPrm().getConfirmedSwitchesByAtc(this);
    for (Airplane plane : planes) {
      if (this.shouldBeSwitched(plane))
        this.doSwitchApplication(plane);
    }
  }

  private void elapseSecondProcessMessagesForAtc(IList<Message> msgs) {
    for (Message m : msgs) {
      try {
        recorder.write(m); // incoming item

        if (m.isSourceOfType(Airplane.class)) {
          // messages from planes
          Airplane p = m.getSource();
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

  private void processPlaneSwitchMessage(Message m) {
    Airplane plane = m.<PlaneSwitchMessage>getContent().plane;
    Atc targetAtc = m.getSource();
    if (getPrm().isUnderSwitchRequest(plane, this, null)) {
      // other ATC confirms our request, plane is going to hang off
      this.doSwitchConfirmation(plane);
    } else {
      RequestResult planeAcceptance = canIAcceptPlane(plane);
      if (planeAcceptance.isAccepted) {
        doSwitchAcceptance(plane, targetAtc);
      } else {
        doSwitchRejection(plane, targetAtc, planeAcceptance);
      }
      // this is a request to accept the plane
//      if (getPrm().getResponsibleAtc(plane) == this){
//        if (plane.getTunedAtc().equals(Acc.atcApp())) {
//          this.abortSwitch(plane, plane.getTunedAtc());
//        } else {
//          this.refuseSwitch(plane, targetAtc, "Under my control, not intended to be switched.");
//        }
//      } else {
//        RequestResult planeAcceptance = canIAcceptPlane(plane);
//        if (planeAcceptance.isAccepted) {
//          this.confirmSwitch(plane, targetAtc);
//          this.approveSwitch(plane);
//        } else {
//          this.refuseSwitch(plane, targetAtc, planeAcceptance.message);
//        }
//      }
    }
  }

  private void doSwitchRejection(Airplane plane, Atc targetAtc, RequestResult planeAcceptance) {
    getPrm().abortSwitchFromApp(plane,this);
    Message nm = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, true, " refused. " + planeAcceptance.message));
    sendMessage(nm);
  }

  private void doSwitchAcceptance(Airplane plane, Atc targetAtc) {
    getPrm().confirmSwitchFromApp(plane,this);
    Message nm = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, false, "accepted"));
    sendMessage(nm);
  }

  protected abstract void processNonPlaneSwitchMessageFromAtc(Message m);

  protected abstract boolean shouldBeSwitched(Airplane plane);

  protected abstract RequestResult canIAcceptPlane(Airplane p);

  private void confirmGoodDayNotificationIfRequired(Airplane p, SpeechList spchs) {
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

  protected abstract void processMessagesFromPlane(Airplane p, SpeechList spchs);

  /**
   * Checks for planes ready to switch and switch them.
   */
  private void checkAndProcessPlanesReadyToSwitch() {

    IReadOnlyList<Airplane> myPlanes = getPrm().getPlanes(this);
    for (Airplane myPlane : myPlanes) {
      if (getPrm().isUnderSwitchRequest(myPlane, this, null))
        continue;

      Atc targetAtc = getTargetAtcIfPlaneIsReadyToSwitch(myPlane);
      if (targetAtc != null) {
        this.doSwitchRequest(myPlane, targetAtc);
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
  protected abstract Atc getTargetAtcIfPlaneIsReadyToSwitch(@NotNull Airplane plane);

  private void repeatOldSwitchRequests() {
    IReadOnlyList<Airplane> awaitings = getPrm().getSwitchRequestsToRepeatByAtc(this);
    for (Airplane p : awaitings) {
      if (speechDelayer.isAny(q -> q.getContent() instanceof PlaneSwitchMessage && ((PlaneSwitchMessage) q.getContent()).plane.equals(p)))
        continue; // if message about this plane is delayed and waiting to process
      Message m = new Message(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, false, "to you (repeated)"));
      Acc.messenger().send(m);
      recorder.write(m);
    }
  }

  protected void doSwitchRequest(Airplane plane, Atc targetAtc) {
    getPrm().registerSwitchRequest(this, targetAtc, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, false, "to you"));
    sendMessage(m);
  }

  protected void doSwitchConfirmation(Airplane plane) {

  }

  private void doSwitchApplication(Airplane plane) {
    getPrm().applyConfirmedSwitch(this, plane);
    Atc newTargetAtc = getPrm().getResponsibleAtc(plane);
    Message msg = new Message(this, plane,
        new SpeechList<>(
            new ContactCommand(newTargetAtc.getType())));
    Acc.messenger().send(msg);
  }

  @Override
  protected void _save(XElement elm) {
    LoadSave.saveField(elm, this, "waitingRequestsList");
    LoadSave.saveField(elm, this, "confirmedRequestList");
  }

  @Override
  protected void _load(XElement elm) {
    LoadSave.loadField(elm, this, "waitingRequestsList");
    LoadSave.loadField(elm, this, "confirmedRequestList");
  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }
}