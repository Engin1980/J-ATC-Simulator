package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class Gimme {
  public static DARoute tryGetDARoute(String routeName) {
    throw new ToDoException();
  }

  public static Navaid tryGetNavaid(String navaidName) {
    throw new ToDoException();
  }

  public static PublishedHold tryGetPublishedHold(String navaidName) {
    throw new ToDoException();
  }

  public static ActiveRunwayThreshold tryGetRunwayThreshold(String runwayThresholdName) {
    throw new ToDoException();
  }
}
