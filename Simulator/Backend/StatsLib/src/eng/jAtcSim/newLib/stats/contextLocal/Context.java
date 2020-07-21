package eng.jAtcSim.newLib.stats.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedContext;

public class Context {
  public static ISharedContext getShared(){
    return ContextManager.getContext(ISharedContext.class);
  }
}
