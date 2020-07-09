package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Atc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

public class AtcXmlLoader implements IXmlLoader<Atc> {
  @Override
  public Atc load(XElement source) {
    log(2, "Xml-loading atc");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    log(2, "... atc '%s'", name);
    AtcType type = SmartXmlLoaderUtils.loadEnum("type", AtcType.class);
    double frequency = SmartXmlLoaderUtils.loadDouble("frequency");
    int acceptAltitude = SmartXmlLoaderUtils.loadAltitude("acceptAltitude");
    int releaseAltitude = SmartXmlLoaderUtils.loadAltitude("releaseAltitude");
    int orderedAltitude = SmartXmlLoaderUtils.loadAltitude("orderedAltitude");
    Integer ctrAcceptDistance = SmartXmlLoaderUtils.loadInteger("ctrAcceptDistance", null);
    Integer ctrNavaidAcceptDistance = SmartXmlLoaderUtils.loadInteger("ctrNavaidAcceptDistance", null);

    Atc ret = Atc.create(name, type, frequency,
        acceptAltitude, releaseAltitude, orderedAltitude,
        ctrAcceptDistance, ctrNavaidAcceptDistance);

    return ret;
  }
}
