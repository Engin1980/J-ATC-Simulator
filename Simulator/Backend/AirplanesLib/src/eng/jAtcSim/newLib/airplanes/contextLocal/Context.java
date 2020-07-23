package eng.jAtcSim.newLib.airplanes.contextLocal;

import eng.jAtcSim.newLib.airplanes.context.IAirplaneAcc;
import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.messaging.context.IMessagingContext;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static IAirplaneAcc getAirplane() {
    return ContextManager.getContext(IAirplaneAcc.class);
  }

  public static IAreaAcc getArea() {
    return ContextManager.getContext(IAreaAcc.class);
  }

  public static ISharedContext getShared() {
    return ContextManager.getContext(ISharedContext.class);
  }

  public static IWeatherContext getWeather() {
    return ContextManager.getContext(IWeatherContext.class);
  }

  public static IAppContext getApp(){
    return ContextManager.getContext(IAppContext.class);
  }

  public static IMessagingContext getMessaging(){return ContextManager.getContext(IMessagingContext.class);}
}
