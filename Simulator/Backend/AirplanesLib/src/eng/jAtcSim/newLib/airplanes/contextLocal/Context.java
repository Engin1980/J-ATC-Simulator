package eng.jAtcSim.newLib.airplanes.contextLocal;

import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.messaging.context.IMessagingAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;

public class Context {
  public static IAirplaneAcc getAirplane() {
    return ContextManager.getContext(IAirplaneAcc.class);
  }

  public static IAppAcc getApp() {
    return ContextManager.getContext(IAppAcc.class);
  }

  public static IAreaAcc getArea() {
    return ContextManager.getContext(IAreaAcc.class);
  }

  public static IMessagingAcc getMessaging() {
    return ContextManager.getContext(IMessagingAcc.class);
  }

  public static ISharedAcc getShared() {
    return ContextManager.getContext(ISharedAcc.class);
  }

  public static IWeatherAcc getWeather() {
    return ContextManager.getContext(IWeatherAcc.class);
  }
}
