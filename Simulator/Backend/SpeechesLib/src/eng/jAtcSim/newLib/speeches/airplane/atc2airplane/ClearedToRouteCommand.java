package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

import exml.annotations.XConstructor;

public class ClearedToRouteCommand implements ICommand {

  public static ClearedToRouteCommand create(String routeName, DARouteType routeType, String expectedRunwayThresholdName) {
    return new ClearedToRouteCommand(routeName, routeType, expectedRunwayThresholdName);
  }

  private final String routeName;
  private final DARouteType routeType;
  private final String expectedRunwayThresholdName;

  private ClearedToRouteCommand(String routeName, DARouteType routeType, String expectedRunwayThresholdName) {
    EAssert.Argument.isNonemptyString(routeName);
    EAssert.Argument.isNonemptyString(expectedRunwayThresholdName);
    this.routeName = routeName;
    this.expectedRunwayThresholdName = expectedRunwayThresholdName;
    this.routeType = routeType;
  }

  @XConstructor

  private ClearedToRouteCommand() {
    routeName = null;
    routeType = null;
    expectedRunwayThresholdName = null;
  }

  public String getExpectedRunwayThresholdName() {
    return expectedRunwayThresholdName;
  }

  public String getRouteName() {
    return routeName;
  }

  public DARouteType getRouteType() {
    return routeType;
  }

  @Override
  public String toString() {
    return "Clear to route(" + routeName + "/" + expectedRunwayThresholdName + ") {command}";
  }
}
