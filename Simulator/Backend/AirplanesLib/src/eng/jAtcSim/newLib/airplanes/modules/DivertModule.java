package eng.jAtcSim.newLib.airplanes.modules;


import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.DivertTimeNotification;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public class DivertModule extends Module {
  private static final int MINIMAL_DIVERT_TIME_MINUTES = 45;
  private static final int MAXIMAL_DIVERT_TIME_MINUTES = 120;
  private static final int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};

  private static EDayTimeStamp generateDivertTime() {
    EDayTimeStamp now = Context.getShared().getNow().toStamp();
    int divertTimeMinutes = Context.getApp().getRnd().nextInt(MINIMAL_DIVERT_TIME_MINUTES, MAXIMAL_DIVERT_TIME_MINUTES);
    EDayTimeStamp ret = now.addMinutes(divertTimeMinutes);
    return ret;
  }
  private final EDayTimeStamp divertTime;
  private int lastAnnouncedMinute = Integer.MAX_VALUE;
  private boolean possible = true;

  public DivertModule(Airplane plane) {
    super(plane);
    this.divertTime = generateDivertTime();
  }

  public void disable() {
    this.possible = false;
  }

  @Override
  public void elapseSecond() {
    checkForDivert();
  }

  public EDayTimeStamp getDivertTime() {
    return divertTime;
  }

  public boolean isPossible() {
    return possible;
  }

  public void save(XElement target) {
    XmlSaveUtils.Field.storeFields(target, this, "lastAnnouncedMinute", "possible", "divertTime");
  }

  private void checkForDivert() {
    if (possible
            && rdr.getRouting().isDivertable()
            && rdr.getState().is(
            AirplaneState.arrivingHigh, AirplaneState.arrivingLow,
            AirplaneState.holding)
            && rdr.isEmergency() == false) {
      boolean isDiverting = this.divertIfRequested();
      if (!isDiverting) {
        adviceDivertTimeIfRequested();
      }
    }
  }

  private void adviceDivertTimeIfRequested() {
    int minLeft = getMinutesLeft();
    for (int dit : divertAnnounceTimes) {
      if (lastAnnouncedMinute > dit && minLeft < dit) {
        wrt.sendMessage(new DivertTimeNotification(minLeft));
        this.lastAnnouncedMinute = minLeft;
        break;
      }
    }
  }

  private int getMinutesLeft() {
    int diff = (int) Math.ceil((divertTime.getValue() - Context.getShared().getNow().getValue()) / 60d);
    return diff;
  }

  private boolean divertIfRequested() {
    if (this.getMinutesLeft() <= 0) {
      wrt.divert(false);
      return true;
    } else {
      return false;
    }
  }
}
