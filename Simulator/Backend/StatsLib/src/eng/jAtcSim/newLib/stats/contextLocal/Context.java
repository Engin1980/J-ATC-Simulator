package eng.jAtcSim.newLib.stats.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;

public class Context {
  public static ISharedAcc getShared(){
    return ContextManager.getContext(ISharedAcc.class);
  }
}
