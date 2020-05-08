package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;

public class ChangeHeadingCommandXmlLoader implements IXmlLoader<ChangeHeadingCommand> {
  @Override
  public ChangeHeadingCommand load(XElement source) {
    assert source.getName().equals("heading");

    XmlLoaderUtils.setContext(source);
    String dirS = XmlLoaderUtils.loadString("direction", "nearest");
    LeftRightAny dir;
    if (dirS.equals("nearest"))
      dir = LeftRightAny.any;
    else
      dir = Enum.valueOf(LeftRightAny.class, dirS);
    int hdg = XmlLoaderUtils.loadInteger("value");
    ChangeHeadingCommand ret = ChangeHeadingCommand.create(hdg, dir);
    return ret;
  }
}
