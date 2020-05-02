package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.ICommand;

public class ClearedToRouteCommand implements ICommand {

  private String routeName;
  private String expectedRunwayThresholdName;

  public static ClearedToRouteCommand create(String routeName, String expectedRunwayThresholdName) {
    return new ClearedToRouteCommand(routeName, expectedRunwayThresholdName);
  }

  private ClearedToRouteCommand(String routeName, String expectedRunwayThresholdName) {
    EAssert.Argument.isNonemptyString(routeName);
    EAssert.Argument.isNonemptyString(expectedRunwayThresholdName);
    this.routeName = routeName;
    this.expectedRunwayThresholdName = expectedRunwayThresholdName;
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
