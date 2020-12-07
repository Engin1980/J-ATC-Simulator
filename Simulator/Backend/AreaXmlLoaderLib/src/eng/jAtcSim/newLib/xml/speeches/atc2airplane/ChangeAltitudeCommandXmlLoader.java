package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.xml.area.contextLocal.Context;

public class ChangeAltitudeCommandXmlLoader implements IXmlLoader<ChangeAltitudeCommand> {
  @Override
  public ChangeAltitudeCommand load(XElement source) {
    assert source.getName().equals("altitude");

    SmartXmlLoaderUtils.setContext(source);
    String dirS = SmartXmlLoaderUtils.loadString("direction", "set");
    ChangeAltitudeCommand.eDirection dir;
    if (dirS.equals("set"))
      dir = ChangeAltitudeCommand.eDirection.any;
    else
      dir = Enum.valueOf(ChangeAltitudeCommand.eDirection.class, dirS);
    int alt = SmartXmlLoaderUtils.loadAltitude("value");
    if (alt < 100 && alt != 0)
      System.out.println("Loading altitude with value " + alt + ". Is it correct?");
    ChangeAltitudeCommand ret = ChangeAltitudeCommand.create(dir, alt);
    return ret;
  }
}
