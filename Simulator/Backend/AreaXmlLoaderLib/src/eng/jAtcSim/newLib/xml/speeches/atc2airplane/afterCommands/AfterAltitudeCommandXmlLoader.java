package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;

public class AfterAltitudeCommandXmlLoader implements IXmlLoader<AfterAltitudeCommand> {
  @Override
  public AfterAltitudeCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);
    int value = SmartXmlLoaderUtils.loadInteger("value");
    AboveBelowExactly extension = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterAltitudeCommand ret = AfterAltitudeCommand.create(value, extension);
    return ret;
  }
}
