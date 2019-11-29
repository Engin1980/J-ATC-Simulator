package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.CoordinateValue;
import eng.eSystem.xmlSerialization.Log;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IFactory;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.atcs.CenterAtc;
import eng.jAtcSim.lib.atcs.TowerAtc;
import eng.jAtcSim.lib.atcs.UserAtc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.StringResponse;

import java.lang.reflect.Field;

public class LoadSave {

  private static XmlSerializer ser;

  private static NavaidParser navaidParser = new NavaidParser();
  private static AirportParser airportParser = new AirportParser();
  private static AtcParser atcParser = new AtcParser();
  private static RouteParser routeParser = new RouteParser();
  private static RunwayParser runwayParser = new RunwayParser();
  private static RunwayThresholdParser runwayThresholdParser = new RunwayThresholdParser();
  private static AirplaneTypeParser airplaneTypeParser = new AirplaneTypeParser();
  private static AirplaneParser airplaneParser = new AirplaneParser();

  static {
    XmlSettings sett = new XmlSettings();

    // parser
    sett.forType(Navaid.class).setCustomParser(navaidParser);
    sett.forType(AirplaneType.class).setCustomParser(airplaneTypeParser);
    sett.forType(ActiveRunwayThreshold.class).setCustomParser(runwayThresholdParser);
    sett.forType(ActiveRunway.class).setCustomParser(runwayParser);
    sett.forType(DARoute.class).setCustomParser(routeParser);
    sett.forType(Airplane.class).setCustomParser(airplaneParser);
    sett.forType(Atc.class).setCustomParser(atcParser);
    sett.forType(UserAtc.class).setCustomParser(atcParser);
    sett.forType(CenterAtc.class).setCustomParser(atcParser);
    sett.forType(TowerAtc.class).setCustomParser(atcParser);
    sett.forType(Airport.class).setCustomParser(airportParser);

    // factories
    sett.forType(PlaneSwitchMessage.class).setFactory(new PlaneSwitchMessageIC());
    sett.forType(RunwayCheck.class).setFactory(new RunwayCheckIC());
    sett.forType(StringResponse.class).setFactory(new StringResponseIC());
    sett.forType(Coordinate.class).setFactory(new CoordinateFactory());
    sett.forType(CoordinateValue.class).setFactory(new CoordinateValueFactory());

    sett.setLogLevel(Log.LogLevel.warning);
    ser = new XmlSerializer(sett);
  }

  public static void saveField(XElement elm, Object src, String fieldName) {
    Object v = getFieldValue(src, fieldName);
    LoadSave.saveAsElement(elm, fieldName, v);
  }

  public static void saveAsElement(XElement elm, String elementName, Object obj) {
    XElement tmp = saveIntoElement(elementName, obj);
    elm.addElement(tmp);
  }

  public static XElement saveIntoElement(String elementName, Object obj) {
    XElement ret = new XElement(elementName);
    try {
      ser.serialize(ret, obj);
    } catch (XmlSerializationException e) {
      throw new EApplicationException("Failed to save object " + obj + ".", e);
    }
    return ret;
  }

  public static Object loadFromElement(XElement elm, String name, Class type) {
    XElement tmp = elm.getChildren().getFirst(q -> q.getName().equals(name));
    Object ret;
    try {
      ret = ser.deserialize(tmp, type);
    } catch (Exception e) {
      throw new EApplicationException("Failed to load object " + name + " of kind " + type.getClass().getName() + ".", e);
    }
    return ret;
  }

  public static void loadField(XElement elm, Object src, String fieldName) {
    Field f;
    try {
      f = getField(src.getClass(), fieldName);
    } catch (NoSuchFieldException e) {
      throw new EApplicationException("Unable to find field " + fieldName + " in kind " + src.getClass().getName());
    }

    Object v;
    v = LoadSave.loadFromElement(elm, fieldName, f.getType());

    try {
      f.setAccessible(true);
      f.set(src, v);
    } catch (IllegalAccessException e) {
      throw new EApplicationException("Unable to set value " + v + " into " + src.getClass().getName() + "." + f.getName());
    }
  }

  public static void setRelativeArea(Area area, Airport aip, Atc[] atcs) {
    navaidParser.setRelative(area.getNavaids());

    airportParser.setRelative(area.getAirports());

    atcParser.setRelative(new EList<>(atcs));

    routeParser.setRelative(aip);

    runwayParser.setRelative(aip);

    runwayThresholdParser.setRelative(aip);
  }

  public static void setRelativeAirplaneTypes(AirplaneTypes types) {
    airplaneTypeParser.setRelative(types);
  }

  public static void loadFromElement(XElement elm, Object object) {
    ser.deserializeContent(elm, object);
  }

  public static void setRelativeAirplanes(IList<Airplane> lst) {
    airplaneParser.setRelative(lst);
  }

  private static Object getFieldValue(Object src, String fieldName) {
    Class cls = src.getClass();
    Field f;
    Object v;
    try {
      f = getField(cls, fieldName);
      f.setAccessible(true);
      v = f.get(src);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      throw new EApplicationException("Unreadable field " + fieldName + " on object " + src.getClass(), ex);
    }
    return v;
  }

  private static Field getField(Class type, String name) throws NoSuchFieldException {
    Field f;
    try {
      f = type.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      if (type.equals(Object.class))
        throw e;
      else {
        type = type.getSuperclass();
        f = getField(type, name);
      }
    }
    return f;
  }
}


class PlaneSwitchMessageIC implements IFactory<PlaneSwitchMessage> {
  @Override
  public PlaneSwitchMessage createInstance() {
    return new PlaneSwitchMessage(null, PlaneSwitchMessage.eMessageType.rejection);
  }
}

class RunwayCheckIC implements IFactory<RunwayCheck> {
  @Override
  public RunwayCheck createInstance() {
    return new RunwayCheck(null, RunwayCheck.eType.askForTime);
  }
}

class StringResponseIC implements IFactory<StringResponse> {
  @Override
  public StringResponse createInstance() {
    return new StringResponse(false, "");
  }
}

class CoordinateFactory implements IFactory<Coordinate>{

  @Override
  public Coordinate createInstance() {
    return new Coordinate(Double.NaN, Double.NaN);
  }
}

class CoordinateValueFactory implements IFactory<CoordinateValue>{

  @Override
  public CoordinateValue createInstance() {
    return new CoordinateValue(Double.NaN);
  }
}
