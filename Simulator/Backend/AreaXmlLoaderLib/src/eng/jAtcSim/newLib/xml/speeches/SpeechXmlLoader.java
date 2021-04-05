package eng.jAtcSim.newLib.xml.speeches;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;
import eng.jAtcSim.newLib.xml.speeches.atc2airplane.*;
import eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands.*;

import java.util.Optional;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SpeechXmlLoader extends XmlLoader<ICommand> {

  private final IMap<String, XmlLoader<? extends ICommand>> loaders;

  public SpeechXmlLoader(LoadingContext context) {
    super(context);
    this.loaders = new EMap<>();
    this.loaders.set("proceedDirect", new ProceedDirectCommandXmlLoader(context));
    this.loaders.set("then", new ThenCommandXmlLoader(context));
    this.loaders.set("speed", new ChangeSpeedCommandXmlLoader(context));
    this.loaders.set("altitude", new ChangeAltitudeCommandXmlLoader(context));
    this.loaders.set("altitudeRouteRestriction", new AltitudeRestrictionCommandXmlLoader(context));
    this.loaders.set("altitudeRouteRestrictionClear", new AltitudeRestrictionCommandXmlLoader(context));
    this.loaders.set("heading", new ChangeHeadingCommandXmlLoader(context));
    this.loaders.set("hold", new HoldCommandXmlLoader(context));
    this.loaders.set("afterSpeed", new AfterSpeedCommandXmlLoader(context));
    this.loaders.set("afterHeading", new AfterHeadingCommandXmlLoader(context));
    this.loaders.set("afterAltitude", new AfterAltitudeCommandXmlLoader(context));
    this.loaders.set("afterDistance", new AfterDistanceCommandXmlLoader(context));
    this.loaders.set("afterRadial", new AfterRadialCommandXmlLoader(context));
    this.loaders.set("afterNavaid", new AfterNavaidCommandXmlLoader(context));
  }

  @Override
  public ICommand load(XElement source) {
    ICommand ret;
    Optional<XmlLoader<? extends ICommand>> xmlLoader = this.loaders.tryGet(source.getName());
    if (xmlLoader.isEmpty()) {
      throw new EApplicationException(
              sf("Unable to load command from xml-element '%s'. No loader defined for this element.", source.getName()));
    } else {
      try {
        ret = xmlLoader.get().load(source);
      } catch (Exception e) {
        throw new EApplicationException(
                sf("Failed to parse speech from xml. XmlLoader: '%s', element: '%s'.", xmlLoader, source), e);
      }
    }
    return ret;
  }
}
