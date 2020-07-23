package eng.jAtcSim.newLib.mood.contextLocal;

import eng.jAtcSim.newLib.mood.context.IMoodAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;

public class Context {
  public static IMoodAcc getMood(){
    return ContextManager.getContext(IMoodAcc.class);
  }
  public static ISharedAcc getShared(){
    return ContextManager.getContext(ISharedAcc.class);
  }
}
