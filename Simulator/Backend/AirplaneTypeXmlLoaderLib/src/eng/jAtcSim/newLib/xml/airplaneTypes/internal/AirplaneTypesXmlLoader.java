package eng.jAtcSim.newLib.xml.airplaneTypes.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AirplaneTypesXmlLoader {
  public AirplaneTypes load(XElement source) {
    IList<AirplaneType> tmp = new EList<>();

    analyseXElement(source, tmp);

    AirplaneTypes ret = AirplaneTypes.create(tmp);
    return ret;
  }

  private void analyseXElement(XElement source, IList<AirplaneType> types) {
    source.getChildren("type").forEach(q -> types.add(loadAirplaneType(q)));
    source.getChildren("group").forEach(q -> analyseXElement(q, types));
  }

  private AirplaneType loadAirplaneType(XElement source) {
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    String fullName = XmlLoaderUtils.loadString("fullName", null);
    char category = XmlLoaderUtils.loadStringRestricted("category", new String[]{"A", "B", "C", "D"}).charAt(0);
    int maxAltitude = XmlLoaderUtils.loadInteger("maxAltitude");
    int vMinApp = XmlLoaderUtils.loadInteger("vMinApp");
    int vApp = XmlLoaderUtils.loadInteger("vApp");
    int vMaxApp = XmlLoaderUtils.loadInteger("vMaxApp");
    int vR = XmlLoaderUtils.loadInteger("vR");
    int vDep = XmlLoaderUtils.loadInteger("vDep");
    int vMinClean = XmlLoaderUtils.loadInteger("vMinClean");
    int vMaxClean = XmlLoaderUtils.loadInteger("vMaxClean");
    int vCruise = XmlLoaderUtils.loadInteger("vCruise");
    int lowClimbRate = XmlLoaderUtils.loadInteger("lowClimbRate");
    int highClimbRate = XmlLoaderUtils.loadInteger("highClimbRate");
    int lowDescendRate = XmlLoaderUtils.loadInteger("lowDescendRate");
    int highDescendRate = XmlLoaderUtils.loadInteger("highDescendRate");
    int speedIncreaseRate = XmlLoaderUtils.loadInteger("speedIncreaseRate");
    int speedDecreaseRate = XmlLoaderUtils.loadInteger("speedDecreaseRate");
    int headingChangeRate = XmlLoaderUtils.loadInteger("headingChangeRate");

    AirplaneType ret = new AirplaneType(name, fullName, category, maxAltitude,
        vR, vMinApp, vMaxApp, vApp,
        vMinClean, vMaxClean, vCruise, vDep,
        lowClimbRate, highClimbRate, highDescendRate, lowDescendRate,
        speedIncreaseRate, speedDecreaseRate, headingChangeRate);
    return ret;
  }
}
