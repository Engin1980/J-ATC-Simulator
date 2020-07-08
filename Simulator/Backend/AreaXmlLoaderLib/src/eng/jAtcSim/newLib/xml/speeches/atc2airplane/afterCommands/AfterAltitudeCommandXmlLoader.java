package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterAltitudeCommand;

public class AfterAltitudeCommandXmlLoader implements IXmlLoader<AfterAltitudeCommand> {
  @Override
  public AfterAltitudeCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);

    String arg = element.getAttribute("value");
    SmartXmlLoaderUtils.ValueAndABE tmp = SmartXmlLoaderUtils.loadValueAndAboveBelowExactly(arg);

    AfterAltitudeCommand ret = AfterAltitudeCommand.create(
        tmp.getValue(),
        tmp.getAbe());
    return ret;
  }
}
