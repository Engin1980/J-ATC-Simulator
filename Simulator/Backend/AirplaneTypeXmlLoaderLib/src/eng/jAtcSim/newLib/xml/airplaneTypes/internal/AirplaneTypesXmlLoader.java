package eng.jAtcSim.newLib.xml.airplaneTypes.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

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
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    String fullName = SmartXmlLoaderUtils.loadString("fullName", null);
    char category = SmartXmlLoaderUtils.loadStringRestricted("category", new String[]{"A", "B", "C", "D"}).charAt(0);
    int maxAltitude = SmartXmlLoaderUtils.loadInteger("maxAltitude");
    int vMinApp = SmartXmlLoaderUtils.loadInteger("vMinApp");
    int vApp = SmartXmlLoaderUtils.loadInteger("vApp");
    int vMaxApp = SmartXmlLoaderUtils.loadInteger("vMaxApp");
    int vR = SmartXmlLoaderUtils.loadInteger("vR");
    int vDep = SmartXmlLoaderUtils.loadInteger("vDep");
    int vMinClean = SmartXmlLoaderUtils.loadInteger("vMinClean");
    int vMaxClean = SmartXmlLoaderUtils.loadInteger("vMaxClean");
    int vCruise = SmartXmlLoaderUtils.loadInteger("vCruise");
    int lowClimbRate = SmartXmlLoaderUtils.loadInteger("lowClimbRate");
    int highClimbRate = SmartXmlLoaderUtils.loadInteger("highClimbRate");
    int lowDescendRate = SmartXmlLoaderUtils.loadInteger("lowDescendRate");
    int highDescendRate = SmartXmlLoaderUtils.loadInteger("highDescendRate");
    int speedIncreaseRate = SmartXmlLoaderUtils.loadInteger("speedIncreaseRate");
    int speedDecreaseRate = SmartXmlLoaderUtils.loadInteger("speedDecreaseRate");
    int headingChangeRate = SmartXmlLoaderUtils.loadInteger("headingChangeRate");

    AirplaneType ret = new AirplaneType(name, fullName, category, maxAltitude,
        vR, vMinApp, vMaxApp, vApp,
        vMinClean, vMaxClean, vCruise, vDep,
        lowClimbRate, highClimbRate, highDescendRate, lowDescendRate,
        speedIncreaseRate, speedDecreaseRate, headingChangeRate);
    return ret;
  }
}
