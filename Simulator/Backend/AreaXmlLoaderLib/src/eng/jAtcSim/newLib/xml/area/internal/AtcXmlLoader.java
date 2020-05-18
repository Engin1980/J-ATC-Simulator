package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Atc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

public class AtcXmlLoader implements IXmlLoader<Atc> {
  @Override
  public Atc load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    AtcType type = SmartXmlLoaderUtils.loadEnum("type", AtcType.class);
    String name = SmartXmlLoaderUtils.loadString("name");
    double frequency = SmartXmlLoaderUtils.loadDouble("frequency");
    int acceptAltitude = SmartXmlLoaderUtils.loadInteger("acceptAltitude");
    int releaseAltitude = SmartXmlLoaderUtils.loadInteger("releaseAltitude");
    int orderedAltitude = SmartXmlLoaderUtils.loadInteger("orderedAltitude");
    Integer ctrAcceptDistance = SmartXmlLoaderUtils.loadInteger("ctrAcceptDistance", null);
    Integer ctrNavaidAcceptDistance = SmartXmlLoaderUtils.loadInteger("ctrNavaidAcceptDistance", null);

    Atc ret = Atc.create(name, type, frequency,
        acceptAltitude, releaseAltitude, orderedAltitude,
        ctrAcceptDistance, ctrNavaidAcceptDistance);

    return ret;
  }
}
