package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterAltitudeCommandFactory {
  public static AfterAltitudeCommand load(XElement element) {
    XmlLoader.setContext(element);

    AfterAltitudeCommand ret = AfterAltitudeCommand.create(
        XmlLoader.loadInteger("value"),
        XmlLoader.loadEnum("extension", AfterValuePosition.class, AfterValuePosition.exactly)
    );
    return ret;
  }
}
