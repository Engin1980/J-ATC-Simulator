//package eng.jAtcSim.newLib.area.approaches;
//
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.eSystem.geo.Headings;
//import eng.eSystem.validation.EAssert;
//import eng.jAtcSim.newLib.area.Navaid;
//import eng.jAtcSim.newLib.area.NavaidList;
//import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
//import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
//import eng.jAtcSim.newLib.area.routes.IafRoute;
//import eng.jAtcSim.newLib.speeches.airplane.ICommand;
//import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
//import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
//
//import static eng.eSystem.utilites.FunctionShortcuts.sf;
//
//public class ApproachFactory {
//  public static class Entry {
//    public static class Location {
//      public static ILocation createApproachEntryLocationBetweenMaptAndFaf(String maptName, String fafName, NavaidList navaids) {
//        Navaid faf = navaids.getWithPBD(fafName);
//        Navaid mapt = navaids.getWithPBD(maptName);
//        double distance = Coordinates.getDistanceInNM(faf.getCoordinate(), mapt.getCoordinate());
//        double radial = Coordinates.getBearing(faf.getCoordinate(), mapt.getCoordinate());
//        ILocation ret = new FixRelatedLocation(mapt.getCoordinate(),
//            (int) Headings.add(radial, -30),
//            (int) Headings.add(radial, +30),
//            distance + 1);
//        return ret;
//      }
//
//      public static ILocation createApproachEntryLocationForRoute(IafRoute route, NavaidList navaids) {
//        double expHeading = getOptimalEntryHeadingForRoute(route, navaids);
//        int fromRadial = (int) Headings.add(expHeading, 115);
//        int toRadial = (int) Headings.add(expHeading, 115);
//        Coordinate coordinate;
//        {
//          String navaidName = ((ProceedDirectCommand) route.getRouteCommands().get(0)).getNavaidName();
//          coordinate = navaids.get(navaidName).getCoordinate();
//        }
//        double maximalDistance = 10;
//        FixRelatedLocation ret = new FixRelatedLocation(
//            coordinate, fromRadial, toRadial, maximalDistance
//        );
//        return ret;
//      }
//
//      public static ILocation createApproachEntryLocationForFAF(
//          Coordinate coordinate, int coordinateAltitude, int radial, int altitude, double slope) {
//        double dist = (altitude - coordinateAltitude) / slope;
//        Coordinate fafCoordinate = Coordinates.getCoordinate(
//            coordinate, Headings.getOpposite(radial), dist);
//        ILocation ret = new FixRelatedLocation(fafCoordinate,
//            (int) Headings.add(radial, -45),
//            (int) Headings.add(radial, 45),
//            10);
//        return ret;
//      }
//
//      public static ILocation createApproachEntryLocationForFAF(String fafName, int course, NavaidList navaids) {
//        Navaid navaid = navaids.getWithPBD(fafName);
//        ILocation ret = new FixRelatedLocation(navaid.getCoordinate(),
//            (int) Headings.add(course, -45),
//            (int) Headings.add(course, 45),
//            20);
//        return ret;
//      }
//
//      private static double getOptimalEntryHeadingForRoute(IafRoute route, NavaidList navaids) {
//        EAssert.Argument.isNotNull(route);
//        EAssert.Argument.isTrue(route.getRouteCommands().isEmpty() == false);
//        EAssert.Argument.isTrue(route.getRouteCommands().get(0) instanceof ProceedDirectCommand);
//        ProceedDirectCommand first = (ProceedDirectCommand) route.getRouteCommands().get(0);
//        ICommand second = route.getRouteCommands().tryGetFirst(
//            q -> q instanceof ProceedDirectCommand || q instanceof ChangeHeadingCommand);
//        EAssert.isNotNull(
//            second,
//            sf("Iaf-Route %s does not contain change-heading or proceed-direct on second or later index in commands.",
//                route.getName()));
//        double ret;
//        if (second instanceof ProceedDirectCommand) {
//          Navaid a = navaids.get(first.getNavaidName());
//          Navaid b = navaids.get(((ProceedDirectCommand) second).getNavaidName());
//          ret = Coordinates.getBearing(a.getCoordinate(), b.getCoordinate());
//        } else if (second instanceof ChangeHeadingCommand) {
//          ret = ((ChangeHeadingCommand) second).getHeading();
//        } else throw new UnsupportedOperationException();
//        return ret;
//      }
//    }
//  }
//}
