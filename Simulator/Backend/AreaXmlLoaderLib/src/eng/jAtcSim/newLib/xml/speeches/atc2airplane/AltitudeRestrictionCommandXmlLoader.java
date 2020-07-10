package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.AltitudeRestrictionCommand;

public class AltitudeRestrictionCommandXmlLoader implements IXmlLoader<AltitudeRestrictionCommand> {
  @Override
  public AltitudeRestrictionCommand load(XElement element) {
    assert element.getName().equals("altitudeRouteRestriction") ||
        element.getName().equals("altitudeRouteRestrictionClear");
    AltitudeRestrictionCommand ret;

    SmartXmlLoaderUtils.setContext(element);

    if (element.getName().equals("altitudeRouteRestrictionClear"))
      ret = AltitudeRestrictionCommand.createClearRestriction();
    else {
      AboveBelowExactly restriction = SmartXmlLoaderUtils.loadEnum("restriction", AboveBelowExactly.class);
      int value = SmartXmlLoaderUtils.loadInteger("value");
      ret = AltitudeRestrictionCommand.create(restriction, value);
    }
    return ret;
  }
}
