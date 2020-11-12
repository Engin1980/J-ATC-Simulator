//package eng.jAtcSim.newLib.traffic;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.geo.Coordinate;
//import eng.eSystem.geo.Coordinates;
//import eng.eSystem.utilites.ArrayUtils;
//import eng.eSystem.validation.EAssert;
//
//public class TrafficManager {
//  private IList<Movement> scheduledMovements = new EList<>();
//  @XmlIgnore
//  private TrafficProvider trafficProvider;
//  private Object lastRelativeInfo;
//  private ETime nextGenerateTime = new ETime(0);
//  private TrafficManagerSettings settings;
//  private int offeredMovements = 0;
//  private int createdMovements = 0;
//
//
//
//
//
//  public TrafficManager(TrafficManagerSettings settings, TrafficProvider trafficProvider) {
//    EAssert.Argument.isNotNull(settings, "settings");
//    EAssert.Argument.isNotNull(trafficProvider, "traffic");
//    this.settings = settings;
//    this.trafficProvider = trafficProvider;
//  }
//
//  public void generateNewTrafficIfRequired() {
//    if (Acc.now().isAfterOrEq(nextGenerateTime)) {
//      GeneratedMovementsResponse gmr = trafficProvider.generateMovements(lastRelativeInfo);
//      if (!settings.allowDelays)
//        gmr.getNewMovements().forEach(q -> q.clearDelayMinutes());
//      this.scheduledMovements.add(gmr.getNewMovements());
//      this.scheduledMovements.sort(q -> q.getInitTime()); // Movement has inner class comparer
//      this.nextGenerateTime = gmr.getNextTime();
//      this.lastRelativeInfo = gmr.getSyncObject();
//    }
//  }
//
//  /**
//   * Returns new airplanes antecedent specified time.
//   *
//   * @return New airplanes
//   */
//  public final IReadOnlyList<Airplane> getNewAirplanes() {
//
//    IList<Airplane> ret = new EList<>();
//
//    IList<Movement> readyMovements = scheduledMovements.where(q -> q.getInitTime().isBeforeOrEq(Acc.now()));
//    scheduledMovements.remove(readyMovements);
//    for (Movement readyMovement : readyMovements) {
//      boolean createNewMovement = shouldCreateMovementByDensity();
//      if (!createNewMovement)
//        continue;
//      Airplane a = this.convertMovementToAirplane(readyMovement);
//      if (a == null) {
//        Acc.messenger().send(new Message(Messenger.SYSTEM, Acc.atcApp(),
//            new StringMessageContent("Flight " + readyMovement.getCallsign() + " IFR flight plan canceled, no route.")));
//      } else
//        ret.add(a);
//    }
//
//    // restrict to max planes count
//    while (Acc.planes().size() + ret.size() > this.settings.maxPlanes) {
//      Airplane a = ret.getRandom();
//      ret.remove(a);
//    }
//
//    return ret;
//  }
//
////
////  public void load(XElement root) {
////      XElement trafficElement = root.getChild("trafficManager");
////
////      LoadSave.loadField(trafficElement, this, "scheduledMovements");
////      LoadSave.loadField(trafficElement, this, "lastRelativeInfo");
////      LoadSave.loadField(trafficElement, this, "nextGenerateTime");
////      LoadSave.loadField(trafficElement, this, "settings");
////      LoadSave.loadField(trafficElement, this, "offeredMovements");
////      LoadSave.loadField(trafficElement, this, "createdMovements");
////  }
//
//  private boolean shouldCreateMovementByDensity() {
//    double perc;
//    if (offeredMovements == 0)
//      perc = 0;
//    else
//      perc = createdMovements / (double) offeredMovements;
//    boolean ret = perc <= settings.densityPercentage;
//    offeredMovements++;
//    if (ret)
//      createdMovements++;
//    return ret;
//  }
//
//  public final IReadOnlyList<Movement> getScheduledMovements() {
//    IReadOnlyList<Movement> ret = scheduledMovements;
//    return ret;
//  }
//
//  public void throwOutElapsedMovements(ETime minTime) {
//    scheduledMovements.remove(q -> q.getInitTime().isBefore(minTime));
//  }
//
//  private Airplane convertMovementToAirplane(Movement m) {
//    if (m.isDeparture()) {
//      return generateNewDepartureAirplaneFromMovement(m);
//    } else {
//      return generateNewArrivalPlaneFromMovement(m);
//    }
//  }
//
//  private Airplane generateNewDepartureAirplaneFromMovement(Movement m) {
//    Airplane ret;
//
//    Callsign cs;
//    cs = m.getCallsign();
//    AirplaneType pt = m.getAirplaneType();
//
//    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryRadial(), false, pt);
//    if (entryPoint == null) return null; // no route means disallowed IFR
//    Coordinate coord = Acc.airport().getLocation();
//    eng.jAtcSim.newLib.shared.Squawk sqwk = generateSqwk();
//
//    int heading = 0;
//    int alt = Acc.airport().getAltitude();
//    int spd = 0;
//
//    ret = new Airplane(
//        cs, coord, sqwk, pt, heading, alt, spd, true,
//        entryPoint.getNavaid(), m.getDelayInMinutes(), m.getInitTime().addMinutes(3));
//
//    return ret;
//  }
//
//  private EntryExitPoint tryGetRandomEntryPoint(int entryRadial, boolean isArrival, AirplaneType pt) {
//    IReadOnlyList<EntryExitPoint> tmp = Acc.airport().getEntryExitPoints();
//    if (isArrival)
//      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.entry || q.getType() == EntryExitPoint.Type.both);
//    else
//      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.exit || q.getType() == EntryExitPoint.Type.both);
//    tmp = tmp.where(q -> q.getMaxMrvaAltitudeOrHigh() < pt.maxAltitude);
//
//    assert !tmp.isEmpty() : "There are no avilable entry/exit points for plane kind " + pt.name + " with service ceiling at " + pt.maxAltitude;
//
//    EntryExitPoint ret = tmp.getSmallest(q -> Headings.getDifference(entryRadial, q.getRadialFromAirport(), true));
//
//    return ret;
//  }
//
//  private Airplane generateNewArrivalPlaneFromMovement(Movement m) {
//    Airplane ret;
//
//    Callsign cs;
//    cs = m.getCallsign();
//
//    AirplaneType pt = m.getAirplaneType();
//
//    Coordinate coord;
//    int heading;
//    int alt;
//    int spd;
//
//    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryRadial(), true, pt);
//    if (entryPoint == null) {
//      return null; // no route means disallowed IFR
//    }
//    coord = generateArrivalCoordinate(entryPoint.getNavaid().getCoordinate(), Acc.airport().getLocation());
//    heading = (int) Coordinates.getBearing(coord, entryPoint.getNavaid().getCoordinate());
//    alt = generateArrivingPlaneAltitude(entryPoint, coord, pt);
//
//    eng.jAtcSim.newLib.shared.Squawk sqwk = generateSqwk();
//    spd = pt.vCruise;
//
//    ret = new Airplane(
//        cs, coord, sqwk, pt, heading, alt, spd, false,
//        entryPoint.getNavaid(), m.getDelayInMinutes(), m.getInitTime().addMinutes(25));
//
//    return ret;
//  }
//
//  private int generateArrivingPlaneAltitude(EntryExitPoint eep, Coordinate planeCoordinate, AirplaneType type) {
//
//    int ret;
//
//    // min alt by mrva
//    ret = eep.getMaxMrvaAltitudeOrHigh();
//
//    // update by distance
//    {
//      final double thousandsFeetPerMile = 500;
//      final double distance = Coordinates.getDistanceInNM(Acc.airport().getLocation(), eep.getNavaid().getCoordinate())
//          + Coordinates.getDistanceInNM(eep.getNavaid().getCoordinate(), planeCoordinate);
//      int tmp = (int) (distance * thousandsFeetPerMile);
//      ret = Math.max(ret, tmp);
//    }
//
//    // update by random value
//    ret += Acc.rnd().nextInt(-3000, 5000);
//    if (ret > type.maxAltitude) {
//      if (ret < 12000)
//        ret = type.maxAltitude - Acc.rnd().nextInt(4) * 1000;
//      else if (ret < 20000)
//        ret = type.maxAltitude - Acc.rnd().nextInt(7) * 1000;
//      else
//        ret = type.maxAltitude - Acc.rnd().nextInt(11) * 1000;
//    }
//    ret = ret / 1000 * 1000;
//
//    // check if initial altitude is not below STAR mrva
//    if (ret < eep.getMaxMrvaAltitudeOrHigh()) {
//      double tmp = Math.ceil(eep.getMaxMrvaAltitudeOrHigh() / 10d) * 10;
//      ret = (int) tmp;
//    }
//
//    return ret;
//  }
//
//  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
//    double radial = Coordinates.getBearing(aipFix, navFix);
//    radial += Simulation.rnd.nextDouble(-15, 15); // nahodne zatoceni priletoveho radialu
//    double dist =  Coordinates.getDistanceInNM(navFix, Acc.airport().getLocation());
//    if (dist > (Acc.airport().getCoveredDistance())){
//      dist = Simulation.rnd.nextDouble(25, 40);
//    } else {
//      dist = Acc.airport().getCoveredDistance() - dist;
//      if (dist < 25) dist = Simulation.rnd.nextDouble(25,40);
//    }
//    Coordinate ret = Coordinates.getCoordinate(navFix, (int) radial, dist);
//    return ret;
//  }
//
//  private eng.jAtcSim.newLib.shared.Squawk generateSqwk() {
//    String[] illegals = new String[]{
//        "7500", "7600", "7700"
//    };
//
//    int len = 4;
//    char[] tmp;
//    eng.jAtcSim.newLib.shared.Squawk ret = null;
//    while (ret == null) {
//      tmp = new char[len];
//      for (int i = 0; i < len; i++) {
//        tmp[i] = Integer.toString(Simulation.rnd.nextInt(8)).charAt(0);
//      }
//      if (ArrayUtils.contains(illegals, tmp.toString()))
//        continue;
//      ret = eng.jAtcSim.newLib.shared.Squawk.create(tmp);
//      for (Airplane p : Acc.planes()) {
//        if (p.getSqwk().equals(ret)) {
//          ret = null;
//          break;
//        }
//      }
//    }
//    return ret;
//  }
//}
