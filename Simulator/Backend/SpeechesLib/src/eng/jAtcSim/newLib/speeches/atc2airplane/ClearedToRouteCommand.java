package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.ICommand;

public class ClearedToRouteCommand implements ICommand {

  private String routeName;
  private DARouteType routeType;
  private String expectedRunwayThresholdName;

  public ClearedToRouteCommand(String routeName, DARouteType routeType, String expectedRunwayThresholdName) {
    EAssert.Argument.isNonEmptyString(routeName);
    EAssert.Argument.isNonEmptyString(expectedRunwayThresholdName);
    this.routeName = routeName;
    this.routeType = routeType;
    this.expectedRunwayThresholdName = expectedRunwayThresholdName;
  }

  public String getRouteName() {
    return routeName;
  }

  public DARouteType getRouteType() {
    return routeType;
  }

  public String getExpectedRunwayThresholdName() {
    return expectedRunwayThresholdName;
  }

  @Override
  public String toString() {
    return "Clear to route(" + routeName + "/" + expectedRunwayThresholdName + ") {command}";
  }
}
