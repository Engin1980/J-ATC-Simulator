package eng.jAtcSim.newLib.xml.area.contextLocal;

import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.shared.ContextManager;

public class Context {
  public static IAreaAcc getArea(){
    return ContextManager.getContext(IAreaAcc.class);
  }
}
