package eng.jAtcSim.newLib.traffic.contextLocal;

import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;

public class Context {
  public static IAppAcc getApp() {
    return ContextManager.getContext(IAppAcc.class);
  }

  public static ISharedAcc getShared() {
    return ContextManager.getContext(ISharedAcc.class);
  }
}
