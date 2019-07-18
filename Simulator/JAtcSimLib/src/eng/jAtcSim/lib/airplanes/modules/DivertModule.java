package eng.jAtcSim.lib.airplanes.modules;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.interfaces.modules.IDivertModuleRO;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;

public class DivertModule extends Module implements IDivertModuleRO {

  private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};
  private ETime divertTime;
  private int lastAnnouncedMinute = Integer.MAX_VALUE;
  private boolean isPossible = true;

  public DivertModule(IAirplaneWriteSimple parent) {
    super(parent);
  }

  public void disable() {
    this.isPossible = false;
  }

  public void elapseSecond() {
    checkForDivert();
  }

  @Override
  public int getMinutesLeft() {
    int diff = divertTime.getTotalMinutes() - Acc.now().getTotalMinutes();
    return diff;
  }

  public void init(ETime divertTime) {
    this.divertTime = divertTime;
  }

  private void adviceDivertTimeIfRequested() {
    assert this.isPossible;
    int minLeft = getMinutesLeft();
    for (int dit : divertAnnounceTimes) {
      if (lastAnnouncedMinute > dit && minLeft < dit) {
        parent.sendMessage(
            new DivertTimeNotification(minLeft));
        this.lastAnnouncedMinute = minLeft;
        break;
      }
    }
  }

  private void checkForDivert() {
    if (this.isPossible
        && parent.getBehaviorModule().get().isDivertable()
        && parent.getState().is(
        Airplane.State.arrivingHigh, Airplane.State.arrivingLow,
        Airplane.State.holding)
        && parent.getEmergencyModule().isEmergency() == false) {
      boolean isDiverting = this.divertIfRequested();
      if (!isDiverting) {
        adviceDivertTimeIfRequested();
      }
    }
  }

  private boolean divertIfRequested() {
    assert this.isPossible;
    if (this.getMinutesLeft() <= 0) {
      parent.getAdvanced().divert(false);
      return true;
    } else {
      return false;
    }
  }
}
