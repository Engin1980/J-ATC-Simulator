package eng.jAtcSim.newLib.xml.speeches;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.xml.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SpeechXmlLoader implements IXmlLoader<ICommand> {

  private final IMap<String, IXmlLoader<? extends ICommand>> loaders;

  public SpeechXmlLoader() {
    this.loaders = new EMap<>();
    this.loaders.set("proceedDirect", new ProceedDirectCommandXmlLoader());
    this.loaders.set("then", new ThenCommandXmlLoader());
    this.loaders.set("speed", new ChangeSpeedCommandXmlLoader());
    this.loaders.set("altitude", new ChangeAltitudeCommandXmlLoader());
    this.loaders.set("altitudeRouteRestriction", new AltitudeRestrictionCommandXmlLoader());
    this.loaders.set("altitudeRouteRestrictionClear", new AltitudeRestrictionCommandXmlLoader());
    this.loaders.set("heading", new ChangeHeadingCommandXmlLoader());
    this.loaders.set("hold" , new HoldCommandXmlLoader());
    this.loaders.set("afterSpeed", new AfterSpeedCommandXmlLoader());
    this.loaders.set("afterHeading", new AfterHeadingCommandXmlLoader());
    this.loaders.set("afterAltitude", new AfterAltitudeCommandXmlLoader());
    this.loaders.set("afterDistance", new AfterDistanceCommandXmlLoader());
    this.loaders.set("afterRadial", new AfterRadialCommandXmlLoader());
    this.loaders.set("afterNavaid", new AfterNavaidCommandXmlLoader());
  }

  @Override
  public ICommand load(XElement source) {
    IXmlLoader<? extends ICommand> xmlLoader = this.loaders.tryGet(source.getName());
    if (xmlLoader == null)
      throw new XmlLoadException(
          sf("Unable to load command from xml-element '%s'. No loader defined for this element.", source.getName()));
    else{
      ICommand ret = xmlLoader.load(source);
      return ret;
    }
  }
}
