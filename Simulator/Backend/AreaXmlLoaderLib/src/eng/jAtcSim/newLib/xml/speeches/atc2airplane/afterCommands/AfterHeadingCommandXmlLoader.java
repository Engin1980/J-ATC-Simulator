package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class AfterHeadingCommandXmlLoader extends XmlLoader<AfterHeadingCommand> {

  public AfterHeadingCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public AfterHeadingCommand load(XElement element) {
    SmartXmlLoaderUtils.setContext(element);
    int value = SmartXmlLoaderUtils.loadInteger("value");
    AboveBelowExactly extension = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterHeadingCommand ret = AfterHeadingCommand.create(value, extension);
    return ret;
  }
}
