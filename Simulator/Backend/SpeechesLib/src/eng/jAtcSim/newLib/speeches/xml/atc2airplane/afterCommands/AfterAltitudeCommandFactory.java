package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterAltitudeCommandFactory {
  public static AfterAltitudeCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterAltitudeCommand ret = AfterAltitudeCommand.create(
        XmlLoaderUtils.loadInteger("value"),
        XmlLoaderUtils.loadEnum("extension", AfterValuePosition.class, AfterValuePosition.exactly)
    );
    return ret;
  }
}
