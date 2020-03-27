package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;

public class ChangeAltitudeCommandFactory {
  public static ChangeAltitudeCommand load(XElement source) {
    assert source.getName().equals("altitude");

    XmlLoader.setContext(source);
    String dirS = XmlLoader.loadString("direction", "set");
    ChangeAltitudeCommand.eDirection dir;
    if (dirS.equals("set"))
      dir = ChangeAltitudeCommand.eDirection.any;
    else
      dir = Enum.valueOf(ChangeAltitudeCommand.eDirection.class, dirS);
    int alt = XmlLoader.loadAltitude("value");
    ChangeAltitudeCommand ret = ChangeAltitudeCommand.create(dir, alt);
    return ret;
  }
}
