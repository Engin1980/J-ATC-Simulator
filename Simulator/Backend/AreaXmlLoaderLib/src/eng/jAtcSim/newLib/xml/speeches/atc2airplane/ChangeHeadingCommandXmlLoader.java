package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class ChangeHeadingCommandXmlLoader extends XmlLoader<ChangeHeadingCommand> {

  public ChangeHeadingCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public ChangeHeadingCommand load(XElement source) {
    assert source.getName().equals("heading");

    SmartXmlLoaderUtils.setContext(source);
    String dirS = SmartXmlLoaderUtils.loadString("direction", "nearest");
    LeftRightAny dir;
    if (dirS.equals("nearest"))
      dir = LeftRightAny.any;
    else
      dir = Enum.valueOf(LeftRightAny.class, dirS);
    int hdg = SmartXmlLoaderUtils.loadInteger("value");
    ChangeHeadingCommand ret = ChangeHeadingCommand.create(hdg, dir);
    return ret;
  }
}
