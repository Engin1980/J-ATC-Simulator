package eng.jAtcSim.lib.atcs;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneList;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

import java.util.ArrayList;
import java.util.List;

public abstract class ComputerAtc extends Atc {

  public static class RequestResult {
    public final boolean isAccepted;
    public final String message;

    public RequestResult(boolean isAccepted, String message) {
      this.isAccepted = isAccepted;
      this.message = message;
    }
  }

  private final WaitingList waitingRequestsList = new WaitingList();
  private final IList<SwitchRequest> confirmedRequestList = new EList<>();


  public ComputerAtc(AtcTemplate template) {
    super(template);
  }

  public void elapseSecond() {

    List<Message> msgs = Acc.messenger().getByTarget(this, true);

    for (Message m : msgs) {
      recorder.write(m); // incoming speech

      if (m.isSourceOfType(Airplane.class)) {
        // messages from planes
        Airplane p = m.getSource();
        SpeechList spchs = m.getContent();

        confirmGoodDayNotificationIfRequired(p, spchs);
        processMessagesFromPlane(p, spchs);
      } else if (m.getSource() instanceof Atc) {
        elapseSecondProcessMessageFromAtc(m);
      }
    }
    List<SwitchRequest> srs = new ArrayList<>();
    for (SwitchRequest sr : this.confirmedRequestList) {
      if (shouldBeSwitched(sr.airplane))
        srs.add(sr);
    }
    for (SwitchRequest sr : srs) {
      this.confirmedRequestList.remove(sr);
      if (Acc.prm().isApprovedToSwitch(sr.airplane)) {
        this.approveSwitch(sr.airplane);
        Message nm = new Message(this, sr.airplane,
            new SpeechList<>(new ContactCommand(sr.atc.getType()))
        );
        this.sendMessage(nm);
      } else {
        Message nm = new Message(this, sr.atc,
            new PlaneSwitchMessage(sr.airplane, true, " refused. This airplane is not intended to be switched."));
        this.sendMessage(nm);
      }
    }

    checkAndProcessPlanesReadyToSwitch();
    repeatOldSwitchRequests();
  }

  private void elapseSecondProcessMessageFromAtc(Message m) {
    if (m.getContent() instanceof PlaneSwitchMessage) {
      // messages from ATCs
      Airplane plane = m.<PlaneSwitchMessage>getContent().plane;
      Atc targetAtc = m.getSource();
      if (this.waitingRequestsList.contains(plane)) {
        // p is waiting to be switch-confirmed
        this.waitingRequestsList.remove(plane);
        this.confirmedRequestList.add(
            new SwitchRequest(targetAtc, plane));
      } else {
        // other ATC asks to let us accept the plane
        RequestResult planeAcceptance = canIAcceptPlane(plane);
        if (planeAcceptance.isAccepted) {
          this.confirmSwitch(plane, targetAtc);
          this.approveSwitch(plane);
        } else {
          this.refuseSwitch(plane, targetAtc, planeAcceptance.message);
        }
      }
    } else {
      processMessageFromAtc(m);
    }
  }

  @Override
  public void init() {
  }

  @Override
  public boolean isHuman() {
    return false;
  }

  protected abstract void processMessageFromAtc(Message m);

  protected abstract boolean shouldBeSwitched(Airplane plane);

  protected abstract RequestResult canIAcceptPlane(Airplane p);

  private void confirmGoodDayNotificationIfRequired(Airplane p, SpeechList spchs) {
    if (spchs.containsType(GoodDayNotification.class)) {
      SpeechList lst = new SpeechList();
      lst.add(new RadarContactConfirmationNotification());
      if (Acc.prm().getResponsibleAtc(p) != this) {
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
   *
   * @param plane Plane checked if ready to switch
   * @return Target atc, or null if plane not ready to switch.
   */
  @Nullable
  protected abstract Atc getTargetAtcIfPlaneIsReadyToSwitch(@NotNull Airplane plane);

  private void repeatOldSwitchRequests() {
    // opakovani starych zadosti
    List<Airplane> awaitings = this.waitingRequestsList.getAwaitings();
    for (Airplane p : awaitings) {
      Message m = new Message(this, Acc.atcApp(),
          new PlaneSwitchMessage(p, false, " to you (repeated)"));
      Acc.messenger().send(m);
      recorder.write(m);
    }
  }

  protected void requestSwitch(Airplane plane, Atc targetAtc) {
    getPrm().requestSwitch(this, targetAtc, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, false, " to you"));
    sendMessage(m);
  }

  protected void approveSwitch(Airplane plane) {
    getPrm().approveSwitch(plane);
    recorder.write(this, "OTH", "approveSwitch " + plane.getCallsign().toString());
  }

  protected void confirmSwitch(Airplane plane, Atc targetAtc) {
    getPrm().confirmSwitch(this, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, false, "accepted"));
    sendMessage(m);
  }

  protected void refuseSwitch(Airplane plane, Atc targetAtc, String message) {
    getPrm().refuseSwitch(this, plane);
    Message m = new Message(this, targetAtc,
        new PlaneSwitchMessage(plane, true, " refused. " + message));
    sendMessage(m);
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
}

class SwitchRequest {
  public final Atc atc;
  public final Airplane airplane;

  public SwitchRequest(Atc atc, Airplane airplane) {
    this.atc = atc;
    this.airplane = airplane;
  }
}
