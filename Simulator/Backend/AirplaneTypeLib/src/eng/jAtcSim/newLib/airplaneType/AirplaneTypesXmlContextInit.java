package eng.jAtcSim.newLib.airplaneType;

import eng.jAtcSim.newLib.airplaneType.context.IAirplaneTypeAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;

public class AirplaneTypesXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "airplaneType") == false) return;

    ctx.sdfManager.setFormatter(AirplaneType.class, q -> q.name);
    ctx.sdfManager.setParser(AirplaneType.class, (q, c) -> {
      IAirplaneTypeAcc airplaneTypeAcc = ContextManager.getContext(IAirplaneTypeAcc.class);
      AirplaneType ret = airplaneTypeAcc.getAirplaneTypes().getByName(q);
      return ret;
    });
  }
}
