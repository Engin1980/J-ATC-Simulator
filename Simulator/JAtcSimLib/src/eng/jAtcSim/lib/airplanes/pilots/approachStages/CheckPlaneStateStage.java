package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.collections.*;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;

public class CheckPlaneStateStage implements IApproachStage{
  private final Integer minAltitude;
  private final Integer maxAltitude;
  private final Integer minHeading;
  private final Integer maxHeading;
  private final Integer minSpeed;
  private final Integer maxSpeed;

  public CheckPlaneStateStage(Integer minAltitude, Integer maxAltitude, Integer minHeading, Integer maxHeading, Integer minSpeed, Integer maxSpeed) {
    this.minAltitude = minAltitude;
    this.maxAltitude = maxAltitude;
    this.minHeading = minHeading;
    this.maxHeading = maxHeading;
    this.minSpeed = minSpeed;
    this.maxSpeed = maxSpeed;
  }
}
