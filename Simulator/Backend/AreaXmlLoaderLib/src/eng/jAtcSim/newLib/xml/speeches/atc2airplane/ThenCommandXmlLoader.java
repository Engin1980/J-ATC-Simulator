package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class ThenCommandXmlLoader extends XmlLoader<ThenCommand> {

  public ThenCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public ThenCommand load(XElement element) {
    assert element.getName().equals("then");
    return ThenCommand.create();
  }
}
