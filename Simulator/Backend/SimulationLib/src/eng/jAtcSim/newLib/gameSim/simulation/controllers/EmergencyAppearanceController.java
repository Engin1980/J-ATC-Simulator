package eng.jAtcSim.newLib.gameSim.simulation.controllers;

import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class EmergencyAppearanceController {
  private EDayTimeStamp nextEmergencyTime;
  private double emergencyPerDayProbability;

  public EmergencyAppearanceController(double emergencyPerDayProbability) {
    this.emergencyPerDayProbability = emergencyPerDayProbability;
  }

  public EDayTimeStamp getNextEmergencyTime() {
    return nextEmergencyTime;
  }

  public void generateEmergencyTime(EDayTime now) {
    if (emergencyPerDayProbability > 0) {
      int secondsToNextEmerg = (int) ((60 * 60 * 24) / emergencyPerDayProbability);
      secondsToNextEmerg = SharedAcc.getRnd().nextInt(secondsToNextEmerg);
      this.nextEmergencyTime = now.addSeconds(secondsToNextEmerg);
    }
  }

  public boolean isEmergencyTimeElapsed(EDayTime now) {
    boolean ret =  this.nextEmergencyTime != null && this.nextEmergencyTime.isBefore(now);
    return ret;
  }
}
