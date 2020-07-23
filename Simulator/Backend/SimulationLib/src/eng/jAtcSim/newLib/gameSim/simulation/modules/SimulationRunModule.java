package eng.jAtcSim.newLib.gameSim.simulation.modules;

import eng.eSystem.ERandom;
import eng.eSystem.TryResult;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.CountryFleet;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.ISimulationModuleParent;
import eng.jAtcSim.newLib.gameSim.simulation.modules.base.SimModule;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.CallsignFactory;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.FinishedPlaneStats;
import eng.jAtcSim.newLib.traffic.movementTemplating.*;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SimulationRunModule extends SimModule {
  private static final int MAX_ALLOWED_DELAY = 120;
  private final CallsignFactory callsignFactory;
  private final int delayStep;
  private final double delayStepProbability;

  public SimulationRunModule(ISimulationModuleParent parent, double delayStepProbability, int delayStep, boolean useExtendedCallsigns) {
    super(parent);
    this.delayStepProbability = delayStepProbability;
    this.delayStep = delayStep;
    this.callsignFactory = new CallsignFactory(useExtendedCallsigns);
  }

  public void elapseSecond() {
    introduceNewPlanes();
    removeOldPlanes();
    generateEmergencyIfRequired();
    updatePlanes();
    evaluateFails();
  }

  private FinishedPlaneStats buildFinishedAirplaneStats(IAirplane airplane) {
    int entryDelay = airplane.getFlight().getEntryDelay();
    int exitDelay = airplane.getFlight().getExitDelay();
    int delayDifference = exitDelay - entryDelay;
    FinishedPlaneStats ret = new FinishedPlaneStats(
        airplane.getCallsign(), airplane.isDeparture(), airplane.isEmergency(), delayDifference,
        Context.getMood().getMoodManager().getMoodResult(airplane.getCallsign(), delayDifference)
    );
    return ret;
  }

  private FlightMovementTemplate convertGenericMovementTemplateToFlightMovementTemplate(MovementTemplate m) {
    FlightMovementTemplate ret;
    EAssert.Argument.isTrue(m instanceof GenericCommercialMovementTemplate || m instanceof GenericGeneralAviationMovementTemplate);

    Callsign callsign;
    String airplaneTypeName;

    if (m instanceof GenericCommercialMovementTemplate) {
      GenericCommercialMovementTemplate gcmt = (GenericCommercialMovementTemplate) m;

      CompanyFleet companyFleet = parent.getContext().getAirlinesFleets().tryGetByIcaoOrDefault(gcmt.getCompanyIcao());
      callsign = this.callsignFactory.generateCommercial(companyFleet.getIcao());
      IList<TypeAndWeight> availableTypes = companyFleet.getTypes()
          .where(q -> parent.getContext().getAirplaneTypes()
              .getTypeNames()
              .contains(q.getTypeName()));
      if (availableTypes.isEmpty())
        airplaneTypeName = "N/A";
      else
        airplaneTypeName = availableTypes.getRandomByWeights(
            q -> (double) q.getWeight()).getTypeName();

    } else if (m instanceof GenericGeneralAviationMovementTemplate) {
      GenericGeneralAviationMovementTemplate ggamt = (GenericGeneralAviationMovementTemplate) m;

      CountryFleet countryFleet = parent.getContext().getGaFleets().tryGetByIcaoOrDefault(ggamt.getCountryIcao());
      callsign = this.callsignFactory.generateGeneralAviation(countryFleet.getAircraftPrefix());
      IList<TypeAndWeight> availableTypes = countryFleet.getTypes()
          .where(q -> parent.getContext().getAirplaneTypes()
              .getTypeNames()
              .contains(q.getTypeName()));
      if (availableTypes.isEmpty())
        airplaneTypeName = "N/A";
      else
        airplaneTypeName = availableTypes.getRandomByWeights(
            q -> (double) q.getWeight()).getTypeName();

    } else
      throw new UnsupportedOperationException();

    ret = new FlightMovementTemplate(
        callsign, airplaneTypeName, m.getKind(), m.getAppearanceTime(), m.getEntryExitInfo());

    return ret;
  }

  private TryResult<AirplaneTemplate> convertMovementToAirplane(MovementTemplate m) {

    FlightMovementTemplate fmt;
    if (m instanceof FlightMovementTemplate)
      fmt = (FlightMovementTemplate) m;
    else
      fmt = convertGenericMovementTemplateToFlightMovementTemplate(m);
    if (fmt.isDeparture()) {
      return generateNewDepartureAirplaneFromMovement(fmt);
    } else {
      return generateNewArrivalPlaneFromMovement(fmt);
    }
  }

  private void evaluateFails() {
    parent.getMrvaController().evaluateMrvaFails();
    parent.getMrvaController().getMrvaViolatingPlanes()
        .where(q -> Context.getAtc().getResponsibleAtcId(q).getType() == AtcType.app)
        .forEach(q -> Context.getMood().getMoodManager().get(q).experience(Mood.SharedExperience.mrvaViolation));

    parent.getAirproxController().evaluateAirproxFails();
    parent.getAirproxController().getAirproxViolatingPlanes()
        .where(q -> Context.getAtc().getResponsibleAtcId(q.getKey()).getType() == AtcType.app
            && q.getValue() == AirproxType.full)
        .forEach(q -> Context.getMood().getMoodManager().get(q.getKey()).experience(Mood.SharedExperience.airprox));
  }

  private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
    double radial = Coordinates.getBearing(aipFix, navFix);
    radial += Context.getApp().getRnd().nextDouble(-15, 15); // nahodne zatoceni priletoveho radialu
    double dist = Coordinates.getDistanceInNM(navFix, Context.getArea().getAirport().getLocation());
    if (dist > (Context.getArea().getAirport().getCoveredDistance())) {
      dist = Context.getApp().getRnd().nextDouble(25, 40);
    } else {
      dist = Context.getArea().getAirport().getCoveredDistance() - dist;
      if (dist < 25) dist = Context.getApp().getRnd().nextDouble(25, 40);
    }
    Coordinate ret = Coordinates.getCoordinate(navFix, (int) radial, dist);
    return ret;
  }

  private int generateArrivingPlaneAltitude(EntryExitPoint eep, Coordinate planeCoordinate, AirplaneType type) {

    int ret;

    // min alt by mrva
    ret = eep.getMaxMrvaAltitudeOrHigh();

    // update by distance
    {
      final double thousandsFeetPerMile = 500;
      final double distance = Coordinates.getDistanceInNM(Context.getArea().getAirport().getLocation(), eep.getNavaid().getCoordinate())
          + Coordinates.getDistanceInNM(eep.getNavaid().getCoordinate(), planeCoordinate);
      int tmp = (int) (distance * thousandsFeetPerMile);
      ret = Math.max(ret, tmp);
    }

    // update by random value
    ret += Context.getApp().getRnd().nextInt(-3000, 5000);
    if (ret > type.maxAltitude) {
      if (ret < 12000)
        ret = type.maxAltitude - Context.getApp().getRnd().nextInt(4) * 1000;
      else if (ret < 20000)
        ret = type.maxAltitude - Context.getApp().getRnd().nextInt(7) * 1000;
      else
        ret = type.maxAltitude - Context.getApp().getRnd().nextInt(11) * 1000;
    }
    ret = ret / 1000 * 1000;

    // check if initial altitude is not below STAR mrva
    if (ret < eep.getMaxMrvaAltitudeOrHigh()) {
      double tmp = Math.ceil(eep.getMaxMrvaAltitudeOrHigh() / 10d) * 10;
      ret = (int) tmp;
    }

    return ret;
  }

  private int generateDelay() {
    double delayStepProbability = this.delayStepProbability;
    int delayStep = this.delayStep;
    ERandom rnd = Context.getApp().getRnd();

    int ret = 0;
    while (rnd.nextDouble() <= delayStepProbability) {
      int tmp = rnd.nextInt(delayStep + 1);
      ret += tmp;

      if (ret > MAX_ALLOWED_DELAY) break;
    }

    return ret;
  }

  private void generateEmergencyIfRequired() {
    if (parent.getEmergencyAppearanceController().isEmergencyTimeElapsed(Context.getShared().getNow())) {
      if (!Context.getAirplane().getAirplanes().isAny(q -> q.isEmergency())) {
        parent.getAirplanesController().throwEmergency();
      }
      parent.getEmergencyAppearanceController().generateEmergencyTime(Context.getShared().getNow());
    }
  }

  private TryResult<AirplaneTemplate> generateNewArrivalPlaneFromMovement(FlightMovementTemplate m) {
    ArrivalAirplaneTemplate ret;

    Callsign cs = m.getCallsign();

    AirplaneType pt = Context.getAirplaneType().getAirplaneTypes().tryGetByName(m.getAirplaneTypeName());
    if (pt == null)
      return new TryResult<>(new EApplicationException("Unable to find plane type name " + m.getAirplaneTypeName()));

    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryExitInfo(), true, pt);
    if (entryPoint == null) {
      return new TryResult<>(new EApplicationException("Unable to find routing.")); // no route means disallowed IFR
    }

    Coordinate coord = generateArrivalCoordinate(entryPoint.getNavaid().getCoordinate(), Context.getArea().getAirport().getLocation());
    int heading = (int) Coordinates.getBearing(coord, entryPoint.getNavaid().getCoordinate());
    int alt = generateArrivingPlaneAltitude(entryPoint, coord, pt);
    int spd = pt.vCruise;

    EDayTimeStamp entryTime =
        m.getAppearanceTime().isAfterOrEq(Context.getShared().getNow().getTime()) ?
            new EDayTimeStamp(Context.getShared().getNow().getDays(), m.getAppearanceTime()) :
            new EDayTimeStamp(Context.getShared().getNow().getDays() + 1, m.getAppearanceTime());

    EDayTimeStamp expectedExitTime = entryTime.addMinutes(25);
    int delay = generateDelay();

    ret = new ArrivalAirplaneTemplate(
        cs, pt, entryPoint, entryTime, delay, expectedExitTime, coord, heading, alt, spd);

    return new TryResult<>(ret);
  }

  private TryResult<AirplaneTemplate> generateNewDepartureAirplaneFromMovement(FlightMovementTemplate m) {
    DepartureAirplaneTemplate ret;

    Callsign cs = m.getCallsign();
    AirplaneType pt = Context.getAirplaneType().getAirplaneTypes().tryGetByName(m.getAirplaneTypeName());
    if (pt == null)
      return new TryResult<>(new EApplicationException("Unable to find plane type name " + m.getAirplaneTypeName()));

    EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryExitInfo(), false, pt);
    if (entryPoint == null) {
      return new TryResult<>(new EApplicationException("Unable to find routing.")); // no route means disallowed IFR
    }

    EDayTimeStamp entryTime =
        m.getAppearanceTime().isAfterOrEq(Context.getShared().getNow().getTime()) ?
            new EDayTimeStamp(Context.getShared().getNow().getDays(), m.getAppearanceTime()) :
            new EDayTimeStamp(Context.getShared().getNow().getDays() + 1, m.getAppearanceTime());

    int entryDelay = generateDelay();

    EDayTimeStamp expectedExitTime = entryTime.addMinutes(3); // 3 minutes from hp to take-off

    ret = new DepartureAirplaneTemplate(
        cs, pt, entryPoint, entryTime, entryDelay, expectedExitTime);

    return new TryResult<>(ret);
  }

  private void introduceNewPlanes() {
    IReadOnlyList<MovementTemplate> newMovements = parent.getTrafficProvider().getMovementsUntilTime(Context.getShared().getNow());
    IList<AirplaneTemplate> newTemplates = new EList<>();
    for (MovementTemplate newMovement : newMovements) {
      TryResult<AirplaneTemplate> res = convertMovementToAirplane(newMovement);
      if (res.getException() != null)
        Context.getApp().getAppLog().write(ApplicationLog.eType.warning,
            sf("Unable to create a flight, error when creating instance: %s.",
                res.getException().getMessage()));
      else
        newTemplates.add(res.getValue());
    }
    parent.getAirplanesController().addNewPreparedPlanes(newTemplates);
    if (Context.getShared().getNow().getHours() == 20 && Context.getShared().getNow().getMinutes() == 0 && Context.getShared().getNow().getSeconds() == 0)
      parent.getTrafficProvider().prepareTrafficForDay(Context.getShared().getNow().getDays() + 1);
  }

  private void removeOldPlanes() {
    IList<IAirplane> ret = new EList<>();

    for (IAirplane p : Context.getAirplane().getAirplanes()) {
      // landed
      if (p.isArrival() && p.getSha().getSpeed() < 11) {
        ret.add(p);
        Context.getStats().getStatsProvider().registerFinishedPlane(buildFinishedAirplaneStats(p));
      }

      // departed
      if (p.isDeparture()
          && Context.getAtc().getResponsibleAtcId(p.getCallsign()).getType() == AtcType.ctr
          && Coordinates.getDistanceInNM(
          p.getCoordinate(),
          Context.getArea().getAirport().getLocation()) > Context.getArea().getAirport().getCoveredDistance()) {
        ret.add(p);
        Context.getStats().getStatsProvider().registerFinishedPlane(buildFinishedAirplaneStats(p));
      }

      if (p.isEmergency() && p.hasElapsedEmergencyTime()) {
        ret.add(p);
      }
    }

    for (IAirplane plane : ret) {
      parent.getAirplanesController().unregisterPlane(plane.getCallsign());
      Context.getMessaging().getMessenger().unregisterListener(Participant.createAirplane(plane.getCallsign()));
      parent.getMrvaController().unregisterPlane(plane);
    }
  }

  private EntryExitPoint tryGetRandomEntryPoint(EntryExitInfo entryExitInfo, boolean isArrival, AirplaneType pt) {
    EntryExitPoint ret;

    IReadOnlyList<EntryExitPoint> tmp = Context.getArea().getAirport().getEntryExitPoints();
    if (isArrival)
      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.entry || q.getType() == EntryExitPoint.Type.both);
    else
      tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.exit || q.getType() == EntryExitPoint.Type.both);
    tmp = tmp.where(q -> q.getMaxMrvaAltitudeOrHigh() < pt.maxAltitude);
    if (tmp.isEmpty()) {
      Context.getApp().getAppLog().write(ApplicationLog.eType.warning,
          sf("There are no available entry/exit points for plane of kind %s with service ceiling at %d ft. " +
                  "Flight must be cancelled.",
              pt.name, pt.maxAltitude));
      return null;
    }

    if (entryExitInfo.getRadial() != null) {
      ret = tmp.getSmallest(q -> Headings.getDifference(entryExitInfo.getRadial(), q.getRadialFromAirport(), true));
    } else if (entryExitInfo.getNavaid() != null) {
      ret = tmp.tryGetFirst(q -> q.getName().equals(entryExitInfo.getNavaid()));
      if (ret == null) {
        Context.getApp().getAppLog().write(ApplicationLog.eType.warning,
            sf("Plane generation asks for entry point %s, but there is not such " +
                    "entry-exit point available.",
                entryExitInfo.getNavaid()));
        ret = tmp.getRandom();
      }
    } else if (entryExitInfo.getOtherAirportCoordinate() != null) {
      double heading = Coordinates.getBearing(entryExitInfo.getOtherAirportCoordinate(), Context.getArea().getAirport().getLocation());
      ret = tmp.getMinimal(q -> {
        double pointHeading = Coordinates.getBearing(q.getNavaid().getCoordinate(), Context.getArea().getAirport().getLocation());
        return Headings.getDifference(heading, pointHeading, true);
      });
    } else
      ret = tmp.getRandom();

    return ret;
  }

  private void updatePlanes() {
    parent.getAirplanesController().elapseSecond();
  }
}
