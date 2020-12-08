package eng.jAtcSim.newLib.area.approaches.behaviors;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlyIafRouteBehavior extends FlyRouteBehavior {

  @XmlConstructor
  private FlyIafRouteBehavior(){
    super();
  }

  public FlyIafRouteBehavior(IafRoute iafRoute) {
    super(iafRoute.getRouteCommands().toList());
  }
}
