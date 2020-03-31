package eng.jAtcSim.newLib.area.approaches;

import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.area.routes.Route;

public class ApproachFactory {
  public static class Entry{
    public static class Location{
      public static ILocation createApproachEntryLocationFoRoute(Route route) {
        FixRelatedLocation ret = new FixRelatedLocation(
            coordinate, fromRadial, toRadial, maximalDistance
        );
      }

      private static double getOptimalEntryHeadingForRoute(Route route){

      }
    }
  }
}
