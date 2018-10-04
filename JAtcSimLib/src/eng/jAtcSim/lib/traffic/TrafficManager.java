package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Squawk;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.messaging.Messenger;
import eng.jAtcSim.lib.messaging.StringMessageContent;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.world.*;

public class TrafficManager {
  private IList<Movement> scheduledMovements = new EList<>();
  @XmlIgnore
  private Traffic traffic;
  private Object lastRelativeInfo;
  private ETime nextGenerateTime = new ETime(0);
  private TrafficManagerSettings settings;
  private int offeredMovements = 0;
  private int createdMovements = 0;

  public static class TrafficManagerSettings {
    public final boolean allowDelays;
    public final int maxPlanes;
    public final double densityPercentage;

    public TrafficManagerSettings(boolean allowDelays, int maxPlanes, double densityPercentage) {
      this.allowDelays = allowDelays;
      this.maxPlanes = maxPlanes;
      this.densityPercentage = densityPercentage;
    }
  }

  @XmlConstructor
  private TrafficManager() {
  }

  public TrafficManager(TrafficManagerSettings settings, Traffic traffic) {
    if (settings == null) {
      throw new IllegalArgumentException("Value of {settings} cannot not be null.");
    }
    if (traffic == null) {
        throw new IllegalArgumentException("Value of {traffic} cannot not be null.");
    }

    this.settings = settings;
    this.traffic = traffic;
  }

  public void generateNewTrafficIfRequired() {
    if (Acc.now().isAfterOrEq(nextGenerateTime)) {
      GeneratedMovementsResponse gmr = traffic.generateMovements(lastRelativeInfo);
      if (!settings.allowDelays)
        gmr.getNewMovements().forEach(q -> q.clearDelayMinutes());
      this.scheduledMovements.add(gmr.getNewMovements());
      this.scheduledMovements.sort(q -> q.getInitTime()); // Movement has inner class comparer
      this.nextGenerateTime = gmr.getNextTime();
      this.lastRelativeInfo = gmr.getSyncObject();
    }
  }

  /**
   * Returns new airplanes antecedent specified time.
   *
   * @return New airplanes
   */
  public final IReadOnlyList<Airplane> getNewAirplanes() {

    IList<Airplane> ret = new EList<>();

    IList<Movement> readyMovements = scheduledMovements.where(q -> q.getInitTime().isBeforeOrEq(Acc.now()));
    scheduledMovements.remove(readyMovements);
    for (Movement readyMovement : readyMovements) {
      boolean createNewMovement = shouldCreateMovementByDensity();
      if (!createNewMovement)
        continue;
      Airplane a = this.convertMovementToAirplane(readyMovement);
      if (a == null) {
        Acc.messenger().send(new Message(Messenger.SYSTEM, Acc.atcApp(),
            new StringMessageContent("Flight " + readyMovement.getCallsign() + " IFR flight plan canceled, no route.")));
      } else
        ret.add(a);
    }

    // restrict to max planes count
    while (Acc.planes().size() + ret.size() > this.settings.maxPlanes) {
      Airplane a = ret.getRandom();
      ret.remove(a);
    }

    return ret;
  }

  public void save(XElement root) {
    XElement trafficElement = new XElement("trafficManager");

    LoadSave.saveField(trafficElement, this, "scheduledMovements");
    LoadSave.saveField(trafficElement, this, "lastRelativeInfo");
    LoadSave.saveField(trafficElement, this, "nextGenerateTime");
    LoadSave.saveField(trafficElement, this, "settings");
    LoadSave.saveField(trafficElement, this, "offeredMovements");
    LoadSave.saveField(trafficElement, this, "createdMovements");

    root.addElement(trafficElement);
  }

  public void load(XElement root) {
      XElement trafficElement = root.getChild("trafficManager");

      LoadSave.loadField(trafficElement, this, "scheduledMovements");
      LoadSave.loadField(trafficElement, this, "lastRelativeInfo");
      LoadSave.loadField(trafficElement, this, "nextGenerateTime");
      LoadSave.loadField(trafficElement, this, "settings");
      LoadSave.loadField(trafficElement, this, "offeredMovements");
      LoadSave.loadField(trafficElement, this, "createdMovements");
  }

  private boolean shouldCreateMovementByDensity() {
    double perc;
    if (offeredMovements == 0)
      perc = 0;
    else
      perc = createdMovements / (double) offeredMovements;
    boolean ret = perc <= settings.densityPercentage;
    offeredMovements++;
    if (ret)
      createdMovements++;
    return ret;
  }

  public final IReadOnlyList<Movement> getScheduledMovements() {
    IReadOnlyList<Movement> ret = scheduledMovements;
    return ret;
  }

  public void throwOutElapsedMovements(ETime minTime) {
    scheduledMovements.remove(q -> q.getInitTime().isBefore(minTime));
  }

  private Airplane convertMovementToAirplane(Movement m) {
    if (m.isDeparture()) {
      return generateNewDepartureAirplaneFromMovement(m);
    } else {
      return generateNewArrivalPlaneFromMovement(m);
    }
  }

  private Airplane generateNewDepartureAirplaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();
    AirplaneType pt = m.getAirplaneType();

    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryRadial(), false, pt);
    if (entryPoint == null) return null; // no route means disallowed IFR
    Coordinate coord = Acc.airport().getLocation();
    Squawk sqwk = generateSqwk();

    int heading = 0;
    int alt = Acc.airport().getAltitude();
    int spd = 0;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, true,
        entryPoint.getNavaid(), m.getDelayInMinutes(), m.getInitTime().addMinutes(3));

    return ret;
  }

  private EntryExitPoint tryGetRandomEntryPoint(int entryRadial, boolean isArrival, AirplaneType pt) {
    IReadOnlyList<EntryExitPoint> tmp = Acc.airport().getEntryExitPoints();
    if (isArrival)
      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.entry || q.getType() == EntryExitPoint.Type.both);
    else
      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.exit || q.getType() == EntryExitPoint.Type.both);
    tmp = tmp.where(q -> q.getMaxMrvaAltitudeOrHigh() < pt.maxAltitude);

    assert !tmp.isEmpty() : "There are no avilable entry/exit points for plane type " + pt.name + " with service ceiling at " + pt.maxAltitude;

    EntryExitPoint ret = tmp.getSmallest(q -> Headings.getDifference(entryRadial, q.getRadialFromAirport(), true));

    return ret;
  }

  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
    Airplane ret;

    Callsign cs;
    cs = m.getCallsign();

    AirplaneType pt = m.getAirplaneType();

    Coordinate coord;
    int heading;
    int alt;
    int spd;

    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryRadial(), true, pt);
    if (entryPoint == null) {
      return null; // no route means disallowed IFR
    }
    coord = generateArrivalCoordinate(entryPoint.getNavaid().getCoordinate(), Acc.airport().getLocation());
    heading = (int) Coordinates.getBearing(coord, entryPoint.getNavaid().getCoordinate());
    alt = generateArrivingPlaneAltitude(entryPoint, coord, pt);

    Squawk sqwk = generateSqwk();
    spd = pt.vCruise;

    ret = new Airplane(
        cs, coord, sqwk, pt, heading, alt, spd, false,
        entryPoint.getNavaid(), m.getDelayInMinutes(), m.getInitTime().addMinutes(25));

    return ret;
  }

  private int generateArrivingPlaneAltitude(EntryExitPoint eep, Coordinate planeCoordinate, AirplaneType type) {

    int ret;

    // min alt by mrva
    ret = eep.getMaxMrvaAltitudeOrHigh();

    // update by distance
    {
      final double thousandsFeetPerMile = 500;
      final double distance = Coordinates.getDistanceInNM(Acc.airport().getLocation(), eep.getNavaid().getCoordinate())
          + Coordinates.getDistanceInNM(eep.getNavaid().getCoordinate(), planeCoordinate);
      int tmp = (int) (distance * thousandsFeetPerMile);
      ret = Math.max(ret, tmp);
    }

    // update by random value
    ret += Acc.rnd().nextInt(-3000, 5000);
    if (ret > type.maxAltitude) {
      if (ret < 12000)
        ret = type.maxAltitude - Acc.rnd().nextInt(4) * 1000;
      else if (ret < 20000)
        ret = type.maxAltitude - Acc.rnd().nextInt(7) * 1000;
      else
        ret = type.maxAltitude - Acc.rnd().nextInt(11) * 1000;
    }
    ret = ret / 1000 * 1000;

    // check if initial altitude is not below STAR mrva
    if (ret < eep.getMaxMrvaAltitudeOrHigh()) {
      double tmp = Math.ceil(eep.getMaxMrvaAltitudeOrHigh() / 10d) * 10;
      ret = (int) tmp;
    }

    return ret;
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Simulation.rnd.nextDouble(-15, 15); // nahodne zatoceni priletoveho radialu
    double dist = Acc.airport().getCoveredDistance();
    Coordinate ret = Coordinates.getCoordinate(Acc.airport().getLocation(), (int) radial, dist);
    return ret;
  }

  private Squawk generateSqwk() {
    String[] illegals = new String[]{
        "7500", "7600", "7700"
    };

    int len = 4;
    char[] tmp;
    Squawk ret = null;
    while (ret == null) {
      tmp = new char[len];
      for (int i = 0; i < len; i++) {
        tmp[i] = Integer.toString(Simulation.rnd.nextInt(8)).charAt(0);
      }
      if (ArrayUtils.contains(illegals, tmp.toString()))
        continue;
      ret = Squawk.create(tmp);
      for (Airplane p : Acc.planes()) {
        if (p.getSqwk().equals(ret)) {
          ret = null;
          break;
        }
      }
    }
    return ret;
  }
}
