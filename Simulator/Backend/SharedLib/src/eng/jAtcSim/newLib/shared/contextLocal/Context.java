package eng.jAtcSim.newLib.shared.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppContext;
import eng.jAtcSim.newLib.shared.context.ISharedContext;

public class Context {
  public static ISharedContext getShared(){
    return ContextManager.getContext(ISharedContext.class);
  }
  public static IAppContext getApp(){
    return ContextManager.getContext(IAppContext.class);
  }
}
