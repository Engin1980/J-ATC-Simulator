package eng.jAtcSim.newLib.weather.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static IAppContext getApp() {
    return ContextManager.getContext(IAppContext.class);
  }

  public static ISharedContext getShared() {
    return ContextManager.getContext(ISharedContext.class);
  }

  public static IWeatherContext getWeather() {
    return ContextManager.getContext(IWeatherContext.class);
  }
}
