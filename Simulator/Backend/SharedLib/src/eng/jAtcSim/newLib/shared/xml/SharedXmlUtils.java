package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.*;
import eng.jAtcSimLib.xmlUtils.Formatter;
import eng.jAtcSimLib.xmlUtils.Serializer;

public class SharedXmlUtils {
  public static Formatter<AtcId> atcIdFormatter = q -> q.getName();
  public static Serializer<AtcId> atcIdSerializer = (e, q) -> e.setContent(atcIdFormatter.invoke(q));
  public static Formatter<Callsign> callsignFormatter = a -> a.toString(false);
  public static Serializer<Callsign> callsignSerializer = (e, q) -> e.setContent(callsignFormatter.invoke(q));
  public static Formatter<Coordinate> coordinateFormatter = q -> q.getLatitude().toDecimalString(true) + ";" + q.getLongitude().toDecimalString(true);
  public static Serializer<Coordinate> coordinateSerializer = (e, q) -> e.setContent(coordinateFormatter.invoke(q));
  public static Formatter<Squawk> squawkFormatter = q -> q.toString();
  public static Serializer<Squawk> squawkSerializer = (e, q) -> e.setContent(squawkFormatter.invoke(q));
  public static Formatter<ITime> iTimeFormatter = q -> q.toString();
  public static Serializer<ITime> iTimeSerializer = (e, q) -> e.setContent(iTimeFormatter.invoke(q));

  public static IMap<Class<?>, Serializer<?>> serializersMap;
  public static IMap<Class<?>, Formatter<?>> formattersMap;

  static {
    serializersMap = new EMap<>();
    serializersMap.set(AtcId.class, atcIdSerializer);
    serializersMap.set(Callsign.class, callsignSerializer);
    serializersMap.set(Squawk.class, squawkSerializer);
    serializersMap.set(Coordinate.class, coordinateSerializer);
    serializersMap.set(EDayTimeRun.class, iTimeSerializer);
    serializersMap.set(EDayTimeStamp.class, iTimeSerializer);
    serializersMap.set(ETimeStamp.class, iTimeSerializer);

    formattersMap = new EMap<>();
    formattersMap.set(AtcId.class, atcIdFormatter);
    formattersMap.set(Callsign.class, callsignFormatter);
    formattersMap.set(Squawk.class, squawkFormatter);
    formattersMap.set(Coordinate.class, coordinateFormatter);
    formattersMap.set(EDayTimeRun.class, iTimeFormatter);
    formattersMap.set(EDayTimeStamp.class, iTimeFormatter);
    formattersMap.set(ETimeStamp.class, iTimeFormatter);
  }
}
