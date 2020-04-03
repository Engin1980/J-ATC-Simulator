package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
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

  public static class Flight {
    private final Callsign callsign;
    private final Integer heading;
    private final Coordinate otherAirport;
    private final MovementTemplate.eKind kind;
    private final String planeType;
    private final LocalTime time;
    private final String follows;
    private Flight bindedFollows = null;

    public Flight(Callsign callsign, Integer heading, Coordinate otherAirport, MovementTemplate.eKind kind,
           String planeType, LocalTime time, String follows) {

      EAssert.Argument.isNotNull(callsign, "callsign");
      EAssert.Argument.isTrue(heading != null || otherAirport != null, "Heading or other-airport location must be set.");
      EAssert.Argument.isNonemptyString(planeType, "planeType");
      EAssert.Argument.isNotNull(time, "time");

      this.callsign = callsign;
      this.heading = heading;
      this.otherAirport = otherAirport;
      this.kind = kind;
      this.planeType = planeType;
      this.time = time;
      this.follows = follows;
    }
  }

  private final IList<Flight> flights;

  public FlightListTrafficModel(IList<Flight> flights) {
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
