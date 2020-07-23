package eng.jAtcSim.newLib.gameSim.contextLocal;

import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.atcs.context.IAtcAcc;
import eng.jAtcSim.newLib.messaging.context.IMessagingAcc;
import eng.jAtcSim.newLib.mood.context.IMoodAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.stats.context.IStatsAcc;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;

public class Context {

  public static IAirplaneAcc getAirplane() {
    return ContextManager.getContext(IAirplaneAcc.class);
  }

  public static IAirplaneTypeAcc getAirplaneType() {
    return ContextManager.getContext(IAirplaneTypeAcc.class);
  }

  public static IAppAcc getApp() {
    return ContextManager.getContext(IAppAcc.class);
  }

  public static IAreaAcc getArea() {
    return ContextManager.getContext(IAreaAcc.class);
  }

  public static IAtcAcc getAtc() {
    return ContextManager.getContext(IAtcAcc.class);
  }

  public static IMessagingAcc getMessaging() {
    return ContextManager.getContext(IMessagingAcc.class);
  }

  public static IMoodAcc getMood() {
    return ContextManager.getContext(IMoodAcc.class);
  }

  public static ISharedAcc getShared() {
    return ContextManager.getContext(ISharedAcc.class);
  }

  public static IStatsAcc getStats() {
    return ContextManager.getContext(IStatsAcc.class);
  }

  public static IWeatherAcc getWeather() {
    return ContextManager.getContext(IWeatherAcc.class);
  }
}
