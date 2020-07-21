package eng.jAtcSim.newLib.textProcessing.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static ISharedContext getShared() {
    return ContextManager.getContext(ISharedContext.class);
  }

  public static IWeatherContext getWeather() {
    return ContextManager.getContext(IWeatherContext.class);
  }
}
