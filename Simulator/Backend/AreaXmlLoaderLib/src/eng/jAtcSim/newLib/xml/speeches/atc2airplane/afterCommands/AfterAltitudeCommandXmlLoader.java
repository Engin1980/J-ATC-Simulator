package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterAltitudeCommand;

public class AfterAltitudeCommandXmlLoader implements IXmlLoader<AfterAltitudeCommand> {
  @Override
  public AfterAltitudeCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterAltitudeCommand ret = AfterAltitudeCommand.create(
        XmlLoaderUtils.loadInteger("value"),
        XmlLoaderUtils.loadEnum("extension", AfterValuePosition.class, AfterValuePosition.exactly)
    );
    return ret;
  }
}
