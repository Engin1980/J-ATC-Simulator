package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.FlightListTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class FlightListTraffixModelXmlLoader {
  public FlightListTrafficModel load(XElement source) {
    XmlLoaderUtils.setContext(source);

    IList<FlightListTrafficModel.Flight> flights = new EList<>();
    XmlLoaderUtils.loadList(source.getChildren("flight"),
        flights,
        q -> loadFlight(q));

    FlightListTrafficModel ret = new FlightListTrafficModel(flights);
    return ret;
  }

  private FlightListTrafficModel.Flight loadFlight(XElement source) {
    XmlLoaderUtils.setContext(source);
    String timeS = XmlLoaderUtils.loadString("time");
    LocalTime time = LocalTime.parse(
        timeS,
        DateTimeFormatter.ofPattern("HH:mm"));
    String callsignS = XmlLoaderUtils.loadString("callsign");
    Callsign callsign = new Callsign(callsignS);
    MovementTemplate.eKind kind = XmlLoaderUtils.loadEnum("kind", MovementTemplate.eKind.class);
    Integer heading = XmlLoaderUtils.loadInteger("heading", null);
    Coordinate otherAirportCoordinate = XmlLoaderUtils.loadCoordinate("otherAirport", null);
    String airplaneType = XmlLoaderUtils.loadString("planeType", null);
    String follows = XmlLoaderUtils.loadString("follows", null);

    FlightListTrafficModel.Flight ret = new FlightListTrafficModel.Flight(
        callsign, heading, otherAirportCoordinate, kind, airplaneType, time, follows);
    return ret;

  }
}
