package eng.jAtcSim.newPacks;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newPacks.views.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ViewFactory {
  private static final IMap<String, Producer<IView>> viewMap;

  static {
    viewMap = new EMap<>();
    viewMap.set("flightListView", () -> new FlightListView());
    viewMap.set("radarView+", () -> {
      RadarView ret = new RadarView();
      ret.getBehaviorSettings().setPaintMessages(true);
      return ret;
    });
    viewMap.set("radarView", () -> {
      RadarView ret = new RadarView();
      ret.getBehaviorSettings().setPaintMessages(false);
      return ret;
    });
    viewMap.set("textInputView", () -> new TextInputView());
    viewMap.set("scheduledListView", () -> new ScheduledListView());
    viewMap.set("smallStatsView", () -> new SmallStatsView());
  }

  public static IView getView(String viewName) {
    if (viewMap.containsKey(viewName) == false)
      throw new EApplicationException("Unknown view name: " + viewName);
    IView ret;
    try {
      ret = viewMap.get(viewName).invoke();
    } catch (Exception e) {
      throw new EApplicationException(sf("Failed to instantiate view '%s' : %s.", viewName, e.getMessage()), e);
    }

    return ret;
  }
}
