package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterRadialCommand;

public class AfterRadialCommandXmlLoader implements IXmlLoader<AfterRadialCommand> {
  @Override
  public AfterRadialCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    int radial = XmlLoaderUtils.loadInteger(element, "radial");
    AfterValuePosition extension = Shared.loadAfterValuePosition(element);
    AfterRadialCommand ret = AfterRadialCommand.create(navaidName, radial, extension);
    return ret;
  }
}
