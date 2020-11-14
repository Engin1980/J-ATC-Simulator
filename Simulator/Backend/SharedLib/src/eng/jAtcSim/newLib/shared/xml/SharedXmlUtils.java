package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.time.ITime;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Formatter;
import eng.newXmlUtils.base.Parser;
import eng.newXmlUtils.base.Serializer;

public class SharedXmlUtils {

  public static class Formatters {
    public static Formatter<AtcId> atcIdFormatter = q -> q.getName();
    public static Formatter<Callsign> callsignFormatter = a -> a.toString(false);
    public static Formatter<Coordinate> coordinateFormatter = q -> q.getLatitude().toDecimalString(true) + ";" + q.getLongitude().toDecimalString(true);
    public static Formatter<Squawk> squawkFormatter = q -> q.toString();
    public static Formatter<ITime> iTimeFormatter = q -> q.format();

    public static IMap<Class<?>, Formatter<?>> formatters;

    static {
      formatters = new EMap<>();
      formatters.set(AtcId.class, atcIdFormatter);
      formatters.set(Callsign.class, callsignFormatter);
      formatters.set(Squawk.class, squawkFormatter);
      formatters.set(Coordinate.class, coordinateFormatter);
      formatters.set(EDayTimeRun.class, iTimeFormatter);
      formatters.set(EDayTimeStamp.class, iTimeFormatter);
      formatters.set(ETimeStamp.class, iTimeFormatter);
    }
  }

  public static class Serializers {
    public static IMap<Class<?>, Serializer> serializers;

    static {
      serializers = Formatters.formatters.select(q -> q, q -> q.toSerializer());
    }
  }

  public static class Parsers {
    public static final IMap<Class<?>, Parser> parsers;
    public static Parser dayTimeStampParser = (q, c) -> EDayTimeStamp.parse(q);
    public static Parser dayTimeRunParser = (q, c) -> EDayTimeRun.parse(q);
    public static Parser squawkParser = (q, c) -> Squawk.create(q.toCharArray());
    public static Parser coordinateParser = (q, c) -> {
      String[] pts = q.split(";");
      Coordinate ret = new Coordinate(
              Double.parseDouble(pts[0]), Double.parseDouble(pts[1]));
      return ret;
    };

    static {
      parsers = new EMap<>();
      parsers.set(EDayTimeStamp.class, dayTimeStampParser);
      parsers.set(EDayTimeRun.class, dayTimeRunParser);
      parsers.set(Squawk.class, squawkParser);
      parsers.set(Coordinate.class, coordinateParser);
    }
  }

  public static class Deserializers {

    public static IMap<Class<?>, Deserializer> deserializers;

    static {
      deserializers = Parsers.parsers.select(q -> q, q -> q.toDeserializer());
    }
  }

  public static class DeserializersDynamic {
//    public static Deserializer getAtcIdDeserializer(IReadOnlyList<AtcId> atcs) {
//      return new ProxyDeserializer<AtcId>(
//              q -> q.getContent(),
//              q -> q.getName(),
//              atcs
//      );
//    }
  }
}
