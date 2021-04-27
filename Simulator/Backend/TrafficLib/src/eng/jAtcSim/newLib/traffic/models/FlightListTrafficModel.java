package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FlightListTrafficModel implements ITrafficModel {

  public static class Flight {
    private Flight bindedFollows = null;
    private final Callsign callsign;
    private final String follows;
    private final Integer heading;
    private final MovementTemplate.eKind kind;
    private final Coordinate otherAirport;
    private final String planeType;
    private final LocalTime time;

    public Flight(Callsign callsign, Integer heading, Coordinate otherAirport, MovementTemplate.eKind kind,
                  String planeType, LocalTime time, String follows) {

      EAssert.Argument.isNotNull(callsign, "callsign");
      EAssert.Argument.isTrue(heading != null || otherAirport != null,
              sf("Heading or other-airport location must be set for '%s'.", callsign));
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

  public IReadOnlyList<String> getRequiredPlaneTypes() {
    IList<String> ret = flights.select(q -> q.planeType).distinct();
    return ret;
  }

  private void bind() {
    for (Flight flight : flights) {
      if (flight.follows != null)
        try {
          flight.bindedFollows = flights.getFirst(q -> q.callsign.toString(false).equals(flight.follows));
        } catch (Exception ex) {
          throw new ApplicationException("Unable to find previous flight with the callsign " + flight.follows + ".", ex);
        }
    }
  }

  private MovementTemplate convertFlightToMovementTemplate(Flight flight) {
    EntryExitInfo eei = flight.otherAirport != null ?
            new EntryExitInfo(flight.otherAirport) : new EntryExitInfo(flight.heading);
    FlightMovementTemplate ret = new FlightMovementTemplate(
            flight.callsign,
            flight.planeType,
            flight.kind,
            new ETimeStamp(flight.time.getHour(), flight.time.getMinute(), flight.time.getSecond()),
            eei);
    return ret;
  }
}
