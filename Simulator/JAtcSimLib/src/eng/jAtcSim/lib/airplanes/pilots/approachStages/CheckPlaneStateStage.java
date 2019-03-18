package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.global.Headings;

public class CheckPlaneStateStage extends CheckApproachStage {
  private final Integer minAltitude;
  private final Integer maxAltitude;
  private final Integer minHeading;
  private final Integer maxHeading;
  private final Integer minSpeed;
  private final Integer maxSpeed;

  public CheckPlaneStateStage(Integer minAltitude, Integer maxAltitude, Integer minHeading, Integer maxHeading, Integer minSpeed, Integer maxSpeed) {
    if (minHeading != null || maxHeading != null)
      Validator.check(
          minHeading != null && maxHeading != null,
          new IllegalArgumentException("Either none or both 'minHeading' and 'maxHeading' must be set."));
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
    this.minSpeed = minSpeed;
    this.maxSpeed = maxSpeed;
  }

  @Override
  protected eCheckResult check(IPilot4Behavior pilot) {
    if (minAltitude != null && pilot.getAltitude() < this.minAltitude)
      return eCheckResult.altitudeTooLow;
    if (maxAltitude != null && pilot.getAltitude() > this.maxAltitude)
      return eCheckResult.altitudeTooHigh;
    if (minHeading != null && Headings.isBetween(this.minHeading, pilot.getHeading(), this.maxHeading) == false)
      return eCheckResult.illegalHeading;
    if (minSpeed != null && pilot.getSpeed() < this.minSpeed)
      return eCheckResult.speedTooLow;
    if (maxSpeed != null && pilot.getSpeed() > this.maxSpeed)
      return eCheckResult.speedTooHigh;

    return eCheckResult.ok;
  }
}