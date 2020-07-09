package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;

public class AfterDistanceCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {

  @Override
  public AfterDistanceCommand load(XElement element) {

    SmartXmlLoaderUtils.setContext(element);
    String navaidName = SmartXmlLoaderUtils.loadString("fix");
    double distance = SmartXmlLoaderUtils.loadDouble("distance");
    AboveBelowExactly extension = SmartXmlLoaderUtils.loadAboveBelowExactly("extension", AboveBelowExactly.exactly);
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, extension);
    return ret;
  }
}
