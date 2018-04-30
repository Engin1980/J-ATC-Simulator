package eng.jAtcSim.lib.serialization;

import eng.eSystem.collections.EList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.IInstanceCreator;
import eng.eSystem.xmlSerialization.Settings;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.StringResponse;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;

import java.lang.reflect.Field;

public class LoadSave {

  private static XmlSerializer ser;

  static {
    Settings sett = new Settings();
    sett.getElementParsers().add(new NavaidParser());
    sett.getElementParsers().add(new AirplaneTypeParser());
    sett.getElementParsers().add(new RunwayThresholdParser());
    sett.getElementParsers().add(new RunwayParser());
    sett.getElementParsers().add(new RouteParser());
    sett.getElementParsers().add(new AirplaneParser());
    sett.getElementParsers().add(new AtcParser());
    sett.getElementParsers().add(new AirportParser());

    sett.getInstanceCreators().add(new PlaneSwitchMessageIC());
    sett.getInstanceCreators().add(new RunwayCheckIC());
    sett.getInstanceCreators().add(new StringResponseIC());
    // TODO here should be all classes with private constructors defined for xml loading

    sett.getIgnoredFieldsRegex().add("this\\$0"); // parent of inner class

    sett.setVerbose(true);
    ser = new XmlSerializer(sett);
  }

  public static void saveField(XElement elm, Object src, String fieldName) {
    Object v = getFieldValue(src, fieldName);
//    if (v == null || v.getClass().isPrimitive() || v.getClass().isEnum())
//      LoadSave.saveAsAttribute(elm, fieldName, v);
//    else
    LoadSave.saveAsElement(elm, fieldName, v);
  }

//  public static void saveAsAttribute(XElement elm, String name, Object value) {
//    if (value == null)
//      elm.setAttribute(name, "(null)");
//    else
//      elm.setAttribute(name, value.toString());
//  }
//
//  public static Object loadFromAttribute(XElement elm, String name) {
//    Object ret;
//    String s = elm.getContent();
//    if (s.equals("(null)"))
//      ret = null;
//    else
//      ret = elm.getAttributes().get(name);
//    return ret;
//  }

  public static void saveAsElement(XElement elm, String name, Object obj) {
    XElement tmp = new XElement(name);
    elm.addElement(tmp);
    try {
      ser.serialize(tmp, obj);
    } catch (XmlSerializationException e) {
      throw new EApplicationException("Failed to save object " + obj + ".", e);
    }
  }

  public static Object loadFromElement(XElement elm, String name, Class type) {
    XElement tmp = elm.getChildren().getFirst(q -> q.getName().equals(name));
    Object ret;
    try {
      ret = ser.deserialize(tmp, type);
    } catch (Exception e) {
      throw new EApplicationException("Failed to load object " + name + " of type " + type.getClass().getName() + ".", e);
    }
    return ret;
  }

  public static void loadField(XElement elm, Object src, String fieldName) {
    Field f;
    try {
      f = getField(src.getClass(), fieldName);
    } catch (NoSuchFieldException e) {
      throw new EApplicationException("Unable to find field " + fieldName + " in type " + src.getClass().getName());
    }

    Object v;
//    if ((f.getType().isPrimitive() || f.getType().isEnum())
//        &&
//        elm.getAttributes().containsKey(fieldName))
//      v = LoadSave.loadFromAttribute(elm, fieldName);
//    else
    v = LoadSave.loadFromElement(elm, fieldName, f.getType());

    try {
      f.setAccessible(true);
      f.set(src, v);
    } catch (IllegalAccessException e) {
      throw new EApplicationException("Unable to set value " + v + " into " + src.getClass().getName() + "." + f.getName());
    }
  }

  public static void setRelativeArea(Area area, Airport aip, Atc[] atcs) {

    NavaidParser np = (NavaidParser) ser.getSettings().getElementParsers().getFirst(q -> q instanceof NavaidParser);
    np.setRelative(area.getNavaids());

    AirportParser ap = (AirportParser) ser.getSettings().getElementParsers().getFirst(q -> q instanceof AirportParser);
    ap.setRelative(area.getAirports());

    AtcParser atp = (AtcParser) ser.getSettings().getElementParsers().getFirst(q -> q instanceof AtcParser);
    atp.setRelative(new EList<Atc>(atcs));

    RouteParser rp = (RouteParser) ser.getSettings().getElementParsers().getFirst(q -> q instanceof RouteParser);
    rp.setRelative(aip);

  }

  public static void setRelativeAirplaneTypes(AirplaneTypes types) {
    AirplaneTypeParser atp = (AirplaneTypeParser) ser.getSettings().getElementParsers().getFirst(q -> q instanceof AirplaneTypeParser);
    atp.setRelative(types);
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


class PlaneSwitchMessageIC implements IInstanceCreator<PlaneSwitchMessage> {

  @Override
  public Class getType() {
    return PlaneSwitchMessage.class;
  }

  @Override
  public PlaneSwitchMessage createInstance() {
    return new PlaneSwitchMessage(null);
  }
}

class RunwayCheckIC implements IInstanceCreator<RunwayCheck> {

  @Override
  public Class getType() {
    return RunwayCheck.class;
  }

  @Override
  public RunwayCheck createInstance() {
    return new RunwayCheck(null, RunwayCheck.eType.askForTime);
  }
}

class StringResponseIC implements IInstanceCreator<StringResponse> {

  @Override
  public Class getType() {
    return StringResponse.class;
  }

  @Override
  public StringResponse createInstance() {
    return new StringResponse(false, "");
  }
}