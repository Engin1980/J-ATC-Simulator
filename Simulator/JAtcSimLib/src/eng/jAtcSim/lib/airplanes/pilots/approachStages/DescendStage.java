package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.world.Navaid;

public class DescendStage implements IApproachStage {
  private final Navaid navaid;
  private final int altitude;
  private final double slope;
  private final Navaid exitFix;
  private final Integer exitAltitude;

  public DescendStage(Navaid navaid, int altitude, double slope, Navaid exitFix, Integer exitAltitude) {
    Validator.check(exitFix != null || exitAltitude != null);
    this.navaid = navaid;
    this.altitude = altitude;
    this.slope = slope;
    this.exitFix = exitFix;
    this.exitAltitude = exitAltitude;
  }
}
