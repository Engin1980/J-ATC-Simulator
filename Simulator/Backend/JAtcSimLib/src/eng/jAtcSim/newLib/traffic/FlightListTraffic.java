package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.airplanes.Callsign;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.world.xml.XmlLoader;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FlightListTraffic extends Traffic {

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
      java.time.LocalTime time = java.time.LocalTime.parse(
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
    protected Flight bindedFollows = null;
    private final int heading;
    private final Coordinate otherAirport;
    private final eKind kind;
    private final String planeType;
    private final java.time.LocalTime time;
    private final String follows;

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

    public boolean isArrival() {
      return kind == eKind.arrival;
    }

    public boolean isCommercial() {
      return Character.isDigit(callsign.getNumber().charAt(0)) == false;
    }

    public Movement toMovement() {
      AirplaneType type = Acc.types().tryGetByName(this.planeType);
      if (type == null)
        throw new EApplicationException("Unable to create flight. Required airplane kind '" + this.planeType + "' not found.");

      int currentHeading;
      if (this.heading == EMPTY_HEADING) {
        double radial = Coordinates.getBearing(Acc.airport().getLocation(), this.otherAirport);
        currentHeading = (int) radial;
      } else
        currentHeading = this.heading;

      ETime initTime;
      if (this.isArrival())
        initTime = new ETime(this.time.plusMinutes(-25));
      else
        initTime = new ETime(this.time);

      Movement ret = new Movement(
          this.callsign, type,
          initTime,
          0,
          !this.isArrival(),
          currentHeading);
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

  public FlightListTraffic(double delayProbability, int maxDelayInMinutesPerStep, IList<Flight> flights) {
    super(delayProbability, maxDelayInMinutesPerStep);
    this.flights = flights;
    bind();
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
    IList<String> ret = this.flights.select(q->q.planeType).distinct();
    return ret;
  }

  private void bind() {
    for (Flight flight : flights) {

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
