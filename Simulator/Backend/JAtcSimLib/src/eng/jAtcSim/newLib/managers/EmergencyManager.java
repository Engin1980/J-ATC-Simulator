package eng.jAtcSim.newLib.area.managers;

;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.global.ETime;

public class EmergencyManager {

  private ETime nextEmergencyTime;
  private double emergencyPerDayProbability;

  @XmlConstructor
  private EmergencyManager() {
  }

  public EmergencyManager(double emergencyPerDayProbability) {
    this.emergencyPerDayProbability = emergencyPerDayProbability;
  }

  public ETime getNextEmergencyTime() {
    return nextEmergencyTime;
  }

  public void generateEmergencyTime(ETime now) {
    if (emergencyPerDayProbability > 0) {
      int secondsToNextEmerg = (int) ((60 * 60 * 24) / emergencyPerDayProbability);
      secondsToNextEmerg = Acc.rnd().nextInt(secondsToNextEmerg);
      this.nextEmergencyTime = now.addSeconds(secondsToNextEmerg);
    }
  }

  public boolean isEmergencyTimeElapsed() {
    boolean ret =  this.nextEmergencyTime != null && this.nextEmergencyTime.isBefore(Acc.now());
    return ret;
  }
}
