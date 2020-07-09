package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterRadialCommand;

public class AfterRadialCommandXmlLoader implements IXmlLoader<AfterRadialCommand> {
  @Override
  public AfterRadialCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);
    String navaidName = SmartXmlLoaderUtils.loadString("fix");
    int radial = SmartXmlLoaderUtils.loadInteger(element, "radial");
    AboveBelowExactly abe = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterRadialCommand ret = AfterRadialCommand.create(navaidName, radial, abe);
    return ret;
  }
}
