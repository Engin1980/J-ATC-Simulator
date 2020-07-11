package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.xml.IXmlLogable;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.FlightListTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlightListTraffixModelXmlLoader implements IXmlLogable {
  public FlightListTrafficModel load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    log(0, "Xml-loading flight list traffic");

    IList<FlightListTrafficModel.Flight> flights = new EList<>();
    SmartXmlLoaderUtils.loadList(source.getChildren("flight"),
        flights,
        q -> loadFlight(q));

    FlightListTrafficModel ret = new FlightListTrafficModel(flights);
    return ret;
  }

  private FlightListTrafficModel.Flight loadFlight(XElement source) {
    log(1, "loading flight");
    SmartXmlLoaderUtils.setContext(source);
    String callsignS = SmartXmlLoaderUtils.loadString("callsign");
    log(1, "... flight '%s'", callsignS);
    String timeS = SmartXmlLoaderUtils.loadString("time");
    LocalTime time = LocalTime.parse(
        timeS,
        DateTimeFormatter.ofPattern("H:mm"));
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
