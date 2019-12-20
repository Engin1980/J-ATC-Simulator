package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.timeOld.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FlightListTraffic extends TrafficOld {

  public static class Flight {
    public static IList<Flight> loadList(IReadOnlyList<XElement> sources) {
      IList<Flight> ret = new EList<>();

      for (XElement source : sources) {
        Flight f = Flight.load(source);
        ret.add(f);
      }

      return ret;
    }

    private static Flight load(XElement source) {
      XmlLoader.setContext(source);
      String timeS = XmlLoader.loadString("time");
      LocalTime time = LocalTime.parse(
          timeS,
          DateTimeFormatter.ofPattern("HH:mm"));
      String callsignS = XmlLoader.loadString("callsign");
      Callsign callsign = new Callsign(callsignS);
      eKind kind = XmlLoader.loadEnum("kind", eKind.class);
      int heading = XmlLoader.loadInteger("heading", EMPTY_HEADING);
      Coordinate otherAirportCoordinate = XmlLoader.loadCoordinate("otherAirport", null);
      String airplaneType = XmlLoader.loadString("planeType", null);
      String follows = XmlLoader.loadString("follows", null);

      Flight ret = new Flight(
          callsign, heading, otherAirportCoordinate, kind, airplaneType, time, follows);
      return ret;

    }

    protected final Callsign callsign;
    private final int heading;
    private final Coordinate otherAirport;
    private final eKind kind;
    private final String planeType;
    private final LocalTime time;
    private final String follows;
    protected Flight bindedFollows = null;

    public Flight(Callsign callsign, int heading, Coordinate otherAirport, eKind kind,
                  String planeType, LocalTime time, String follows) {
      this.callsign = callsign;
      this.heading = heading;
      this.otherAirport = otherAirport;
      this.kind = kind;
      this.planeType = planeType;
      this.time = time;
      this.follows = follows;
    }

    public ETimeStamp getTimeStamp() {
      return new ETimeStamp(this.time);
    }

    public boolean isArrival() {
      return kind == eKind.arrival;
    }

    public boolean isCommercial() {
      return Character.isDigit(callsign.getNumber().charAt(0)) == false;
    }

    public MovementTemplate toMovement() {
      MovementTemplate ret = new FlightMovementTemplate(
          this.callsign,
          this.planeType,
          this.isArrival() ? MovementTemplate.eKind.arrival : MovementTemplate.eKind.departure,
          new ETimeStamp(this.time),
          0, // delay
          new EntryExitInfo(this.otherAirport));
      return ret;
    }
  }

  private enum eKind {
    departure,
    arrival
  }

  private static final int EMPTY_HEADING = -1;

  public static FlightListTraffic load(XElement source) {
    XmlLoader.setContext(source);
    double delayProbability = XmlLoader.loadDouble("delayProbability");
    int maxDelayInMinutesPerStep = XmlLoader.loadInteger("maxDelayInMinutesPerStep");

    IList<Flight> flights = Flight.loadList(source.getChildren("flight"));

    FlightListTraffic ret = new FlightListTraffic(delayProbability, maxDelayInMinutesPerStep, flights);
    return ret;
  }

  private final IList<Flight> flights;

  private FlightListTraffic(double delayProbability, int maxDelayInMinutesPerStep, IList<Flight> flights) {
    super(delayProbability, maxDelayInMinutesPerStep);
    this.flights = flights;
    bind();
  }

//  @Override
//  public GeneratedMovementsResponse generateMovements(Object syncObject) {
//    GeneratedMovementsResponse ret;
//    IList<Movement> movements;
//    Integer nextHour = (Integer) syncObject;
//    if (nextHour == null) {
//      movements = new EList<>();
//      movements.add(
//          generateNewMovements(SharedFactory.getNow().getHours())
//      );
//      movements.add(
//          generateNewMovements(SharedFactory.getNow().getHours() + 1)
//      );
//      nextHour = SharedFactory.getNow().getHours() + 1;
//    } else {
//      nextHour++;
//      movements = generateNewMovements(nextHour);
//    }
//
//    ret = new GeneratedMovementsResponse(SharedFactory.getNow().getRoundedToNextHour(), nextHour, movements);
//    return ret;
//  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    IList<ExpectedMovement> ret = new EList<>();

    for (Flight flight : flights) {
      ExpectedMovement em = new ExpectedMovement(
          new ETimeStamp(flight.time),
          flight.isArrival(),
          flight.isCommercial(),
          'C');
      ret.add(em);
    }

    return ret;
  }

  @Override
  public IReadOnlyList<MovementTemplate> getMovements(ETimeStamp fromTimeInclusive, ETimeStamp toTimeExclusive) {
    IList<MovementTemplate> ret = this.flights
        .where(q -> q.getTimeStamp().isAfterOrEq(fromTimeInclusive) &&
            q.getTimeStamp().isBefore(toTimeExclusive))
        .select(q -> q.toMovement());
    return ret;
  }

  public IList<String> getRequiredPlaneTypes() {
    IList<String> ret = this.flights.select(q -> q.planeType).distinct();
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
}
