package eng.jAtcSim.newLib.xml.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class AreaXmlLoader {
  public static Area load(String fileName){
    Context context = new Context();
    eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader loader =
        new eng.jAtcSim.newLib.xml.area.internal.AreaXmlLoader(context);

    XElement root = ??;

    Area ret = loader.load(root);
    return ret;
  }
}
