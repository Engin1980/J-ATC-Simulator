package eng.jAtcSim.newLib.airplanes.contextLocal;

import eng.jAtcSim.newLib.airplanes.context.IAirplaneContext;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;
import eng.jAtcSim.newLib.weather.context.IWeatherContext;

public class Context {
  public static IAirplaneContext getAirplane() {
    return ContextManager.getContext(IAirplaneContext.class);
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
}
