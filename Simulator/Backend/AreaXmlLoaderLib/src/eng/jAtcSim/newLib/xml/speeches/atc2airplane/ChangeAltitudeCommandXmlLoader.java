package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;

public class ChangeAltitudeCommandXmlLoader implements IXmlLoader<ChangeAltitudeCommand> {
  @Override
  public ChangeAltitudeCommand load(XElement source) {
    assert source.getName().equals("altitude");

    XmlLoaderUtils.setContext(source);
    String dirS = XmlLoaderUtils.loadString("direction", "set");
    ChangeAltitudeCommand.eDirection dir;
    if (dirS.equals("set"))
      dir = ChangeAltitudeCommand.eDirection.any;
    else
      dir = Enum.valueOf(ChangeAltitudeCommand.eDirection.class, dirS);
    int alt = XmlLoaderUtils.loadAltitude("value");
    ChangeAltitudeCommand ret = ChangeAltitudeCommand.create(dir, alt);
    return ret;
  }
}
