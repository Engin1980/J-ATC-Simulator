package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;

public class ChangeHeadingCommandFactory {
  public static ChangeHeadingCommand load(XElement source) {
    assert source.getName().equals("heading");

    XmlLoader.setContext(source);
    String dirS = XmlLoader.loadString("direction", "nearest");
    ChangeHeadingCommand.eDirection dir;
    if (dirS.equals("nearest"))
      dir = ChangeHeadingCommand.eDirection.any;
    else
      dir = Enum.valueOf(ChangeHeadingCommand.eDirection.class, dirS);
    int hdg = XmlLoader.loadInteger("value");
    ChangeHeadingCommand ret = ChangeHeadingCommand.create(hdg, dir);
    return ret;
  }
}
