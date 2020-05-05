package eng.jAtcSim.newLib.simulation.internal;

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
import eng.jAtcSim.newLib.airplaneType.AirplaneTypeAcc;
import eng.jAtcSim.newLib.airplanes.*;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.atcs.AtcAcc;
import eng.jAtcSim.newLib.atcs.AtcProvider;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;
import eng.jAtcSim.newLib.fleet.airliners.CompanyFleet;
import eng.jAtcSim.newLib.fleet.generalAviation.CountryFleet;
import eng.jAtcSim.newLib.messaging.MessagingAcc;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.simulation.TimerProvider;
import eng.jAtcSim.newLib.stats.StatsAcc;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.Parser;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.traffic.movementTemplating.*;
import eng.jAtcSim.newLib.weather.WeatherManager;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class NewSimulation {

  class Airplanes {
    private FlightMovementTemplate convertGenericMovementTemplateToFlightMovementTemplate(MovementTemplate m) {
      FlightMovementTemplate ret;
      EAssert.Argument.isTrue(m instanceof GenericCommercialMovementTemplate || m instanceof GenericGeneralAviationMovementTemplate);

      Callsign callsign;
      String airplaneTypeName;

      if (m instanceof GenericCommercialMovementTemplate) {
        GenericCommercialMovementTemplate gcmt = (GenericCommercialMovementTemplate) m;

        CompanyFleet companyFleet = NewSimulation.this.context.getAirlinesFleets().tryGetByIcaoOrDefault(gcmt.getCompanyIcao());
        callsign = Callsign.generateCommercial(companyFleet.getIcao());
        IList<TypeAndWeight> availableTypes = companyFleet.getTypes()
            .where(q -> NewSimulation.this.context.getAirplaneTypes()
                .getTypeNames()
                .contains(q.getTypeName()));
        if (availableTypes.isEmpty())
          airplaneTypeName = "N/A";
        else
          airplaneTypeName = availableTypes.getRandomByWeights(
              q -> (double) q.getWeight()).getTypeName();

      } else if (m instanceof GenericGeneralAviationMovementTemplate) {
        GenericGeneralAviationMovementTemplate ggamt = (GenericGeneralAviationMovementTemplate) m;

        CountryFleet countryFleet = NewSimulation.this.context.getGaFleets().tryGetByIcaoOrDefault(ggamt.getCountryIcao());
        callsign = Callsign.generateGeneralAviation(countryFleet.getAircraftPrefix());
        IList<TypeAndWeight> availableTypes = countryFleet.getTypes()
            .where(q -> NewSimulation.this.context.getAirplaneTypes()
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

    private Coordinate generateArrivalCoordinate(Coordinate navFix, Coordinate aipFix) {
      double radial = Coordinates.getBearing(aipFix, navFix);
      radial += SharedAcc.getRnd().nextDouble(-15, 15); // nahodne zatoceni priletoveho radialu
      double dist = Coordinates.getDistanceInNM(navFix, AreaAcc.getAirport().getLocation());
      if (dist > (AreaAcc.getAirport().getCoveredDistance())) {
        dist = SharedAcc.getRnd().nextDouble(25, 40);
      } else {
        dist = AreaAcc.getAirport().getCoveredDistance() - dist;
        if (dist < 25) dist = SharedAcc.getRnd().nextDouble(25, 40);
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
        final double distance = Coordinates.getDistanceInNM(AreaAcc.getAirport().getLocation(), eep.getNavaid().getCoordinate())
            + Coordinates.getDistanceInNM(eep.getNavaid().getCoordinate(), planeCoordinate);
        int tmp = (int) (distance * thousandsFeetPerMile);
        ret = Math.max(ret, tmp);
      }

      // update by random value
      ret += SharedAcc.getRnd().nextInt(-3000, 5000);
      if (ret > type.maxAltitude) {
        if (ret < 12000)
          ret = type.maxAltitude - SharedAcc.getRnd().nextInt(4) * 1000;
        else if (ret < 20000)
          ret = type.maxAltitude - SharedAcc.getRnd().nextInt(7) * 1000;
        else
          ret = type.maxAltitude - SharedAcc.getRnd().nextInt(11) * 1000;
      }
      ret = ret / 1000 * 1000;

      // check if initial altitude is not below STAR mrva
      if (ret < eep.getMaxMrvaAltitudeOrHigh()) {
        double tmp = Math.ceil(eep.getMaxMrvaAltitudeOrHigh() / 10d) * 10;
        ret = (int) tmp;
      }

      return ret;
    }

    private TryResult<AirplaneTemplate> generateNewArrivalPlaneFromMovement(FlightMovementTemplate m) {
      AirplaneTemplate ret;

      Callsign cs = m.getCallsign();

      AirplaneType pt = AirplaneTypeAcc.getAirplaneTypes().tryGetByName(m.getAirplaneTypeName());
      if (pt == null)
        return new TryResult<>(new EApplicationException("Unable to find plane type name " + m.getAirplaneTypeName()));

      EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryExitInfo(), true, pt);
      if (entryPoint == null) {
        return new TryResult<>(new EApplicationException("Unable to find routing.")); // no route means disallowed IFR
      }

      Coordinate coord = generateArrivalCoordinate(entryPoint.getNavaid().getCoordinate(), AreaAcc.getAirport().getLocation());
      int heading = (int) Coordinates.getBearing(coord, entryPoint.getNavaid().getCoordinate());
      int alt = generateArrivingPlaneAltitude(entryPoint, coord, pt);
      int spd = pt.vCruise;

      EDayTimeStamp appearanceTime =
          m.getAppearanceTime().isAfterOrEq(SharedAcc.getNow().getTime()) ?
              new EDayTimeStamp(SharedAcc.getNow().getDays(), m.getAppearanceTime()) :
              new EDayTimeStamp(SharedAcc.getNow().getDays() + 1, m.getAppearanceTime());
      ret = new AirplaneTemplate(
          cs, coord, pt, heading, alt, spd, m.isDeparture(), entryPoint, appearanceTime);

      return new TryResult<>(ret);
    }

    private TryResult<AirplaneTemplate> generateNewDepartureAirplaneFromMovement(FlightMovementTemplate m) {
      AirplaneTemplate ret;

      Callsign cs = m.getCallsign();
      AirplaneType pt = AirplaneTypeAcc.getAirplaneTypes().tryGetByName(m.getAirplaneTypeName());
      if (pt == null)
        return new TryResult<>(new EApplicationException("Unable to find plane type name " + m.getAirplaneTypeName()));

      EntryExitPoint entryPoint = tryGetRandomEntryPoint(m.getEntryExitInfo(), false, pt);
      if (entryPoint == null) {
        return new TryResult<>(new EApplicationException("Unable to find routing.")); // no route means disallowed IFR
      }

      Coordinate coord = AreaAcc.getAirport().getLocation();
      int heading = 0;
      int alt = AreaAcc.getAirport().getAltitude();
      int spd = 0;

      EDayTimeStamp appearanceTime =
          m.getAppearanceTime().isAfterOrEq(SharedAcc.getNow().getTime()) ?
              new EDayTimeStamp(SharedAcc.getNow().getDays(), m.getAppearanceTime()) :
              new EDayTimeStamp(SharedAcc.getNow().getDays() + 1, m.getAppearanceTime());
      ret = new AirplaneTemplate(
          cs, coord, pt, heading, alt, spd, m.isDeparture(), entryPoint, appearanceTime);

      return new TryResult<>(ret);
    }

    private void introduceNewPlanes() {
      IReadOnlyList<MovementTemplate> newMovements = trafficProvider.getMovementsUntilTime(SharedAcc.getNow());
      IList<AirplaneTemplate> newTemplates = new EList<>();
      for (MovementTemplate newMovement : newMovements) {
        TryResult<AirplaneTemplate> res = convertMovementToAirplane(newMovement);
        if (res.getException() != null)
          SharedAcc.getAppLog().writeLine(ApplicationLog.eType.warning,
              sf("Unable to create a flight, error when creating instance: %s.",
                  res.getException().getMessage()));
        else
          newTemplates.add(res.getValue());
      }
      airplanesController.addNewPreparedPlanes(newTemplates);
      if (SharedAcc.getNow().getHours() == 20 && SharedAcc.getNow().getMinutes() == 0 && SharedAcc.getNow().getSeconds() == 0)
        trafficProvider.prepareTrafficForDay(SharedAcc.getNow().getDays() + 1);
    }

    private void generateEmergencyIfRequired() {
      if (NewSimulation.this.emergencyAppearanceController.isEmergencyTimeElapsed(SharedAcc.getNow())) {
        if (!AirplaneAcc.getAirplanes().isAny(q -> q.isEmergency())) {
          NewSimulation.this.airplanesController.throwEmergency();
        }
        NewSimulation.this.emergencyAppearanceController.generateEmergencyTime(SharedAcc.getNow());
      }
    }

    void manageTrafficPerSecond() {
      introduceNewPlanes();
      removeOldPlanes();
      generateEmergencyIfRequired();
      updatePlanes();
      evalAirproxes();
      evalMrvas();
    }

    private void removeOldPlanes() {
      IList<IAirplane> ret = new EList<>();

      for (IAirplane p : AirplaneAcc.getAirplanes()) {
        // landed
        if (p.isArrival() && p.getSha().getSpeed() < 11) {
          ret.add(p);
          StatsAcc.getOverallStatsWriter().registerFinishedPlane(p);
        }

        // departed
        if (p.isDeparture()
            && AtcAcc.getResponsibleAtcId(p.getCallsign()).getType() == AtcType.ctr
            && Coordinates.getDistanceInNM(
              p.getCoordinate(),
              AreaAcc.getAirport().getLocation()) > AreaAcc.getAirport().getCoveredDistance()) {
          ret.add(p);
          this.stats.registerFinishedPlane(p);
        }

        if (p.isEmergency() && p.hasElapsedEmergencyTime()) {
          ret.add(p);
        }
      }

      for (IAirplane plane : ret) {
        NewSimulation.this.airplanesController.unregisterPlane(plane.getCallsign());
        MessagingAcc.getMessenger().unregisterListener(Participant.createAirplane(plane.getCallsign()));
        NewSimulation.this.mrvaController.unregisterPlane(plane);
      }
    }

    private EntryExitPoint tryGetRandomEntryPoint(EntryExitInfo entryExitInfo, boolean isArrival, AirplaneType pt) {
      EntryExitPoint ret;

      IReadOnlyList<EntryExitPoint> tmp = AreaAcc.getAirport().getEntryExitPoints();
      if (isArrival)
        tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.entry || q.getType() == EntryExitPoint.Type.both);
      else
        tmp = tmp.where(q -> q.getType() == EntryExitPoint.Type.exit || q.getType() == EntryExitPoint.Type.both);
      tmp = tmp.where(q -> q.getMaxMrvaAltitudeOrHigh() < pt.maxAltitude);
      if (tmp.isEmpty()) {
        SharedAcc.getAppLog().writeLine(ApplicationLog.eType.warning,
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
          SharedAcc.getAppLog().writeLine(ApplicationLog.eType.warning,
              sf("Plane generation asks for entry point %s, but there is not such " +
                      "entry-exit point available.",
                  entryExitInfo.getNavaid()));
          ret = tmp.getRandom();
        }
      } else if (entryExitInfo.getOtherAirportCoordinate() != null) {
        double heading = Coordinates.getBearing(entryExitInfo.getOtherAirportCoordinate(), AreaAcc.getAirport().getLocation());
        ret = tmp.getMinimal(q -> {
          double pointHeading = Coordinates.getBearing(q.getNavaid().getCoordinate(), AreaAcc.getAirport().getLocation());
          return Headings.getDifference(heading, pointHeading, true);
        });
      } else
        ret = tmp.getRandom();

      return ret;
    }
  }

  private static final boolean DEBUG_STYLE_TIMER = false;
  private final SimulationContext context;
  private final SimulationSettings settings;
  private final EDayTimeRun now;
  private final AtcProvider atcProvider;
  private final EmergencyAppearanceController emergencyAppearanceController;
  private final MrvaController mrvaController;
  private final StatsProvider statsProvider;
  private final AirplanesController airplanesController = new AirplanesController();
  private final WeatherManager weatherManager;
  private final TrafficProvider trafficProvider;
  private final TimerProvider timer;
  private final SystemMessagesProcessor systemMessagesProcessor = new SystemMessagesProcessor();
  private final Parser parser;
  private final Formatter formatter;
  private final NewSimulation.Airplanes airplanes = this.new Airplanes();
  private boolean isElapseSecondCalculationRunning = false;

  public NewSimulation(
      SimulationContext simulationContext,
      SimulationSettings simulationSettings,
      EDayTimeStamp simulationStartTime) {
    EAssert.Argument.isNotNull(simulationContext, "simulationContext");
    EAssert.Argument.isNotNull(simulationSettings, "simulationSettings");
    EAssert.Argument.isNotNull(simulationStartTime, "simulationStartTime");
    this.context = simulationContext;
    this.settings = simulationSettings;
    this.now = new EDayTimeRun(simulationStartTime.getValue());

    this.atcProvider = new AtcProvider(context.getActiveAirport());
    this.trafficProvider = new TrafficProvider(context.getTraffic());
    this.emergencyAppearanceController = new EmergencyAppearanceController(
        simulationSettings.getEmergencyPerDayProbability());
    this.mrvaController = new MrvaController(
        context.getArea().getBorders().where(q -> q.getType() == Border.eType.mrva));
    this.statsProvider = new StatsProvider(settings.getStatsSnapshotDistanceInMinutes());
    this.weatherManager = new WeatherManager(simulationContext.getWeatherProvider());

    this.parser = settings.getParser();
    this.formatter = settings.getFormatter();

    this.timer = new TimerProvider(settings.getSimulationSecondLengthInMs(), this::timerTicked);
  }

  public void init() {
    this.airplanesController.init();
    this.weatherManager.init();
    this.atcProvider.init();
    this.statsProvider.init();
    this.trafficProvider.init();
  }

  private void elapseSecond() {
    long elapseStartMs = System.currentTimeMillis();

    if (isElapseSecondCalculationRunning) {
      SharedAcc.getAppLog().writeLine(
          ApplicationLog.eType.warning,
          "elapseSecond() called before the previous one was finished!");
      return;
    }
    if (DEBUG_STYLE_TIMER)
      timer.stop();
    isElapseSecondCalculationRunning = true;
    now.increaseSecond();

    // process system messages
    systemMessagesProcessor.elapseSecond();

    // traffic stuff
    airplanes.manageTrafficPerSecond();
  }

  private void timerTicked(TimerProvider sender) {
    elapseSecond();
  }
}
