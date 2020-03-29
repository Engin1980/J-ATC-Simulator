package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterDistanceCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {

  @Override
  public AfterDistanceCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    double distance = XmlLoaderUtils.loadDouble(element, "distance");
    AfterValuePosition extension = Shared.loadAfterValuePosition(element);
    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, extension);
    return ret;
  }

//  public AfterDistanceCommandFactory read(XElement element, Airport parent) {
//    super.read(element, parent);
//    this.distance = XmlLoader.loadInteger(element, "distance");
//    this.extension = XmlLoader.loadEnum(element, "extension", AfterValuePosition.class, AfterValuePosition.exactly);
//  }
}
