package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.FlightListTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlightListTraffixModelXmlLoader {
  public FlightListTrafficModel load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);

    IList<FlightListTrafficModel.Flight> flights = new EList<>();
    SmartXmlLoaderUtils.loadList(source.getChildren("flight"),
        flights,
        q -> loadFlight(q));

    FlightListTrafficModel ret = new FlightListTrafficModel(flights);
    return ret;
  }

  private FlightListTrafficModel.Flight loadFlight(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String timeS = SmartXmlLoaderUtils.loadString("time");
    LocalTime time = LocalTime.parse(
        timeS,
        DateTimeFormatter.ofPattern("HH:mm"));
    String callsignS = SmartXmlLoaderUtils.loadString("callsign");
    Callsign callsign = new Callsign(callsignS);
    MovementTemplate.eKind kind = SmartXmlLoaderUtils.loadEnum("kind", MovementTemplate.eKind.class);
    Integer heading = SmartXmlLoaderUtils.loadInteger("heading", null);
    Coordinate otherAirportCoordinate = SmartXmlLoaderUtils.loadCoordinate("otherAirport", null);
    String airplaneType = SmartXmlLoaderUtils.loadString("planeType", null);
    String follows = SmartXmlLoaderUtils.loadString("follows", null);

    FlightListTrafficModel.Flight ret = new FlightListTrafficModel.Flight(
        callsign, heading, otherAirportCoordinate, kind, airplaneType, time, follows);
    return ret;

  }
}
