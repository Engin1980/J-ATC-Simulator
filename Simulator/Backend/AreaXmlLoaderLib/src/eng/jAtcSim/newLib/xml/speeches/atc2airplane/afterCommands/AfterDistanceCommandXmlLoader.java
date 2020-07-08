package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;

public class AfterDistanceCommandXmlLoader implements IXmlLoader<AfterDistanceCommand> {

  @Override
  public AfterDistanceCommand load(XElement element) {
    EAssert.isTrue("distance".equals(element.getAttribute("property")));

    String val = element.getAttribute("value");
    String [] pts = RegexUtils.extractGroups(val, "([A-Z]+)/(\\d+(\\.\\d+)?)([\\+\\-])?");

    String navaidName = pts[1];
    double distance = Double.parseDouble(pts[2]);
    AboveBelowExactly extension = AboveBelowExactly.exactly;
    if (pts.length == 4)
      if (pts[3].equals("-"))
        extension = AboveBelowExactly.below;
      else if (pts[3].equals("+"))
        extension = AboveBelowExactly.above;

    AfterDistanceCommand ret = AfterDistanceCommand.create(navaidName, distance, extension);
    return ret;
  }

//  public AfterDistanceCommandFactory read(XElement element, Airport parent) {
//    super.read(element, parent);
//    this.distance = XmlLoader.loadInteger(element, "distance");
//    this.extension = XmlLoader.loadEnum(element, "extension", AfterValuePosition.class, AfterValuePosition.exactly);
//  }
}
