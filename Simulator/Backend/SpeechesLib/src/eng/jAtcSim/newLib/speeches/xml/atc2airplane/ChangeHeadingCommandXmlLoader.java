package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;

public class ChangeHeadingCommandXmlLoader implements IXmlLoader<ChangeHeadingCommand> {
  @Override
  public ChangeHeadingCommand load(XElement source) {
    assert source.getName().equals("heading");

    XmlLoaderUtils.setContext(source);
    String dirS = XmlLoaderUtils.loadString("direction", "nearest");
    ChangeHeadingCommand.eDirection dir;
    if (dirS.equals("nearest"))
      dir = ChangeHeadingCommand.eDirection.any;
    else
      dir = Enum.valueOf(ChangeHeadingCommand.eDirection.class, dirS);
    int hdg = XmlLoaderUtils.loadInteger("value");
    ChangeHeadingCommand ret = ChangeHeadingCommand.create(hdg, dir);
    return ret;
  }
}
