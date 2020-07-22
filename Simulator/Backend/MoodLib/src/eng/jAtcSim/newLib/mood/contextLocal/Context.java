package eng.jAtcSim.newLib.mood.contextLocal;

import eng.jAtcSim.newLib.mood.context.IMoodContext;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedContext;

public class Context {
  public static IMoodContext getMood(){
    return ContextManager.getContext(IMoodContext.class);
  }
  public static ISharedContext getShared(){
    return ContextManager.getContext(ISharedContext.class);
  }
}
