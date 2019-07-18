package eng.jAtcSim.lib.world.newApproaches.stages.checks;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.newApproaches.stages.CheckStage;

public class CheckAirportVisibilityStage implements CheckStage {
//  @Override
//  protected eResult check(IPilot5Behavior pilot) {
//    Weather w = Acc.weather();
//    if ((w.getCloudBaseInFt() + Acc.airport().getAltitude()) < pilot.getAltitude()) {
//      return eResult.runwayNotInSight;
//    }
//    double d = Coordinates.getDistanceInNM(pilot.getCoordinate(), pilot.getAssignedApproach().getParent().getCoordinate());
//    if (w.getVisibilityInMilesReal() < d) {
//      return eResult.runwayNotInSight;
//    }
//    return eResult.ok;
//  }
}
