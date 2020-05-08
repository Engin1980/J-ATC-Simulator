package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterAltitudeCommand;

public class AfterAltitudeCommandXmlLoader implements IXmlLoader<AfterAltitudeCommand> {
  @Override
  public AfterAltitudeCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterAltitudeCommand ret = AfterAltitudeCommand.create(
        XmlLoaderUtils.loadInteger("value"),
        XmlLoaderUtils.loadEnum("extension", AboveBelowExactly.class, AboveBelowExactly.exactly)
    );
    return ret;
  }
}
