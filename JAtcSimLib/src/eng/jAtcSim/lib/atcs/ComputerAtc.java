package eng.jAtcSim.lib.atcs;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.util.ArrayList;
import java.util.List;

public abstract class ComputerAtc extends Atc {

  private final WaitingList waitingRequestsList = new WaitingList();
  private final List<SwitchRequest> confirmedRequestList = new ArrayList<>();

  public ComputerAtc(AtcTemplate template) {
    super(template);
  }

  public void elapseSecond(){

    List<Message> msgs = Acc.messenger().getByTarget(this,true);

    checkAndProcessPlanesReadyToSwitch();

    for (Message m : msgs) {
      recorder.logMessage(m); // incoming speech

      if (m.isSourceOfType(Airplane.class)) {
        // messages from planes
        Airplane p = m.getSource();
        SpeechList spchs = m.getContent();

        confirmGoodDayNotificationIfRequired(p, spchs);
        processMessagesFromPlane(p, spchs);
      } else if (m.getSource() instanceof Atc){
        // messages from ATCs
        Airplane plane = m.<PlaneSwitchMessage>getContent().plane;
        Atc targetAtc = m.getSource();
        if (this.waitingRequestsList.contains(plane)){
          // p is waiting to be switch-confirmed

            this.waitingRequestsList.remove(plane);
            this.confirmedRequestList.add(
                new SwitchRequest(targetAtc, plane));

        } else {
          // p is not in waiting list, so other ATC asks ...
          // to let us accept the plane

          if (canIAcceptPlane(plane)) {
            this.confirmSwitch(plane, targetAtc);
            this.approveSwitch(plane);
          } else {
            this.refuseSwitch(plane, targetAtc);
          }
        }
      }
    }

    List<SwitchRequest> srs = new ArrayList<>();
    for (SwitchRequest sr : this.confirmedRequestList) {
      if (shouldBeSwitched(sr.airplane))
        srs.add(sr);
    }
    for (SwitchRequest sr : srs) {
        this.confirmedRequestList.remove(sr);
        this.approveSwitch(sr.airplane);
        Message nm = new Message(this, sr.airplane,
            new SpeechList<>(new ContactCommand(sr.atc.getType()))
        );
        this.sendMessage(nm);
    }

    repeatOldSwitchRequests();
  }

  protected abstract boolean shouldBeSwitched(Airplane plane);

  protected abstract boolean canIAcceptPlane(Airplane p);

  private void confirmGoodDayNotificationIfRequired(Airplane p, SpeechList spchs) {
    if (spchs.containsType(GoodDayNotification.class)){
      Message msg;
      msg = new Message(
          this,
          p,
          new SpeechList(new RadarContactConfirmationNotification()));
      sendMessage(msg);
    }
    doAfterGoodDayNotificationConfirmation(p);
  }

  /**
   * Do things when new airplane is switched to me.
   * @param p
   */
  protected abstract void doAfterGoodDayNotificationConfirmation(Airplane p);

  protected abstract void processMessagesFromPlane(Airplane p, SpeechList spchs);

  /**
   * Checks for planes ready to switch and switch them.
   */
  private void checkAndProcessPlanesReadyToSwitch() {

    AirplaneList myPlanes = getPrm().getPlanes(this);
    for (Airplane myPlane : myPlanes) {
      if (getPrm().isAskedToSwitch(myPlane))
        continue;

      Atc targetAtc = getTargetAtcIfPlaneIsReadyToSwitch(myPlane);
      if (targetAtc != null) {
        this.requestSwitch(myPlane, targetAtc);
        this.waitingRequestsList.add(myPlane);
      }
    }
  }

  /**
   * Returns target atc if plane is ready for switch.
   * @param plane Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  @Nullable
  protected abstract Atc getTargetAtcIfPlaneIsReadyToSwitch(@NotNull Airplane plane);

  protected void sendMessage(Message msg){
    Acc.messenger().send(msg);
    recorder.logMessage(msg);
  }

  private void repeatOldSwitchRequests() {
    // opakovani starych zadosti
    List<Airplane> awaitings = this.waitingRequestsList.getAwaitings();
    for (Airplane p : awaitings) {
      Message m = new Message(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, " to you (repeated)"));
      Acc.messenger().send(m);
      recorder.logMessage(m);
    }
  }

  protected void requestSwitch(Airplane plane, Atc targetAtc) {
    getPrm().requestSwitch(this, targetAtc, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, " to you"));
    sendMessage(m);
  }

  protected void approveSwitch(Airplane plane) {
    getPrm().approveSwitch(plane);
    recorder.log(this, "OTH", "approveSwitch " + plane.getCallsign().toString());
  }

  protected void confirmSwitch(Airplane plane, Atc targetAtc) {
    getPrm().confirmSwitch(this, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, " accepted"));
    sendMessage(m);
  }

  protected void refuseSwitch(Airplane plane, Atc targetAtc) {
    getPrm().refuseSwitch(this, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, " refused. Not in my coverage."));
    sendMessage(m);
  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }
}

class SwitchRequest{
  public final Atc atc;
  public final Airplane airplane;

  public SwitchRequest(Atc atc, Airplane airplane) {
    this.atc = atc;
    this.airplane = airplane;
  }
}
