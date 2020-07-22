package eng.jAtcSim.newLib.traffic.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;

public class Context {
  public static IAppContext getApp() {
    return ContextManager.getContext(IAppContext.class);
  }

  public static ISharedContext getShared() {
    return ContextManager.getContext(ISharedContext.class);
  }
}
