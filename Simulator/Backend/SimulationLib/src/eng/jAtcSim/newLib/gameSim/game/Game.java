package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.gameSim.xml.WeatherSourceDeserializer;
import eng.jAtcSim.newLib.gameSim.xml.WeatherSourceSerializer;
import eng.jAtcSimLib.xmlUtils.Parser;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Game implements IGame {

  private Game(){}

  static Game load(String fileName, IMap<String, Object> customData) {
    Game game = new Game(); //TODEL can public ctor be used?

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();

    XmlLoadUtils.Field.restoreField(root, game, "areaSource", (Parser) (q) -> {
      String[] pts = q.split(";");
      AreaSource ret = SourceFactory.createAreaSource(pts[0], pts[1]);
      return ret;
    });
    game.areaSource.init();
    XmlLoadUtils.Field.restoreField(root, game, "airplaneTypesSource",
            (Parser) q -> SourceFactory.createAirplaneTypesSource(q));
    game.airplaneTypesSource.init();
    XmlLoadUtils.Field.restoreField(root, game, "fleetsSource",
            (Parser) q -> {
              String[] pts = q.split(";");
              return SourceFactory.createFleetsSource(pts[1], pts[0]);
            });
    game.fleetsSource.init();
    XmlLoadUtils.Field.restoreField(root, game, "trafficSource",
            (Parser) q -> SourceFactory.createTrafficXmlSource(q));
    game.trafficSource.init();
    XmlLoadUtils.Field.restoreField(root, game, "weatherSource",
            new WeatherSourceDeserializer());
    game.weatherSource.init();


    SimulationStartupContext context = new SimulationStartupContext(
            game.areaSource.getArea(),
            game.areaSource.getIcao(),
            game.airplaneTypesSource.getContent(),
            game.fleetsSource.getContent().companyFleets,
            game.fleetsSource.getContent().gaFleets,
            game.trafficSource.getContent(),
            game.weatherSource.getContent()
    );
    SimulationSettings settings = new SimulationSettings(null, null);

    game.simulation = new Simulation(
            context,
            settings);

    throw new ToDoException();

    //game.simulation.load(root.getChild("simulation"));


//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading radar shortcuts...");
//    {
//      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
//      ret.simulation.setCommandShortcuts(shortcuts);
//    }
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading custom data...");
//    {
//      XElement elm = root.getChild("custom");
//      for (XElement child : elm.getChildren()) {
//        String key = child.getName();
//        Object obj = LoadSave.loadFromElement(elm, key, Object.class);
//        customData.set(key, obj);
//      }
//    }
//
//    return ret;

//    return game;
  }

  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

  public Game(AreaSource areaSource, AirplaneTypesSource airplaneTypesSource, FleetsSource fleetsSource, TrafficSource trafficSource, WeatherSource weatherSource, Simulation simulation) {
    this.areaSource = areaSource;
    this.airplaneTypesSource = airplaneTypesSource;
    this.fleetsSource = fleetsSource;
    this.trafficSource = trafficSource;
    this.weatherSource = weatherSource;
    this.simulation = simulation;
  }

  @Override
  public ISimulation getSimulation() {
    return this.simulation.isim;
  }

  @Override
  public void save(String fileName, IMap<String, String> customData) {
    XElement root = new XElement("game");

    XmlSaveUtils.saveIntoElementChild(root, "areaSource", this.areaSource,
            q -> sf("%s;%s", q.getFileName(), q.getIcao()));
    XmlSaveUtils.saveIntoElementChild(root, "airplaneTypesSource", this.airplaneTypesSource,
            q -> q.getFileName());
    XmlSaveUtils.saveIntoElementChild(root, "fleetsSource", this.fleetsSource,
            q -> sf("%s;%s", q.getCompanyFileName(), q.getGeneralAviationFileName()));
    XmlSaveUtils.saveIntoElementChild(root, "trafficSource", this.trafficSource,
            q -> ((TrafficXmlSource) q).getFileName());
    XmlSaveUtils.saveIntoElementChild(root, "weatherSource", this.weatherSource, new WeatherSourceSerializer());

    {
      XElement tmp = new XElement("simulation");
      this.simulation.save(tmp);
      root.addElement(tmp);
    }
//
//    XmlSaveUtils.saveIntoElementChild(root, "customData", customData,
//            new EntriesViaStringSerializer<>(q -> q, q -> q));


    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }
  }

}
