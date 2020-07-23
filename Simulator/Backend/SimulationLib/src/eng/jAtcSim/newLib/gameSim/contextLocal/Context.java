package eng.jAtcSim.newLib.gameSim.contextLocal;

import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcContext;
import eng.jAtcSim.newLib.messaging.context.IMessagingContext;
import eng.jAtcSim.newLib.mood.context.IMoodContext;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.stats.context.IStatsContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static IAirplaneAcc getAirplane() {
    return ContextManager.getContext(IAirplaneAcc.class);
  }

  public static IAppContext getApp() {
    return ContextManager.getContext(IAppContext.class);
  }

  public static IAtcContext getAtc() {
    return ContextManager.getContext(IAtcContext.class);
  }

  public static IMessagingContext getMessaging() {
    return ContextManager.getContext(IMessagingContext.class);
  }

  public static IMoodContext getMood() {
    return ContextManager.getContext(IMoodContext.class);
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
