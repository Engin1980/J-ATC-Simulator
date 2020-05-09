package eng.jAtcSim.newLib.speeches.atc.atc2user;

import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

public abstract class RunwayMaintenanceBaseNotification implements IAtcSpeech {
  private final String runwayName;

  protected RunwayMaintenanceBaseNotification(String runwayName) {
    this.runwayName = runwayName;
  }

  public String getRunwayName() {
    return runwayName;
  }
}
