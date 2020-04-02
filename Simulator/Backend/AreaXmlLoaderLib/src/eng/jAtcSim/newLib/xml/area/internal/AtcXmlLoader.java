package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Atc;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AtcXmlLoader implements IXmlLoader<Atc> {
  @Override
  public Atc load(XElement source) {
    XmlLoaderUtils.setContext(source);
    Atc.eType type = XmlLoaderUtils.loadEnum("type", Atc.eType.class);
    String name = XmlLoaderUtils.loadString("name");
    double frequency = XmlLoaderUtils.loadDouble("frequency");
    int acceptAltitude = XmlLoaderUtils.loadInteger("acceptAltitude");
    int releaseAltitude = XmlLoaderUtils.loadInteger("releaseAltitude");
    int orderedAltitude = XmlLoaderUtils.loadInteger("orderedAltitude");
    Integer ctrAcceptDistance = XmlLoaderUtils.loadInteger("ctrAcceptDistance", null);
    Integer ctrNavaidAcceptDistance = XmlLoaderUtils.loadInteger("ctrNavaidAcceptDistance", null);

    Atc ret = Atc.create(name, type, frequency,
        acceptAltitude, releaseAltitude, orderedAltitude,
        ctrAcceptDistance, ctrNavaidAcceptDistance);

    return ret;
  }
}
