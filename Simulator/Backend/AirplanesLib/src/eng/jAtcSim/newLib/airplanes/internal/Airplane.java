package eng.jAtcSim.newLib.airplanes.internal;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.airplanes.*;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.modules.AirplaneFlightModule;
import eng.jAtcSim.newLib.airplanes.modules.AtcModule;
import eng.jAtcSim.newLib.airplanes.modules.DivertModule;
import eng.jAtcSim.newLib.airplanes.modules.EmergencyModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.ShaModule;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.ToCoordinateNavigator;
import eng.jAtcSim.newLib.airplanes.modules.speeches.RoutingModule;
import eng.jAtcSim.newLib.airplanes.other.CockpitVoiceRecorder;
import eng.jAtcSim.newLib.airplanes.other.CommandQueueRecorder;
import eng.jAtcSim.newLib.airplanes.other.FlightDataRecorder;
import eng.jAtcSim.newLib.airplanes.pilots.*;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.DepartureAirplaneTemplate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.mood.Mood;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.DivertTimeNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.DivertingNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSimLib.xmlUtils.Deserializer;
import eng.jAtcSimLib.xmlUtils.Parser;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.deserializers.ObjectDeserializer;
import eng.jAtcSimLib.xmlUtils.serializers.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Airplane {

  public class AirplaneShaImpl implements IAirplaneSHA {
    @Override
    public int getAltitude() {
      return Airplane.this.sha.getAltitude();
    }

    @Override
    public int getHeading() {
      return Airplane.this.sha.getHeading();
    }

    @Override
    public int getSpeed() {
      return Airplane.this.sha.getSpeed();
    }

    @Override
    public Restriction getSpeedRestriction() {
      return Airplane.this.sha.getSpeedRestriction();
    }

    @Override
    public int getTargetAltitude() {
      return Airplane.this.sha.getTargetAltitude();
    }

    @Override
    public int getTargetHeading() {
      return Airplane.this.sha.getTargetHeading();
    }

    @Override
    public int getTargetSpeed() {
      return Airplane.this.sha.getTargetSpeed();
    }

    @Override
    public int getVerticalSpeed() {
      return Airplane.this.sha.getVerticalSpeed();
    }
  }

  public class AirplaneAtcImpl implements IAirplaneAtc {
    @Override
    public AtcId getTunedAtc() {
      return Airplane.this.atcModule.getTunedAtc();
    }

    @Override
    public boolean hasRadarContact() {
      return Airplane.this.atcModule.hasRadarContact();
    }
  }

  public class AirplaneFlightImpl implements IAirplaneFlight {

    @Override
    public int getEntryDelay() {
      return Airplane.this.flightModule.getEntryDelay();
    }

    @Override
    public int getExitDelay() {
      return Airplane.this.flightModule.getExitDelay();
    }
  }

  public class AirplaneRoutingImpl implements IAirplaneRouting {

    @Override
    public String getAssignedDARouteName() {
      return Airplane.this.routingModule.getAssignedDARouteName();
    }

    @Override
    public ActiveRunwayThreshold getAssignedRunwayThreshold() {
      return Airplane.this.routingModule.getRunwayThreshold();
    }

    @Override
    public Navaid getEntryExitPoint() {
      return Airplane.this.routingModule.getEntryExitPoint();
    }

    @Override
    public boolean hasLateralDirectionAfterCoordinate() {
      Coordinate coordinate = tryGetTargetCoordinate();
      assert coordinate != null;
      return Airplane.this.routingModule.hasLateralDirectionAfterCoordinate(coordinate);
    }

    @Override
    public boolean isDivertable() {
      return Airplane.this.pilot.isDivertable();
    }

    @Override
    public boolean isGoingToFlightOverNavaid(Navaid n) {
      return Airplane.this.routingModule.isGoingToFlightOverNavaid(n);
    }

    @Override
    public boolean isRoutingEmpty() {
      throw new ToDoException();
    }

    @Override
    public Coordinate tryGetTargetCoordinate() {
      return Airplane.this.sha.tryGetTargetCoordinate();
    }

    @Override
    public Coordinate tryGetTargetOrHoldCoordinate() {
      Coordinate ret = tryGetTargetCoordinate();
      if (ret == null) {
        if (Airplane.this.pilot instanceof HoldPilot) {
          HoldPilot holdPilot = (HoldPilot) Airplane.this.pilot;
          ret = holdPilot.navaid.getCoordinate();
        }
      }

      return ret;
    }
  }

  public class AirplaneImpl implements IAirplane {
    private final IAirplaneAtc atc = Airplane.this.new AirplaneAtcImpl();
    private final IAirplaneFlight flight = Airplane.this.new AirplaneFlightImpl();
    private final IAirplaneRouting routing = Airplane.this.new AirplaneRoutingImpl();
    private final IAirplaneSHA sha = Airplane.this.new AirplaneShaImpl();

    @Override
    public IAirplaneAtc getAtc() {
      return atc;
    }

    @Override
    public Callsign getCallsign() {
      return Airplane.this.flightModule.getCallsign();
    }

    @Override
    public Coordinate getCoordinate() {
      return Airplane.this.coordinate;
    }

    @Override
    public IAirplaneFlight getFlight() {
      return flight;
    }

    @Override
    public IAirplaneRouting getRouting() {
      return routing;
    }

    @Override
    public IAirplaneSHA getSha() {
      return sha;
    }

    @Override
    public Squawk getSqwk() {
      return Airplane.this.squawk;
    }

    @Override
    public AirplaneState getState() {
      return Airplane.this.state;
    }

    @Override
    public AirplaneType getType() {
      return Airplane.this.airplaneType;
    }

    @Override
    public boolean hasElapsedEmergencyTime() {
      return false;
    }

    @Override
    public boolean isArrival() {
      return Airplane.this.flightModule.isArrival();
    }

    @Override
    public boolean isEmergency() {
      return Airplane.this.emergencyModule.isEmergency();
    }

    @Override
    public GoingAroundNotification.GoAroundReason pullLastGoAroundReasonIfAny() {
      GoingAroundNotification.GoAroundReason ret = Airplane.this.lastGoAroundReasonIfAny;
      if (ret != null) Airplane.this.lastGoAroundReasonIfAny = null;
      return ret;
    }
  }

  public class AirplaneWriterImpl implements IAirplaneWriter {

    @Override
    public void abortHolding() {
      if (Airplane.this.flightModule.isArrival())
        setPilotAndState(new ArrivalPilot(Airplane.this), AirplaneState.arrivingHigh);
      else
        setPilotAndState(new DeparturePilot(Airplane.this), AirplaneState.departingLow);
      throw new ToDoException("next line?");
      //Pilot.this.pilotWriteSimple.adjustTargetSpeed();
    }

    @Override
    public void addExperience(Mood.ArrivalExperience experience) {
      Airplane.this.mood.experience(experience);
    }

    @Override
    public void addExperience(Mood.DepartureExperience experience) {
      Airplane.this.mood.experience(experience);
    }

    @Override
    public void applyShortcut(Navaid navaid) {
      Airplane.this.routingModule.applyShortcut(navaid);
    }

    @Override
    public void clearedToApproach(Approach approach, ApproachEntry entry) {
      ApproachPilot approachPilot = new ApproachPilot(Airplane.this, approach, entry);
      setPilotAndState(approachPilot, AirplaneState.flyingIaf2Faf);
    }

    @Override
    public void divert(boolean isInvokedByAtc) {
      if (isInvokedByAtc) {
        if (Airplane.this.emergencyModule.isEmergency())
          this.addExperience(Mood.DepartureExperience.divertedAsEmergency);
        else if (!Context.getAirplane().isSomeActiveEmergency() == false)
          this.addExperience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
        Airplane.this.divertModule.disable();
      } else {
        this.addExperience(Mood.ArrivalExperience.divertOrderedByCaptain);
      }

      Navaid divertNavaid = getDivertNavaid();
      DARoute route = DARoute.createNewVectoringByFix(divertNavaid);

      Airplane.this.flightModule.divert();
      setRouting(route, Airplane.this.routingModule.getRunwayThreshold());
      setPilotAndState(new DeparturePilot(Airplane.this), AirplaneState.departingLow);

      if (!isInvokedByAtc)
        this.sendMessage(new DivertingNotification(divertNavaid.getName()));
    }

    @Override
    public CockpitVoiceRecorder getCVR() {
      return Airplane.this.cvr;
    }

    @Override
    public void goAround(GoingAroundNotification.GoAroundReason reason) {
      EAssert.isNotNull(reason);

      Airplane.this.lastGoAroundReasonIfAny = reason;

      boolean isAtcFail = EnumUtils.is(reason,
              new GoingAroundNotification.GoAroundReason[]{
                      GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
                      GoingAroundNotification.GoAroundReason.noLandingClearance,
                      GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
                      GoingAroundNotification.GoAroundReason.notStabilizedAirplane
              });
      if (isAtcFail)
        this.addExperience(
                Mood.ArrivalExperience.goAroundNotCausedByPilot);

      GoingAroundNotification gan = new GoingAroundNotification(reason);
      this.sendMessage(gan);

      EAssert.isTrue(Airplane.this.pilot instanceof ApproachPilot);
      ApproachPilot prevPilot = (ApproachPilot) Airplane.this.pilot;
      Airplane.this.sha.setTargetSpeed(Airplane.this.airplaneType.vDep);
      Airplane.this.sha.setTargetAltitude(Airplane.this.sha.getAltitude());
      Airplane.this.sha.setNavigator(new HeadingNavigator(prevPilot.getRunwayThreshold().getCourse()));

      SpeechList<ICommand> gas = prevPilot.getGoAroundRouting();
      setRouting(gas);

      setPilotAndState(
              new TakeOffPilot(Airplane.this),
              AirplaneState.takeOffGoAround);
    }

    @Override
    public void hold(Navaid navaid, int inboundRadial, LeftRight turn) {
      setPilotAndState(
              new HoldPilot(Airplane.this, navaid, inboundRadial, turn),
              AirplaneState.holding
      );
    }

    @Override
    public void processRadarContactConfirmation() {
      Airplane.this.atcModule.setHasRadarContact();
    }

    @Override
    public void raiseEmergency() {
      //TODO Implement this: how to raise an emergency
      throw new ToDoException("how to raise an emergency");
    }

    @Override
    public void reportDivertTimeLeft() {
      EAssert.isTrue(Airplane.this.flightModule.isArrival());
      EDayTimeStamp divertTime = Airplane.this.divertModule.getDivertTime();
      EDayTimeStamp now = Context.getShared().getNow().toStamp();
      int minutesLeft = (int) Math.ceil((divertTime.getValue() - now.getValue()) / 60d);
      EAssert.isTrue(minutesLeft >= 0);
      sendMessage(new DivertTimeNotification(minutesLeft));
    }

    @Override
    public void resetHeading(double heading) {
      Airplane.this.sha.resetHeading(heading);
    }

    @Override
    public void sendMessage(SpeechList<IFromPlaneSpeech> speechList) {
      AtcId tunedAtc = atcModule.getTunedAtc();
      if (tunedAtc != null)
        speechCache.getOrSet(tunedAtc, () -> new SpeechList<>()).addMany(speechList);
    }

    @Override
    public void setAltitudeRestriction(Restriction restriction) {
      Airplane.this.sha.setAltitudeRestriction(restriction);
    }

    @Override
    public void setHoldingPoint(ActiveRunwayThreshold t) {
      Airplane.this.coordinate = t.getCoordinate();
    }

    @Override
    public void setRouting(IafRoute iafRoute, ActiveRunwayThreshold activeRunwayThreshold) {
      Airplane.this.routingModule.setRunwayThreshold(activeRunwayThreshold);
      Airplane.this.routingModule.setRouting(iafRoute.getRouteCommands());
    }

    @Override
    public void setRouting(IReadOnlyList<ICommand> routeCommands) {
      Airplane.this.routingModule.setRouting(routeCommands);
    }

    @Override
    public void setRouting(DARoute daRoute, ActiveRunwayThreshold activeRunwayThreshold) {
      Airplane.this.routingModule.setRunwayThreshold(activeRunwayThreshold);
      Airplane.this.routingModule.setEntryExitPoint(daRoute.getMainNavaid());
      Airplane.this.routingModule.setAssignedDARouteName(daRoute.getName());
      Airplane.this.routingModule.setRouting(daRoute.getRouteCommands());
    }

    @Override
    public void setSpeedRestriction(Restriction restriction) {
      Airplane.this.sha.setSpeedRestriction(restriction);
    }

    @Override
    public void setState(AirplaneState state) {
      Airplane.this.state = state;
    }

    @Override
    public void setTargetAltitude(int altitudeInFt) {
      Airplane.this.sha.setTargetAltitude(altitudeInFt);
    }

    @Override
    public void setTargetCoordinate(Coordinate coordinate) {
      EAssert.Argument.isNotNull(coordinate, "coordinate");
      Airplane.this.sha.setNavigator(new ToCoordinateNavigator(coordinate));
    }

    @Override
    public void setTargetHeading(Navigator navigator) {
      EAssert.Argument.isNotNull(navigator, "navigator");
      Airplane.this.sha.setNavigator(navigator);
    }

    @Override
    public void setTargetSpeed(int speed) {
      Airplane.this.sha.setTargetSpeed(speed);
    }

    @Override
    public void startArriving() {
      setPilotAndState(
              new ArrivalPilot(Airplane.this),
              AirplaneState.arrivingHigh
      );
    }

    @Override
    public void startDeparting() {
      setPilotAndState(
              new DeparturePilot(Airplane.this),
              AirplaneState.departingLow);
    }

    @Override
    public void startHolding(Navaid navaid, int inboundRadial, LeftRight turn) {
      HoldPilot pilot = new HoldPilot(
              Airplane.this,
              navaid,
              inboundRadial,
              turn
      );
      setPilotAndState(pilot, AirplaneState.holding);
    }

    @Override
    public void startTakeOff() {
      TakeOffPilot pilot = new TakeOffPilot(Airplane.this);
      this.setPilotAndState(pilot, AirplaneState.holdingPoint);
    }

    @Override
    public String toString() {
      return sf("%s (%s)",
              Airplane.this.flightModule.getCallsign().toString(),
              Airplane.this.squawk.toString());
    }

    @Override
    public void tuneAtc(AtcId atcId) {
      EAssert.Argument.isNotNull(atcId, "atcId");
      Airplane.this.atcModule.changeAtc(atcId);
    }

    private void setPilotAndState(Pilot pilot, AirplaneState state) {
      Airplane.this.pilot = pilot;
      Airplane.this.state = state;
    }
  }

  private static final double secondFraction = 1 / 60d / 60d;

  public static Airplane createArrival(ArrivalAirplaneTemplate template, Squawk sqwk, AtcId initialAtcId) {
    Airplane ret = new Airplane(
            template.getCallsign(), template.getCoordinate(), sqwk, template.getAirplaneType(),
            template.getHeading(), template.getAltitude(), template.getSpeed(), false,
            template.getEntryPoint().getNavaid(), template.getExpectedExitTime(), template.getEntryDelay(),
            initialAtcId
    );
    return ret;
  }

  public static Airplane createDeparture(DepartureAirplaneTemplate template, Squawk sqwk, AtcId initialAtcId) {
    Airplane ret = new Airplane(
            template.getCallsign(), Context.getArea().getAirport().getLocation(), sqwk, template.getAirplaneType(),
            0, Context.getArea().getAirport().getAltitude(), 0, true,
            template.getExitPoint().getNavaid(), template.getExpectedExitTime(), template.getEntryDelay(),
            initialAtcId
    );
    return ret;
  }

  public static Airplane load(XElement element, IMap<String, Object> context) {
    IReadOnlyList<AtcId> atcs = (IReadOnlyList<AtcId>) context.get("atcs");
    AirplaneTypes airplaneTypes = (AirplaneTypes) context.get("airplaneTypes");

    String callsignString = element.getChild("flightModule").getChild("callsign").getContent();
    Callsign callsign = new Callsign(callsignString);
    Airplane ret = new Airplane(callsign);

    XmlLoadUtils.Field.restoreField(element, ret, "state");
    XmlLoadUtils.Field.restoreField(element, ret, "lastGoAroundReasonIfAny");
    XmlLoadUtils.Field.restoreField(element, ret, "coordinate", SharedXmlUtils.Deserializers.coordinateDeserializer);
    XmlLoadUtils.Field.restoreField(element, ret, "squawk", SharedXmlUtils.Deserializers.squawkDeserializer);
    XmlLoadUtils.Field.restoreField(element, ret, "airplaneType", (Parser) e -> airplaneTypes.getByName(e));

    XmlLoadUtils.Field.restoreField(element, ret, "atcModule",
            ObjectDeserializer.createFor(AtcModule.class)
                    .useDeserializer(AtcId.class, SharedXmlUtils.DeserializersDynamic.getAtcIdDeserializer(atcs))
                    .excludeFields("plane", "rdr", "wrt")
                    .useInstanceProvider(AtcModule.class, () -> ret.atcModule));

    XmlLoadUtils.Field.restoreField(element, ret, "mood", (Deserializer) e -> Mood.load(e));

    if (ret.divertModule != null)
      XmlLoadUtils.Field.restoreField(element, ret, "divertModule",
              ObjectDeserializer.createFor(DivertModule.class)
                      .useDeserializers(SharedXmlUtils.Deserializers.deserializersMap)
                      .excludeFields("plane", "rdr", "wrt"));

    XmlLoadUtils.Field.restoreField(element, ret, "emergencyModule",
            ObjectDeserializer.createFor(EmergencyModule.class)
                    .useDeserializers(SharedXmlUtils.Deserializers.deserializersMap));

    XmlLoadUtils.Field.restoreField(element, ret, "flightModule",
            ObjectDeserializer.createFor(AirplaneFlightModule.class)
                    .useDeserializers(SharedXmlUtils.Deserializers.deserializersMap)
                    .excludeFields("plane", "rdr", "wrt")
                    .useInstanceProvider(AirplaneFlightModule.class, () -> ret.flightModule));

    XmlLoadUtils.Field.restoreField(element, ret, "routingModule", (Deserializer) e -> ret.routingModule.load(e, context));


    XmlLoadUtils.Field.restoreField(element, ret, "shaModule", (Deserializer) e -> ret.sha.load(ret, e));

    XmlLoadUtils.Field.restoreField(element, ret, "atcModule",
            ObjectDeserializer.createFor(AtcModule.class)
                    .useDeserializer(AtcId.class, SharedXmlUtils.DeserializersDynamic.getAtcIdDeserializer(atcs)));


    XmlLoadUtils.Field.restoreField(element, ret, "coordinate", SharedXmlUtils.Deserializers.coordinateDeserializer);

    XmlLoadUtils.Field.restoreField(element, ret, "pilot", (Deserializer) e -> Pilot.load(e));

    return ret;
  }

  private final AirplaneType airplaneType;
  private final AtcModule atcModule;
  private Coordinate coordinate;
  private final CockpitVoiceRecorder cvr;
  private final DivertModule divertModule;
  private final EmergencyModule emergencyModule;
  private final FlightDataRecorder fdr;
  private final CommandQueueRecorder cqr;
  private final AirplaneFlightModule flightModule;
  private final Mood mood;
  private Pilot pilot;
  private final IAirplane rdr = new AirplaneImpl();
  private final RoutingModule routingModule;
  private final ShaModule sha;
  private final Squawk squawk;
  private AirplaneState state;
  private final IAirplaneWriter wrt = new AirplaneWriterImpl();
  private GoingAroundNotification.GoAroundReason lastGoAroundReasonIfAny = null;
  private final IMap<AtcId, SpeechList<IFromPlaneSpeech>> speechCache = new EMap<>();

  private Airplane(Callsign callsign) {
    this.squawk = Squawk.create("0000");
    this.flightModule = new AirplaneFlightModule(
            callsign,
            0, new EDayTimeStamp(0), true);

    this.airplaneType = new AirplaneType(
            "NOTYP", "only-for-loading", 'A', 0, 0, 0, 0, 0, 0
            , 0, 0, 0, 0, 0, 0, 0, 0
            , 0, 0);

    this.atcModule = new AtcModule(this, new AtcId("LOADING", 0, AtcType.app));

    this.divertModule = new DivertModule(this);
    this.emergencyModule = new EmergencyModule();
    this.mood = new Mood();
    this.routingModule = new RoutingModule(this, Navaid.create(
            "LOADN", Navaid.eType.auxiliary, new Coordinate(0, 0)));
    this.sha = new ShaModule(this, 0, 0, 0, this.airplaneType);

    this.fdr = new FlightDataRecorder(this.flightModule.getCallsign());
    this.cvr = new CockpitVoiceRecorder(this.flightModule.getCallsign());
    this.cqr = new CommandQueueRecorder(this.flightModule.getCallsign());
    this.routingModule.setCqr(this.cqr);
  }

  private Airplane(Squawk squawk, AirplaneType airplaneType,
                   AirplaneFlightModule flightModule,
                   ShaModule shaModule, AtcModule atcModule, RoutingModule routingModule,
                   DivertModule divertModule, Mood mood, EmergencyModule emergencyModule,
                   Coordinate coordinate, Pilot pilot, AirplaneState state) {
    this.squawk = squawk;
    this.flightModule = flightModule;
    this.fdr = new FlightDataRecorder(this.flightModule.getCallsign());
    this.cvr = new CockpitVoiceRecorder(this.flightModule.getCallsign());
    this.cqr = new CommandQueueRecorder(this.flightModule.getCallsign());
    this.sha = shaModule;
    this.emergencyModule = emergencyModule;
    this.atcModule = atcModule;
    this.routingModule = routingModule;
    this.routingModule.setCqr(this.cqr);
    this.divertModule = divertModule;
    this.mood = mood;
    this.state = state;
    this.coordinate = coordinate;
    this.airplaneType = airplaneType;
    this.pilot = pilot;
  }

  private Airplane(Callsign callsign, Coordinate coordinate, Squawk squawk, AirplaneType airplaneType,
                   int heading, int altitude, int speed, boolean isDeparture,
                   Navaid entryExitPoint, EDayTimeStamp expectedExitTime, int entryDelay,
                   AtcId initialAtcId) {


    this.squawk = squawk;
    this.flightModule = new AirplaneFlightModule(
            callsign, entryDelay, expectedExitTime, isDeparture);

    this.fdr = new FlightDataRecorder(this.flightModule.getCallsign());
    this.cvr = new CockpitVoiceRecorder(this.flightModule.getCallsign());
    this.cqr = new CommandQueueRecorder(this.flightModule.getCallsign());

    this.sha = new ShaModule(this, heading, altitude, speed, airplaneType);
    this.emergencyModule = new EmergencyModule();
    this.atcModule = new AtcModule(this, initialAtcId);
    this.routingModule = new RoutingModule(this, entryExitPoint);
    this.routingModule.setCqr(this.cqr);
    if (isDeparture)
      this.divertModule = null;
    else
      this.divertModule = new DivertModule(this);
    this.mood = new Mood();

    this.state = isDeparture ? AirplaneState.holdingPoint : AirplaneState.arrivingHigh;
    this.coordinate = coordinate;
    this.airplaneType = airplaneType;

    if (isDeparture) {
      this.pilot = new HoldingPointPilot(this);
    } else {
      this.pilot = new ArrivalPilot(this);
    }
  }

  public void elapseSecond() {

    this.routingModule.elapseSecond(); // here messages are processed

    this.pilot.elapseSecond();
    this.atcModule.elapseSecond();
    if (this.divertModule != null) // only for arrivals
      this.divertModule.elapseSecond();

    this.flushSpeeches();

    this.sha.elapseSecond();
    updateCoordinates();

    logToFdr();

    //printAfterCommands();
    //this.recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  public IAirplane getReader() {
    return this.rdr;
  }

  public IAirplaneWriter getWriter() {
    return this.wrt;
  }

  public void save(XElement target) {

    XmlSaveUtils.Field.storeFields(target, this,
            "state", "lastGoAroundReasonIfAny");
    XmlSaveUtils.Field.storeField(target, this, "coordinate", SharedXmlUtils.Formatters.coordinateFormatter);
    XmlSaveUtils.Field.storeField(target, this, "squawk", SharedXmlUtils.Formatters.squawkFormatter);
    XmlSaveUtils.Field.storeField(target, this, "airplaneType", (AirplaneType q) -> q.name);

    XmlSaveUtils.Field.storeField(target, this, "atcModule",
            ObjectSerializer.createFor(AtcModule.class)
                    .useSerializer(AtcId.class, SharedXmlUtils.Serializers.atcIdSerializer)
                    .excludeFields("plane", "rdr", "wrt"));

    XmlSaveUtils.Field.storeField(target, this, "mood",
            (XElement e, Mood q) -> q.save(e));

    XmlSaveUtils.Field.storeField(target, this, "divertModule",
            ObjectSerializer.createFor(DivertModule.class)
                    .useSerializers(SharedXmlUtils.Serializers.serializersMap)
                    .excludeFields("plane", "rdr", "wrt"));

    XmlSaveUtils.Field.storeField(target, this, "emergencyModule",
            ObjectSerializer.createFor(DivertModule.class)
                    .useSerializers(SharedXmlUtils.Serializers.serializersMap));

    XmlSaveUtils.Field.storeField(target, this, "flightModule",
            ObjectSerializer.createFor(AirplaneFlightModule.class)
                    .useSerializers(SharedXmlUtils.Serializers.serializersMap)
                    .excludeFields("plane", "rdr", "wrt"));

    XmlSaveUtils.Field.storeField(target, this, "routingModule",
            (XElement e, RoutingModule q) -> q.save(e));
    XmlSaveUtils.Field.storeField(target, this, "sha",
            (XElement e, ShaModule q) -> q.save(e));
    XmlSaveUtils.Field.storeField(target, this, "pilot",
            (XElement e, Pilot q) -> q.save(e));
  }

  private void flushSpeeches() {
    for (AtcId atcId : speechCache.getKeys()) {
      SpeechList<IFromPlaneSpeech> msgs = speechCache.get(atcId);
      if (msgs.isEmpty()) continue;
      Message m = new Message(
              Participant.createAirplane(Airplane.this.getReader().getCallsign()),
              Participant.createAtc(atcId),
              msgs.clone());
      Context.getMessaging().getMessenger().send(m);
      msgs.clear();
    }
  }

  private Navaid getDivertNavaid() {
    IList<DARoute> rts = Context.getArea().getCurrentRunwayConfiguration()
            .getDepartures()
            .where(q -> q.isForCategory(Airplane.this.airplaneType.category))
            .getRandom()
            .getThreshold()
            .getRoutes()
            .where(q -> q.getType() == DARouteType.sid);
    DARoute r = rts.getRandom();
    //TODO here can null-pointer-exception occur when no route is found for threshold and category
    Navaid ret = r.getMainNavaid();
    return ret;
  }

  private void logToFdr() {
    fdr.log(
            coordinate,
            sha.getHeading(), sha.getTargetHeading(),
            sha.getAltitude(), sha.getVerticalSpeed(), sha.getTargetAltitude(),
            sha.getSpeed(), sha.getGS(), sha.getTargetSpeed(),
            state,
            sha.getNavigator()
    );
  }

  private void updateCoordinates() {
    double dist = this.sha.getGS() * secondFraction;
    Coordinate newC
            = Coordinates.getCoordinate(coordinate, this.sha.getHeading(), dist);

    // add wind if flying
    if (this.state.is(
            AirplaneState.holdingPoint,
            AirplaneState.takeOffRoll,
            AirplaneState.landed
    ) == false) {
      Weather weather = Context.getWeather().getWeather();
      newC = Coordinates.getCoordinate(
              newC,
              weather.getWindHeading(),
              UnitProvider.ftToNm(weather.getWindSpeedOrWindGustSpeed()));
    }

    this.coordinate = newC;
  }


}
