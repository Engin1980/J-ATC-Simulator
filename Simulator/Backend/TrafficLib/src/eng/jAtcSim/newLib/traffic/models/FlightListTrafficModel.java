package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.traffic.models.base.ITrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlightListTrafficModel implements ITrafficModel {

  private static class Flight {

    private static Flight load(XElement source) {
      XmlLoaderUtils.setContext(source);
      String timeS = XmlLoaderUtils.loadString("time");
      LocalTime time = LocalTime.parse(
          timeS,
          DateTimeFormatter.ofPattern("HH:mm"));
      String callsignS = XmlLoaderUtils.loadString("callsign");
      Callsign callsign = new Callsign(callsignS);
      MovementTemplate.eKind kind = XmlLoaderUtils.loadEnum("kind", MovementTemplate.eKind.class);
      int heading = XmlLoaderUtils.loadInteger("heading", EMPTY_HEADING);
      Coordinate otherAirportCoordinate = XmlLoaderUtils.loadCoordinate("otherAirport", null);
      String airplaneType = XmlLoaderUtils.loadString("planeType", null);
      String follows = XmlLoaderUtils.loadString("follows", null);

      Flight ret = new Flight(
          callsign, heading, otherAirportCoordinate, kind, airplaneType, time, follows);
      return ret;

    }

    private final Callsign callsign;
    private final int heading;
    private final Coordinate otherAirport;
    private final MovementTemplate.eKind kind;
    private final String planeType;
    private final LocalTime time;
    private final String follows;
    private Flight bindedFollows = null;

    Flight(Callsign callsign, int heading, Coordinate otherAirport, MovementTemplate.eKind kind,
           String planeType, LocalTime time, String follows) {
      this.callsign = callsign;
      this.heading = heading;
      this.otherAirport = otherAirport;
      this.kind = kind;
      this.planeType = planeType;
      this.time = time;
      this.follows = follows;
    }
  }

  private static final int EMPTY_HEADING = -1;

  public static FlightListTrafficModel load(XElement source) {
    XmlLoaderUtils.setContext(source);

    IList<Flight> flights = new EList<>();
    XmlLoaderUtils.loadList(source.getChildren("flight"),
        flights,
        q -> Flight.load(q));

    FlightListTrafficModel ret = new FlightListTrafficModel(flights);
    return ret;
  }

  private final IList<Flight> flights;

  private FlightListTrafficModel(IList<Flight> flights) {
    this.flights = flights;
    bind();
  }

  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    //TODO not working with binded flights!!!
    IList<MovementTemplate> ret = flights.select(q -> convertFlightToMovementTemplate(q));
    return ret;
  }


  private void bind() {
    for (Flight flight : flights) {

      if (flight.heading == EMPTY_HEADING && flight.otherAirport == null)
        throw new EApplicationException("Flight " + flight.callsign + " has neither heading nor other-airport coordinate.");

      if (flight.follows != null)
        try {
          flight.bindedFollows = flights.getFirst(q -> q.callsign.toString(false).equals(flight.follows));
        } catch (Exception ex) {
          throw new EApplicationException("Unable to find previous flight with the callsign " + flight.follows + ".", ex);
        }
    }
  }

  private MovementTemplate convertFlightToMovementTemplate(Flight flight) {
    FlightMovementTemplate ret = new FlightMovementTemplate(
        flight.callsign,
        flight.planeType,
        flight.kind,
        new ETimeStamp(flight.time),
        new EntryExitInfo(flight.otherAirport));
    return ret;
  }
}
