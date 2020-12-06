package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class FlyIafRouteBehavior extends FlyRouteBehavior {
  public FlyIafRouteBehavior(IafRoute iafRoute) {
    super(iafRoute.getRouteCommands().toList());
  }
}
