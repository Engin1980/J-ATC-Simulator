package eng.jAtcSim.newLib.area.approaches.stages.checks;

import eng.eSystem.validation.Validator;
import eng.jAtcSim.newLib.area.approaches.stages.ICheckStage;

public class CheckPlaneShaStage implements ICheckStage {
  private final Integer minAltitude;
  private final Integer maxAltitude;
  private final Integer minHeading;
  private final Integer maxHeading;
  private final Integer minSpeed;
  private final Integer maxSpeed;

  public CheckPlaneShaStage(Integer minAltitude, Integer maxAltitude, Integer minHeading, Integer maxHeading, Integer minSpeed, Integer maxSpeed) {
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

  public Integer getMinAltitude() {
    return minAltitude;
  }

  public Integer getMaxAltitude() {
    return maxAltitude;
  }

  public Integer getMinHeading() {
    return minHeading;
  }

  public Integer getMaxHeading() {
    return maxHeading;
  }

  public Integer getMinSpeed() {
    return minSpeed;
  }

  public Integer getMaxSpeed() {
    return maxSpeed;
  }

  //  @Override
//  protected eResult check(IPilot5Behavior pilot) {
//    if (minAltitude != null && pilot.getAltitude() < this.minAltitude)
//      return eResult.altitudeTooLow;
//    if (maxAltitude != null && pilot.getAltitude() > this.maxAltitude)
//      return eResult.altitudeTooHigh;
//    if (minHeading != null && Headings.isBetween(this.minHeading, pilot.getHeading(), this.maxHeading) == false)
//      return eResult.illegalHeading;
//    if (minSpeed != null && pilot.getSpeed() < this.minSpeed)
//      return eResult.speedTooLow;
//    if (maxSpeed != null && pilot.getSpeed() > this.maxSpeed)
//      return eResult.speedTooHigh;
//
//    return eResult.ok;
//  }
}
