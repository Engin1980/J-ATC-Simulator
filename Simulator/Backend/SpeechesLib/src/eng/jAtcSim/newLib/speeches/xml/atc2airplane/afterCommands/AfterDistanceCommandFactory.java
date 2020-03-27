package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

public class AfterDistanceCommandFactory {

  public static AfterDistanceCommand load(XElement element) {
    String navaidName = Shared.loadNavaidName(element);
    double distance = XmlLoader.loadDouble(element, "distance");
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
