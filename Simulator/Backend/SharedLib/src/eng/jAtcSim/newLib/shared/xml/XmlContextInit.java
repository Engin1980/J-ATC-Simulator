package eng.jAtcSim.newLib.shared.xml;

import eng.jAtcSim.newLib.shared.contextLocal.Context;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.newXmlUtils.XmlContext;

public class XmlContextInit {

  public static boolean checkCanBeInitialized(XmlContext ctx, String moduleKey){
    final String key = "__" + moduleKey + " XmlContext init";
    if (ctx.values.containsKey(key)) {
      Context.getApp().getAppLog().write(ApplicationLog.eType.warning, key + " already set.");
      return false;
    }
    ctx.values.set(key, null);
    return true;
  }
}
