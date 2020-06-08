//package eng.jAtcSim.newLib.area.airplanes.modules;
//
//import eng.jAtcSim.newLib.Acc;
//import eng.jAtcSim.newLib.area.airplanes.Airplane;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
//import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IDivertModuleRO;
//import eng.jAtcSim.newLib.global.ETime;
//import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.DivertTimeNotification;
//
//public class DivertModule extends Module implements IDivertModuleRO {
//
//  private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};
//  private ETime divertTime;
//  private int lastAnnouncedMinute = Integer.MAX_VALUE;
//  private boolean isPossible = true;
//
//  public DivertModule(IAirplaneWriteSimple parent) {
//    super(parent);
//  }
//
//  public void disable() {
//    this.isPossible = false;
//  }
//
//  public void elapseSecond() {
//    checkForDivert();
//  }
//
//  @Override
//  public int getMinutesLeft() {
//    int diff = divertTime.getTotalMinutes() - Acc.now().getTotalMinutes();
//    return diff;
//  }
//
//  public void init(ETime divertTime) {
//    this.divertTime = divertTime;
//  }
//
//  private void adviceDivertTimeIfRequested() {
//    assert this.isPossible;
//    int minLeft = getMinutesLeft();
//    for (int dit : divertAnnounceTimes) {
//      if (lastAnnouncedMinute > dit && minLeft < dit) {
//        parent.sendMessage(
//            new DivertTimeNotification(minLeft));
//        this.lastAnnouncedMinute = minLeft;
//        break;
//      }
//    }
//  }
//
//  private void checkForDivert() {
//    if (this.isPossible
//        && parent.getBehaviorModule().get().isDivertable()
//        && parent.getState().is(
//        Airplane.State.arrivingHigh, Airplane.State.arrivingLow,
//        Airplane.State.holding)
//        && parent.getEmergencyModule().isEmergency() == false) {
//      boolean isDiverting = this.divertIfRequested();
//      if (!isDiverting) {
//        adviceDivertTimeIfRequested();
//      }
//    }
//  }
//
//  private boolean divertIfRequested() {
//    assert this.isPossible;
//    if (this.getMinutesLeft() <= 0) {
//      parent.getAdvanced().divert(false);
//      return true;
//    } else {
//      return false;
//    }
//  }
//}
