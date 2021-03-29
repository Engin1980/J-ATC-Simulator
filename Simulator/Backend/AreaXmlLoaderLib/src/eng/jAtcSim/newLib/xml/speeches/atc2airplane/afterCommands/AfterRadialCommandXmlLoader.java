package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterRadialCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class AfterRadialCommandXmlLoader extends XmlLoader<AfterRadialCommand> {

  public AfterRadialCommandXmlLoader(LoadingContext data) {
    super(data);
  }

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
