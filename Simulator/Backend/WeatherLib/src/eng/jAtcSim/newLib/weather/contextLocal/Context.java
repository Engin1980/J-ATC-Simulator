package eng.jAtcSim.newLib.weather.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.weather.context.IWeatherAcc;

public class Context {
  public static IAppAcc getApp() {
    return ContextManager.getContext(IAppAcc.class);
  }

  public static ISharedAcc getShared() {
    return ContextManager.getContext(ISharedAcc.class);
  }

  public static IWeatherAcc getWeather() {
    return ContextManager.getContext(IWeatherAcc.class);
  }
}
