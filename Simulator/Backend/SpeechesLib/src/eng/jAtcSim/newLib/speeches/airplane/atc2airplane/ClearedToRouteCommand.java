package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class ClearedToRouteCommand implements ICommand {

  private final String routeName;
  private final DARouteType routeType;
  private final String expectedRunwayThresholdName;

  public static ClearedToRouteCommand create(String routeName, DARouteType routeType, String expectedRunwayThresholdName) {
    return new ClearedToRouteCommand(routeName, routeType, expectedRunwayThresholdName);
  }

  private ClearedToRouteCommand(String routeName, DARouteType routeType, String expectedRunwayThresholdName) {
    EAssert.Argument.isNonemptyString(routeName);
    EAssert.Argument.isNonemptyString(expectedRunwayThresholdName);
    this.routeName = routeName;
    this.expectedRunwayThresholdName = expectedRunwayThresholdName;
  }

  public DARouteType getRouteType() {
    return routeType;
  }

  public String getRouteName() {
    return routeName;
  }

  public String getExpectedRunwayThresholdName() {
    return expectedRunwayThresholdName;
  }

  @Override
  public String toString() {
    return "Clear to route(" + routeName + "/" + expectedRunwayThresholdName + ") {command}";
  }
}
