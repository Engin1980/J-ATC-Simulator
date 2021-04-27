package eng.jAtcSim.newPacks;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newPacks.views.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ViewFactory {
  private static final IMap<String, Producer<IView>> viewMap;

  static {
    viewMap = new EMap<>();
    viewMap.set("flightListView", FlightListView::new);
    viewMap.set("radarView", RadarView::new);
    viewMap.set("textInputView", TextInputView::new);
    viewMap.set("scheduledListView", ScheduledListView::new);
    viewMap.set("smallStatsView", SmallStatsView::new);
    viewMap.set("appLogView", AppLogView::new);
  }

  public static IView getView(String viewName) {
    if (viewMap.containsKey(viewName) == false)
      throw new ApplicationException("Unknown view name: " + viewName);
    IView ret;
    try {
      ret = viewMap.get(viewName).invoke();
    } catch (Exception e) {
      throw new ApplicationException(sf("Failed to instantiate view '%s' : %s.", viewName, e.getMessage()), e);
    }

    return ret;
  }
}
