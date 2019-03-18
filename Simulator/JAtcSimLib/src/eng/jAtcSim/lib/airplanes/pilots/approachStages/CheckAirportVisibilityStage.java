package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.weathers.Weather;

public class CheckAirportVisibilityStage extends CheckApproachStage {
  @Override
  protected eCheckResult check(IPilot4Behavior pilot) {
    Weather w = Acc.weather();
    if ((w.getCloudBaseInFt() + Acc.airport().getAltitude()) < pilot.getAltitude()) {
      return eCheckResult.runwayNotInSight;
    }
    double d = Coordinates.getDistanceInNM(pilot.getCoordinate(), pilot.getAssignedApproach().getParent().getCoordinate());
    if (w.getVisibilityInMilesReal() < d) {
      return eCheckResult.runwayNotInSight;
    }
    return eCheckResult.ok;
  }
}