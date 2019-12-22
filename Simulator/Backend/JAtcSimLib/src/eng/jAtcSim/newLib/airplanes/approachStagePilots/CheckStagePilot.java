package eng.jAtcSim.newLib.area.airplanes.approachStagePilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IShaRO;
import eng.jAtcSim.newLib.world.approaches.stages.ICheckStage;
import eng.jAtcSim.newLib.world.approaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.newLib.world.approaches.stages.checks.CheckPlaneLocationStage;
import eng.jAtcSim.newLib.world.approaches.stages.checks.CheckPlaneShaStage;

public class CheckStagePilot implements IApproachStagePilot<ICheckStage> {

  @Override
  public eResult disposeStage(IAirplaneWriteSimple plane, ICheckStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult flyStage(IAirplaneWriteSimple plane, ICheckStage stage) {
    return eResult.ok;
  }

  @Override
  public eResult initStage(IAirplaneWriteSimple plane, ICheckStage stage) {
    eResult ret;
    if (stage instanceof CheckAirportVisibilityStage)
      ret = evaluateCheckAirportVisibility(plane);
    else if (stage instanceof CheckPlaneLocationStage)
      ret = evaluateCheckPlaneLocationStage(plane, (CheckPlaneLocationStage) stage);
    else if (stage instanceof CheckPlaneShaStage)
      ret = evaluateCheckPlaneShaStage(plane, (CheckPlaneShaStage) stage);
    else
      throw new EApplicationException("CheckStagePilot does not support stage of type '" + stage.getClass().getSimpleName() + "'.");

    return ret;
  }

  @Override
  public boolean isFinishedStage(IAirplaneWriteSimple plane, ICheckStage stage) {
    return true;
  }

  private eResult evaluateCheckAirportVisibility(IAirplaneWriteSimple plane) {
    int planeAltitude = plane.getSha().getAltitude();

    int cloudsAltitude = Acc.weather().getCloudBaseInFt();
    double cloudProbability = Acc.weather().getCloudBaseHitProbability();
    if (planeAltitude > cloudsAltitude)
      if (Acc.rnd().nextDouble() < cloudProbability)
        return eResult.runwayNotInSight;
    return eResult.ok;
  }

  private eResult evaluateCheckPlaneLocationStage(IAirplaneWriteSimple plane, CheckPlaneLocationStage stage) {

    Coordinate planeCoordinate = plane.getCoordinate();

    double distance = Coordinates.getDistanceInNM(stage.getCoordinate(), planeCoordinate);
    if (distance > stage.getMaxDistance() || distance < stage.getMinDistance())
      return eResult.illegalLocation;
    double radial = Coordinates.getBearing(planeCoordinate, stage.getCoordinate());
    if (Headings.isBetween(stage.getFromInboundRadial(), radial, stage.getToInboundRadial()) == false)
      return eResult.illegalLocation;

    return eResult.ok;
  }

  private eResult evaluateCheckPlaneShaStage(IAirplaneWriteSimple plane, CheckPlaneShaStage stage) {
    IShaRO sha = plane.getSha();
    if (stage.getMinHeading() != null) {
      int minHeading = stage.getMinHeading();
      int maxHeading = stage.getMaxHeading();
      if (Headings.isBetween(minHeading, sha.getHeading(), maxHeading) == false)
        return eResult.illegalHeading;
    }
    if (stage.getMinAltitude() != null
        && stage.getMinAltitude() > sha.getAltitude())
      return eResult.altitudeTooLow;
    if (stage.getMaxAltitude() != null
        && stage.getMaxAltitude() < sha.getAltitude())
      return eResult.altitudeTooHigh;
    if (stage.getMinSpeed() != null
        && stage.getMinSpeed() > sha.getSpeed())
      return eResult.speedTooLow;
    if (stage.getMaxSpeed() != null
        && stage.getMaxSpeed() < sha.getSpeed())
      return eResult.speedTooHigh;
    return eResult.ok;
  }
}
