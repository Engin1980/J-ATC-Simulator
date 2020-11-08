package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.time.ITime;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.Formatter;
import eng.jAtcSimLib.xmlUtils.Parser;
import eng.jAtcSimLib.xmlUtils.Serializer;
import eng.jAtcSimLib.xmlUtils.deserializers.ProxyDeserializer;

public class SharedXmlUtils {

  public static class Formatters{
    public static Formatter<AtcId> atcIdFormatter = q -> q.getName();
    public static Formatter<Callsign> callsignFormatter = a -> a.toString(false);
    public static Formatter<Coordinate> coordinateFormatter = q -> q.getLatitude().toDecimalString(true) + ";" + q.getLongitude().toDecimalString(true);
    public static Formatter<Squawk> squawkFormatter = q -> q.toString();
    public static Formatter<ITime> iTimeFormatter = q -> q.format();

    public static IMap<Class<?>, Formatter<?>> formattersMap;

    static{
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

  public static class Serializers{
    public static Serializer<AtcId> atcIdSerializer = (e, q) -> e.setContent(Formatters.atcIdFormatter.invoke(q));
    public static Serializer<Callsign> callsignSerializer = (e, q) -> e.setContent(Formatters.callsignFormatter.invoke(q));
    public static Serializer<Coordinate> coordinateSerializer = (e, q) -> e.setContent(Formatters.coordinateFormatter.invoke(q));
    public static Serializer<Squawk> squawkSerializer = (e, q) -> e.setContent(Formatters.squawkFormatter.invoke(q));
    public static Serializer<ITime> iTimeSerializer = (e, q) -> e.setContent(Formatters.iTimeFormatter.invoke(q));

    public static IMap<Class<?>, Serializer<?>> serializersMap;

    static{
      serializersMap = new EMap<>();
      serializersMap.set(AtcId.class, atcIdSerializer);
      serializersMap.set(Callsign.class, callsignSerializer);
      serializersMap.set(Squawk.class, squawkSerializer);
      serializersMap.set(Coordinate.class, coordinateSerializer);
      serializersMap.set(EDayTimeRun.class, iTimeSerializer);
      serializersMap.set(EDayTimeStamp.class, iTimeSerializer);
      serializersMap.set(ETimeStamp.class, iTimeSerializer);
    }
  }

  public static class Parsers{
    public static Parser dayTimeStampParser = q -> EDayTimeStamp.parse(q);
    public static Parser dayTimeRunParser = q -> EDayTimeRun.parse(q);

  }

  public static class Deserializers {
    public static Deserializer squawkDeserializer= e -> Squawk.create(e.getContent().toCharArray());
    public static Deserializer coordinateDeserializer =  e-> {
      String[] pts = e.getContent().split(";");
      Coordinate ret = new Coordinate(
              Double.parseDouble(pts[0]), Double.parseDouble(pts[1]));
      return ret;
    };
    public static Deserializer dayTimeStampDeserializer = Parsers.dayTimeStampParser.toDeserializer();

    public static IMap<Class<?>, Deserializer> deserializersMap;

    static{
      deserializersMap = new EMap<>();
      deserializersMap.set(EDayTimeStamp.class, dayTimeStampDeserializer);
      deserializersMap.set(Squawk.class, squawkDeserializer);
      deserializersMap.set(Coordinate.class, coordinateDeserializer);
    }
  }

  public static class DeserializersDynamic{
    public static IMap<Class<?>, Deserializer> deserializersMap;

    public static Deserializer getAtcIdDeserializer(IReadOnlyList<AtcId> atcs) {
      return new ProxyDeserializer<>(
              q -> q.getContent(),
              q -> q.getName(),
              atcs
      );
    }
  }
}
