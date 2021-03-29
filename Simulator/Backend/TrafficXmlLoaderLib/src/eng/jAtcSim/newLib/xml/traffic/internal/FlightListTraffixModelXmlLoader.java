package eng.jAtcSim.newLib.xml.traffic.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.FlightListTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FlightListTraffixModelXmlLoader {
  public FlightListTrafficModel load(XElement source) {

    IList<FlightListTrafficModel.Flight> flights = new EList<>();
    source.getChildren("flight").forEach(q -> {
      FlightListTrafficModel.Flight f = loadFlight(q);
      flights.add(f);
    });

    FlightListTrafficModel ret = new FlightListTrafficModel(flights);
    return ret;
  }

  private FlightListTrafficModel.Flight loadFlight(XElement elm) {
    String callsignS = elm.getAttribute("callsign");
    Callsign callsign = new Callsign(callsignS);

    String timeS = elm.getAttribute("time");
    LocalTime time = LocalTime.parse(
            timeS,
            DateTimeFormatter.ofPattern("H:mm"));

    MovementTemplate.eKind kind = Enum.valueOf(MovementTemplate.eKind.class, elm.getAttribute("kind"));
    Integer heading = elm.hasAttribute("heading") ?
            Integer.parseInt(elm.getAttribute("heading")) : null;

    Coordinate otherAirportCoordinate = SmartXmlLoaderUtils.loadCoordinate(elm, "otherAirport", null);
    String airplaneType = elm.tryGetAttribute("planeType", null);
    String follows = elm.tryGetAttribute("follows", null);
    EAssert.isTrue(airplaneType != null || follows != null,
            sf("Flight '%s' must have type or be following of previous flight.", callsignS));

    FlightListTrafficModel.Flight ret = new FlightListTrafficModel.Flight(
            callsign, heading, otherAirportCoordinate, kind, airplaneType, time, follows);
    return ret;

  }
}
