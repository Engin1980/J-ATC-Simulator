package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

import exml.IXPersistable;
import exml.annotations.XConstructor;

public class EmergencyAppearanceController implements IXPersistable {
  private EDayTimeStamp nextEmergencyTime;
  private final double emergencyPerDayProbability;

  @XConstructor
  public EmergencyAppearanceController(double emergencyPerDayProbability) {
    this.emergencyPerDayProbability = emergencyPerDayProbability;
  }

  public EDayTimeStamp getNextEmergencyTime() {
    return nextEmergencyTime;
  }

  public void generateEmergencyTime(EDayTime now) {
    if (emergencyPerDayProbability > 0) {
      int secondsToNextEmerg = (int) ((60 * 60 * 24) / emergencyPerDayProbability);
      secondsToNextEmerg = Context.getApp().getRnd().nextInt(secondsToNextEmerg);
      this.nextEmergencyTime = now.addSeconds(secondsToNextEmerg);
    }
  }

  public boolean isEmergencyTimeElapsed(EDayTime now) {
    boolean ret =  this.nextEmergencyTime != null && this.nextEmergencyTime.isBefore(now);
    return ret;
  }
}
