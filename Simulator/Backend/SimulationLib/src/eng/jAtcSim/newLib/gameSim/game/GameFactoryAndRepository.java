package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.functionalInterfaces.Producer;
import eng.eSystem.functionalInterfaces.Selector;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.sources.AirplaneTypesSource;
import eng.jAtcSim.newLib.gameSim.game.sources.AreaSource;
import eng.jAtcSim.newLib.gameSim.game.sources.FleetsSource;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import exml.Constants;
import exml.TypingShortcutsProvider;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class GameFactoryAndRepository {

  public Game create(GameStartupInfo gsi) {
    Game game;
    ApplicationLog appLog = Context.getApp().getAppLog();

    Simulation simulation;

    try {
      appLog.write(ApplicationLog.eType.info, "Loading area");
      EAssert.isNotNull(gsi.areaSource, "Area-Source not set.");
      gsi.areaSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize area.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading plane types");
      EAssert.isNotNull(gsi.airplaneTypesSource, "Airplane-Type-Source not set.");
      gsi.airplaneTypesSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize plane types.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading fleets");
      EAssert.isNotNull(gsi.fleetsSource, "Fleet-Source not set.");
      gsi.fleetsSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize fleets.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading traffic");
      EAssert.isNotNull(gsi.trafficSource, "Traffic-Source not set.");
      gsi.trafficSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Initializing weather");
      EAssert.isNotNull(gsi.weatherSource, "Weather-Source not set.");
      gsi.weatherSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load, download or initialize weather.", ex);
    }

    PostContracts.checkAndClear();

    try {
      appLog.write(ApplicationLog.eType.info, "Creating the simulation");
      SimulationStartupContext simulationContext = new SimulationStartupContext(
              gsi.areaSource.getContent(),
              gsi.areaSource.getIcao(),
              gsi.airplaneTypesSource.getContent(),
              gsi.fleetsSource.getContent().companyFleets,
              gsi.fleetsSource.getContent().gaFleets,
              gsi.trafficSource.getContent(),
              gsi.weatherSource.getContent()
      );

      SimulationSettings simulationSettings = new SimulationSettings(
              gsi.trafficSettings,
              gsi.simulationSettings
      );

      simulation = new Simulation(simulationContext, simulationSettings);
      simulation.init();
      game = new Game(
              gsi.areaSource,
              gsi.airplaneTypesSource,
              gsi.fleetsSource,
              gsi.trafficSource,
              gsi.weatherSource,
              simulation
      );
      appLog.write(ApplicationLog.eType.info, "Initializing the simulation");
    } catch (Exception ex) {
      throw new EApplicationException("Unable to create or initialize the simulation.", ex);
    }

    PostContracts.checkAndClear();

    return game;
  }

  public Game load(String fileName) {
    //TODO update with custom data
    IMap<String, Object> customData = new EMap<>();

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();
    TypingShortcutsProvider.expandTypes(root);

    XLoadContext ctx = new XLoadContext();
    initLoadingContext(ctx);

    GameStartupInfo gsi = new GameStartupInfo();
    gsi.areaSource = ctx.loadObject(root.getChild("areaSource"), AreaSource.class);
    gsi.airplaneTypesSource = ctx.loadObject(root.getChild("airplaneTypesSource"), AirplaneTypesSource.class);
    gsi.fleetsSource = ctx.loadObject(root.getChild("fleetsSource"), FleetsSource.class);
    gsi.trafficSource = ctx.loadObject(root.getChild("trafficSource"), null); // type derived by xml
    gsi.weatherSource = ctx.loadObject(root.getChild("weatherSource"), null); // type derived by xml

    gsi.areaSource.init();
    gsi.airplaneTypesSource.init();
    gsi.fleetsSource.init();
    gsi.trafficSource.init();
    gsi.weatherSource.init();

    ctx.parents.set(gsi.areaSource.getArea());
    ctx.parents.set(gsi.areaSource.getActiveAirport());

    AtcIdList atcIdList = new AtcIdList();
    atcIdList.addMany(gsi.areaSource.getActiveAirport().getAtcTemplates().select(qq -> qq.toAtcId()));

    ctx.values.set(atcIdList);
    ctx.values.set(gsi.areaSource.getArea().getNavaids());
    ctx.values.set(gsi.airplaneTypesSource.getContent());
    ctx.values.set(gsi.fleetsSource.getContent().companyFleets);
    ctx.values.set(gsi.fleetsSource.getContent().gaFleets);
    ctx.values.set(ITrafficModel.class, gsi.trafficSource.getClass());
    ctx.values.set(WeatherProvider.class, gsi.weatherSource.getContent());

    SharedAcc sharedContext = new SharedAcc(
            gsi.areaSource.getActiveAirport().getIcao(),
            atcIdList,
            new EDayTimeRun(0),
            new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    Simulation simulation = ctx.loadObject(root.getChild("simulation"), Simulation.class);
    simulation.init();

    PostContracts.checkAndClear();

    Game game = new Game(
            gsi.areaSource,
            gsi.airplaneTypesSource,
            gsi.fleetsSource,
            gsi.trafficSource,
            gsi.weatherSource,
            simulation
    );

    return game;
  }

  public void save(IGame game, IMap<String, Object> customData, String fileName) {
    XElement root = new XElement("game");

    XSaveContext ctx = new XSaveContext();
    initSavingContext(ctx);

    try {
      ctx.saveObject(game, root);
      TypingShortcutsProvider.collapseTypes(root);
    } catch (Exception ex) {
      System.out.println("Failed to save the whole save file");
      ex.printStackTrace(System.out);
    }

    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }
  }

  private void initSavingContext(XSaveContext ctx) {

    ctx.setFormatter(short.class, q -> q.toString());
    ctx.setFormatter(byte.class, q -> q.toString());
    ctx.setFormatter(int.class, q -> q.toString());
    ctx.setFormatter(long.class, q -> q.toString());
    ctx.setFormatter(float.class, q -> q.toString());
    ctx.setFormatter(double.class, q -> q.toString());
    ctx.setFormatter(boolean.class, q -> q.toString());
    ctx.setFormatter(char.class, q -> q.toString());
    ctx.setFormatter(Short.class, q -> q.toString());
    ctx.setFormatter(Byte.class, q -> q.toString());
    ctx.setFormatter(Integer.class, q -> q.toString());
    ctx.setFormatter(Long.class, q -> q.toString());
    ctx.setFormatter(Float.class, q -> q.toString());
    ctx.setFormatter(Double.class, q -> q.toString());
    ctx.setFormatter(Boolean.class, q -> q.toString());
    ctx.setFormatter(Character.class, q -> q.toString());
    ctx.setFormatter(String.class, v -> v);
    ctx.setSerializer(ArrayList.class, getItemsConsumer(true, Object.class, ctx));

    // eSystem
    ctx.setSerializer(AirplaneList.class, getItemsConsumer(false, null, ctx));
    ctx.setSerializer(EList.class, getItemsConsumer(true, Object.class, ctx));
    ctx.setSerializer(EDistinctList.class, getItemsConsumer(true, Object.class, ctx));
    ctx.setSerializer(ESet.class, getItemsConsumer(true, Object.class, ctx));
    ctx.setSerializer(EMap.class, getEntriesConsumer(true, Object.class, Object.class, ctx));
    ctx.setFormatter(Coordinate.class, q -> q.getLatitude().toDecimalString(true) + ";" + q.getLongitude().toDecimalString(true));

    // shared
    ctx.setFormatter(Callsign.class, q -> q.toString(true));
    ctx.setFormatter(Restriction.class, q -> q.direction.toString() + ";" + q.value);
    ctx.setFormatter(EDayTimeStamp.class, v -> v.toDayTimeString());
    ctx.setFormatter(EDayTimeRun.class, v -> v.toDayTimeString());
    ctx.setFormatter(ETimeStamp.class, v -> v.toTimeString());
    ctx.setFormatter(Squawk.class, q -> q.toString());

    // area
    ctx.setFormatter(Navaid.class, q -> q.getName());
    ctx.setFormatter(ActiveRunwayThreshold.class, q -> q.getName());

    // airplane type
    ctx.setFormatter(AirplaneType.class, q -> q.name);

    // atc
    ctx.setFormatter(AtcId.class, v -> v.getName());

    // airplane

  }

  private void initLoadingContext(XLoadContext ctx) {
    ctx.setParser(short.class, q -> Short.valueOf(q));
    ctx.setParser(byte.class, q -> Byte.valueOf(q));
    ctx.setParser(int.class, q -> Integer.valueOf(q));
    ctx.setParser(long.class, q -> Long.valueOf(q));
    ctx.setParser(float.class, q -> Float.valueOf(q));
    ctx.setParser(double.class, q -> Double.valueOf(q));
    ctx.setParser(boolean.class, q -> Boolean.valueOf(q));
    ctx.setParser(char.class, q -> q.charAt(0));
    ctx.setParser(Short.class, q -> Short.valueOf(q));
    ctx.setParser(Byte.class, q -> Byte.valueOf(q));
    ctx.setParser(Integer.class, q -> Integer.valueOf(q));
    ctx.setParser(Long.class, q -> Long.valueOf(q));
    ctx.setParser(Float.class, q -> Float.valueOf(q));
    ctx.setParser(Double.class, q -> Double.valueOf(q));
    ctx.setParser(Boolean.class, q -> Boolean.valueOf(q));
    ctx.setParser(Character.class, q -> q.charAt(0));
    ctx.setParser(String.class, q -> q);
    ctx.setDeserializer(ArrayList.class, getItemsProducer(() -> new ArrayList<>(), Object.class, ctx));

    // eSystem
    //getItemsProducer(AirplaneList.createForAirplane()), null, ctx)
    //ctx.setDeserializer(AirplaneList.class, AirplaneList.getAirplaneListForAirplanesDeserializer());
    ctx.setDeserializer(EList.class, getItemsProducer(() -> new EList<>(), Object.class, ctx));
    ctx.setDeserializer(EDistinctList.class, getItemsProducer(() -> new EDistinctList<>(), Object.class, ctx));
    ctx.setDeserializer(ESet.class, getItemsProducer(() -> new ESet<>(), Object.class, ctx));
    ctx.setDeserializer(EMap.class, getEntriesProducer(() -> new EMap<>(), Object.class, Object.class, ctx));
    ctx.setParser(Coordinate.class, q -> {
      String[] pts = q.split(";");
      NumberFormat nf = new DecimalFormat("00.00000");
      double lat, lng;
      try {
        Number num;
        num = nf.parse(pts[0]);
        lat = num instanceof Long ? (double) (long) (num) : (double) num;
        num = nf.parse(pts[1]);
        lng = num instanceof Long ? (double) (long) (num) : (double) num;
      } catch (ParseException e) {
        throw new EApplicationException(sf("Failed to parse %s to latitude/longitude coordinate.", q));
      }
      Coordinate ret = new Coordinate(lat, lng);
      return ret;
    });

    // shared
    ctx.setParser(Callsign.class, q -> new Callsign(q));
    ctx.setParser(Restriction.class, q -> {
      String[] pts = q.split(";");
      AboveBelowExactly abe = Enum.valueOf(AboveBelowExactly.class, pts[0]);
      int val = Integer.parseInt(pts[1]);
      return new Restriction(abe, val);
    });
    ctx.setParser(EDayTimeStamp.class, v -> EDayTimeStamp.parse(v));
    ctx.setParser(EDayTimeRun.class, v -> EDayTimeRun.parse(v));
    ctx.setParser(ETimeStamp.class, v -> ETimeStamp.parse(v));
    ctx.setParser(Squawk.class, v -> Squawk.create(v.toCharArray()));

    // area
    ctx.setParser(Navaid.class, v -> ctx.values.get(NavaidList.class).get(v));
    ctx.setParser(ActiveRunwayThreshold.class, v -> ctx.parents.get(Airport.class).getRunwayThreshold(v));

    // airplane type
    ctx.setParser(AirplaneType.class, v -> ctx.values.get(AirplaneTypes.class).getByName(v));

    // atc
    ctx.setParser(AtcId.class, v -> {
      AtcIdList atcIds = ctx.values.get(AtcIdList.class);
      return atcIds.getFirst(q -> q.getName().equals(v));
    });
  }

  private <T extends Iterable<?>> Consumer2<T, XElement> getItemsConsumer(boolean saveItemsType, Class<?> expectedItemType, XSaveContext ctx) {
    Consumer2<T, XElement> ret = (lst, e) -> {
      if (saveItemsType)
        e.setAttribute("__type", lst.getClass().getName());
      ctx.objects.saveItems(lst, expectedItemType, e);
    };

    return ret;
  }

  private <T> Selector<XElement, T> getItemsProducer(Producer<Object> listProducer, Class<?> expectedItemType, XLoadContext ctx) {
    Selector<XElement, T> ret = e -> {
      Object target = listProducer.invoke();
      ctx.loadItems(e, target, expectedItemType);
      return (T) target;
    };

    return ret;
  }

  private <T> Selector<XElement, T> getEntriesProducer(Producer<Object> mapProducer, Class<?> expectedKeyType, Class<?> expectedValueType, XLoadContext ctx) {
    Selector<XElement, T> ret = e -> {
      ESet<Tuple<?, ?>> entries = new ESet<>();

      for (XElement entryElement : e.getChildren(Constants.ENTRY_ELEMENT)) {
        XElement keyElement = entryElement.getChild(Constants.KEY_ELEMENT);
        XElement valueElement = entryElement.getChild(Constants.VALUE_ELEMENT);
        Object key = ctx.loadObject(keyElement, expectedKeyType);
        Object value = ctx.loadObject(valueElement, expectedValueType);

        entries.add(new Tuple<>(key, value));
      }

      Object target = mapProducer.invoke();
      if (target instanceof IMap)
        entries.forEach(q -> ((IMap) target).set(q.getA(), q.getB()));
      else if (target instanceof Map)
        entries.forEach(q -> ((Map) target).put(q.getA(), q.getB()));
      else
        throw new RuntimeException(sf("getEntriesProducer does not support map type %s", target.getClass()));

      T tmp = (T) target;
      return tmp;
    };

    return ret;
  }

  private <K, V, T extends Iterable<Map.Entry<K, V>>> Consumer2<T, XElement> getEntriesConsumer(boolean saveItemsType, Class<K> expectedKeyType, Class<V> expectedValueType, XSaveContext ctx) {
    Consumer2<T, XElement> ret = (lst, e) -> {
      if (saveItemsType)
        e.setAttribute("__type", lst.getClass().getName());
      ctx.objects.saveEntries(lst, expectedKeyType, expectedValueType, e);
    };

    return ret;
  }
}
