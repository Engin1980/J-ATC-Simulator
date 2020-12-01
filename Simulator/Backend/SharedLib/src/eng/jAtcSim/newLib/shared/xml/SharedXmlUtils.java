package eng.jAtcSim.newLib.shared.xml;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.time.ITime;
import eng.newXmlUtils.base.Deserializer;
import eng.newXmlUtils.base.Formatter;
import eng.newXmlUtils.base.Parser;
import eng.newXmlUtils.base.Serializer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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
      formatters.set(ApproachType.class, q -> q.toString());
    }
  }

  public static class Serializers {
    public static IMap<Class<?>, Serializer> serializers;

    static {
      serializers = Formatters.formatters.select(q -> q, q -> q.toSerializer());
    }
  }

  public static class Parsers {
    public static final IMap<Class<?>, Parser<?>> parsers;
    public static Parser<ETimeStamp> timeStampParser = (q, c) -> ETimeStamp.parse(q);
    public static Parser<EDayTimeStamp> dayTimeStampParser = (q, c) -> EDayTimeStamp.parse(q);
    public static Parser<EDayTimeRun> dayTimeRunParser = (q, c) -> EDayTimeRun.parse(q);
    public static Parser<Squawk> squawkParser = (q, c) -> Squawk.create(q.toCharArray());
    public static Parser<Coordinate> coordinateParser = (q, c) -> {
      String[] pts = q.split(";");
      NumberFormat nf = new DecimalFormat("00.00000");
      double lat, lng;
      try {
        lat = (double) nf.parse(pts[0]);
        lng = (double) nf.parse(pts[1]);
      } catch (ParseException e) {
        throw new EApplicationException(sf("Failed to parse %s to latitude/longitude coordinate.", q));
      }
      Coordinate ret = new Coordinate(lat, lng);
      return ret;
    };
    public static Parser<Callsign> callsignParser = (q, c) -> new Callsign(q);

    static {
      parsers = new EMap<>();
      parsers.set(EDayTimeStamp.class, dayTimeStampParser);
      parsers.set(EDayTimeRun.class, dayTimeRunParser);
      parsers.set(Squawk.class, squawkParser);
      parsers.set(Coordinate.class, coordinateParser);
      parsers.set(Callsign.class, callsignParser);
      parsers.set(ETimeStamp.class, timeStampParser);
      parsers.set(ApproachType.class, (q, c) -> ApproachType.parse(q));
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
