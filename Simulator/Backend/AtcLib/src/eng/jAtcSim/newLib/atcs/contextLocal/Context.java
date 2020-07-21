package eng.jAtcSim.newLib.atcs.contextLocal;

import eng.jAtcSim.newLib.airplanes.context.IAirplaneContext;
import eng.jAtcSim.newLib.atcs.context.IAtcContext;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.stats.context.IStatsContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static IAirplaneContext getAirplane() {
    return ContextManager.getContext(IAirplaneContext.class);
  }

  public static IAppContext getApp() {
    return ContextManager.getContext(IAppContext.class);
  }

  public static IAtcContext getAtc() {
    return ContextManager.getContext(IAtcContext.class);
  }

  public static ISharedContext getShared() {
    return ContextManager.getContext(ISharedContext.class);
  }

  public static IStatsContext getStats() {
    return ContextManager.getContext(IStatsContext.class);
  }

  public static IWeatherContext getWeather() {
    return ContextManager.getContext(IWeatherContext.class);
  }
}
