package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.EDistinctList;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.annotations.XmlAttribute;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.eSystem.xmlSerialization.common.parsers.JavaTimeLocalTimeValueParser;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.DataFormat;
import eng.jAtcSim.lib.global.ETime;

public class FlightListTraffic extends Traffic {

  private static final int EMPTY_HEADING = -1;

  public static class Flight {
    protected String callsign;
    @XmlIgnore
    protected Callsign bindedCallsign;
    @XmlIgnore
    protected Flight bindedFollows;
    @XmlOptional
    private int heading = EMPTY_HEADING;
    @XmlOptional
    private Coordinate otherAirport = null;
    private eKind kind;
    private String planeType;
    @XmlAttribute(parser = JavaTimeLocalTimeValueParser.class)
    private java.time.LocalTime time;
    @XmlOptional
    private String follows = null;

    public boolean isArrival() {
      return kind == eKind.arrival;
    }

    public boolean isCommercial() {
      return Character.isDigit(bindedCallsign.getNumber().charAt(0)) == false;
    }

    public Movement toMovement() {
      AirplaneType type = Acc.types().tryGetByName(this.planeType);
      if (type == null)
        throw new EApplicationException("Unable to create flight. Required airplane type '" + this.planeType + "' not found.");

      if (this.heading == EMPTY_HEADING){
        double radial = Coordinates.getBearing(Acc.airport().getLocation(), this.otherAirport);
        this.heading = (int) radial;
      }

      ETime initTime;
      if (this.isArrival())
        initTime = new ETime(this.time.plusMinutes(-25));
      else
        initTime = new ETime(this.time);

      Movement ret = new Movement(
          this.bindedCallsign, type,
          initTime,
          0,
          !this.isArrival(),
          this.heading);
      return ret;
    }
  }

  private enum eKind {
    departure,
    arrival
  }

  @XmlItemElement(elementName = "flight", type = Flight.class)
  private IList<Flight> flights = new EList<>();

  public void bind() {
    for (Flight flight : flights) {
      try {
        flight.bindedCallsign = new Callsign(flight.callsign);
      } catch (Exception ex) {
        throw new EApplicationException("Unable to create a callsign from " + flight.callsign + ".", ex);
      }

      if (flight.heading == EMPTY_HEADING && flight.otherAirport == null)
        throw new EApplicationException("Flight " + flight.callsign + " has neither heading nor other-airport coordinate.");

      if (flight.follows != null)
        try {
          flight.bindedFollows = flights.getFirst(q -> q.callsign.equals(flight.follows));
        } catch (Exception ex) {
          throw new EApplicationException("Unable to find previous flight with the callsign " + flight.follows + ".", ex);
        }
    }
  }

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    GeneratedMovementsResponse ret;
    IList<Movement> movements;
    Integer nextHour = (Integer) syncObject;
    if (nextHour == null) {
      movements = new EList<>();
      movements.add(
          generateNewMovements(Acc.now().getHours())
      );
      movements.add(
          generateNewMovements(Acc.now().getHours() + 1)
      );
      nextHour = Acc.now().getHours() + 1;
    } else {
      nextHour++;
      movements = generateNewMovements(nextHour);
    }

    ret = new GeneratedMovementsResponse(Acc.now().getRoundedToNextHour(), nextHour, movements);
    return ret;
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    IList<ExpectedMovement> ret = new EList<>();

    for (Flight flight : flights) {
      ExpectedMovement em = new ExpectedMovement(
          new ETime(flight.time),
          flight.isArrival(),
          flight.isCommercial(),
          'C');
      ret.add(em);
    }

    return ret;
  }

  public IList<String> getRequiredPlaneTypes() {
    IList<String> ret = new EDistinctList<>(EDistinctList.Behavior.skip);

    for (Flight flight : flights) {
      ret.add(flight.planeType);
    }

    return ret;
  }

  private IList<Movement> generateNewMovements(int hours) {
    IList<Flight> flights = this.flights.where(q -> q.time.getHour() == hours);
    IList<Movement> ret = new EList<>();
    for (Flight flight : flights) {
      Movement m = flight.toMovement();
      ret.add(m);
    }
    return ret;
  }
}
