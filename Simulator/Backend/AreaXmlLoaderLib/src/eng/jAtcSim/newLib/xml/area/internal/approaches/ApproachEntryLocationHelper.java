//package eng.jAtcSim.newLib.xml.area.internal.approaches;
//
//import eng.eSystem.collections.*;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.eSystem.geo.Headings;
//import eng.eSystem.validation.EAssert;
//import eng.jAtcSim.newLib.area.Navaid;
//import eng.jAtcSim.newLib.area.NavaidList;
//import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
//import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
//import eng.jAtcSim.newLib.area.routes.IafRoute;
//import eng.jAtcSim.newLib.speeches.ICommand;
//import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;
//import eng.jAtcSim.newLib.speeches.atc2airplane.ProceedDirectCommand;
//import eng.jAtcSim.newLib.xml.area.internal.context.Context;
//
//import static eng.eSystem.utilites.FunctionShortcuts.*;
//
//public class ApproachEntryLocationHelper {
//  public static ILocation createApproachEntryLocationForRoute(IafRoute route, NavaidList navaids) {
//    double expHeading = getOptimalEntryHeadingForRoute(route, navaids);
//    int fromRadial = (int) Headings.add(expHeading, 115);
//    int toRadial = (int) Headings.add(expHeading, 115);
//    Coordinate coordinate;
//    {
//      String navaidName = ((ProceedDirectCommand) route.getRouteCommands().get(0)).getNavaidName();
//      coordinate = navaids.get(navaidName).getCoordinate();
//    }
//    double maximalDistance = 10;
//    FixRelatedLocation ret = new FixRelatedLocation(
//        coordinate, fromRadial, toRadial, maximalDistance
//    );
//    return ret;
//  }
//
//  private static double getOptimalEntryHeadingForRoute(IafRoute route, Context context) {
//    EAssert.Argument.isNotNull(route);
//    EAssert.Argument.isTrue(route.getRouteCommands().isEmpty() == false);
//    EAssert.Argument.isTrue(route.getRouteCommands().get(0) instanceof ProceedDirectCommand);
//    ProceedDirectCommand first = (ProceedDirectCommand) route.getRouteCommands().get(0);
//    ICommand second = route.getRouteCommands().tryGetFirst(
//        q -> q instanceof ProceedDirectCommand || q instanceof ChangeHeadingCommand);
//    EAssert.isNotNull(
//        second,
//        sf("Iaf-Route does not contain change-heading or proceed-direct on second or later index in commands. Airport: %s, threshold %s",
//            context.airport.icao,
//            context.threshold.name));
//    double ret;
//    if (second instanceof ProceedDirectCommand) {
//      Navaid a = context.area.navaids.get(first.getNavaidName());
//      Navaid b = context.area.navaids.get(((ProceedDirectCommand) second).getNavaidName());
//      ret = Coordinates.getBearing(a.getCoordinate(), b.getCoordinate());
//    } else if (second instanceof ChangeHeadingCommand) {
//      ret = ((ChangeHeadingCommand) second).getHeading();
//    } else throw new UnsupportedOperationException();
//    return ret;
//  }
//}
