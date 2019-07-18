/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import com.sun.istack.internal.Nullable;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.*;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneWriteSimple;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.*;
import eng.jAtcSim.lib.airplanes.pilots.modules.*;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.ToCoordinateNavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.global.logging.FileSaver;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertingNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.EmergencyNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Pilot {

  public class PilotWriteSimple implements IPilotWriteSimple {

    @Override
    public void adjustTargetSpeed() {
      int minOrdered;
      int maxOrdered;
      Restriction speedRestriction = parent.getSha().getSpeedRestriction();
      if (speedRestriction != null) {
        switch (speedRestriction.direction) {
          case exactly:
            minOrdered = speedRestriction.value;
            maxOrdered = speedRestriction.value;
            break;
          case atLeast:
            minOrdered = speedRestriction.value;
            maxOrdered = Integer.MAX_VALUE;
            break;
          case atMost:
            minOrdered = Integer.MIN_VALUE;
            maxOrdered = speedRestriction.value;
            break;
          default:
            throw new EEnumValueUnsupportedException(speedRestriction.direction);
        }
      } else {
        minOrdered = Integer.MIN_VALUE;
        maxOrdered = Integer.MAX_VALUE;
      }
      int ts;
      switch (parent.getState()) {
        case holdingPoint:
        case landed:
          ts = 0;
          break;
        case takeOffRoll:
        case takeOffGoAround:
          ts = parent.getType().vR + 10;
          break;
        case departingLow:
        case arrivingLow:
          ts = getBoundedValueIn(minOrdered, Math.min(250, parent.getType().vCruise), maxOrdered);
          break;
        case departingHigh:
        case arrivingHigh:
          ts = getBoundedValueIn(minOrdered, Math.min(287, parent.getType().vCruise), maxOrdered);
          break;
        case arrivingCloseFaf:
        case flyingIaf2Faf:
          ts = getBoundedValueIn(minOrdered, Math.min(287, parent.getType().vMinClean + 15), maxOrdered);
          break;
        case approachEnter:
          ts = getBoundedValueIn(minOrdered, Math.min(parent.getType().vMaxApp, parent.getType().vMinClean), maxOrdered);
          break;
        case approachDescend:
          ts = getBoundedValueIn(minOrdered, parent.getType().vApp, maxOrdered);
          break;
        case longFinal:
        case shortFinal:
          minOrdered = Math.max(minOrdered, parent.getType().vMinApp);
          maxOrdered = Math.min(maxOrdered, parent.getType().vMaxApp);
          ts = getBoundedValueIn(minOrdered, parent.getType().vApp, maxOrdered);
          break;
        case holding:
          if (parent.getSha().getTargetAltitude() > 10000)
            ts = getBoundedValueIn(minOrdered, Math.min(250, parent.getType().vCruise), maxOrdered);
          else
            ts = getBoundedValueIn(minOrdered, Math.min(220, parent.getType().vCruise), maxOrdered);
          break;
        default:
          throw new EEnumValueUnsupportedException(parent.getState());
      }
      parent.setTargetSpeed(ts);
    }

    @Override
    public void applyShortcut(Navaid navaid) {
      Pilot.this.routingModule.applyShortcut(navaid);
      Pilot.this.parent.evaluateMoodForShortcut(navaid);
    }

    @Override
    public IPilotWriteAdvanced getAdvanced() {
      return pilotWriteAdvanced;
    }

    @Override
    public IAtcModuleRO getAtcModule() {
      return Pilot.this.atcModule;
    }

    @Override
    public IBehaviorModuleRO getBehaviorModule() {
      return Pilot.this.behaviorModule;
    }

    @Override
    public IDivertModuleRO getDivertModule() {
      return Pilot.this.divertModule;
    }

    @Override
    public IAirplaneRO getPlane() {
      return Pilot.this.parent;
    }

    @Override
    public PilotRecorderModule getRecorderModule() {
      return Pilot.this.recorder;
    }

    @Override
    public IRoutingModuleRO getRoutingModule() {
      return Pilot.this.routingModule;
    }

    @Override
    public void passMessageToAtc(Atc atc, SpeechList saidText) {
      parent.sendMessage(atc, saidText);
    }

    @Override
    public void processRadarContactConfirmation() {
      Pilot.this.atcModule.setHasRadarContact();
    }

    @Override
    public void setAltitudeRestriction(Restriction restriction) {
      Pilot.this.parent.setAltitudeRestriction(restriction);
    }

    @Override
    public void setBehaviorAndState(Behavior behavior, Airplane.State state) {
      Pilot.this.behaviorModule.setBehavior(behavior);
      Pilot.this.parent.setxState(state);
    }

    @Override
    public void setNavigator(INavigator navigator) {
      parent.setNavigator(navigator);
    }

    @Override
    public void setSpeedRestriction(Restriction speedRestriction) {
      Pilot.this.parent.setSpeedRestriction(speedRestriction);
    }

    @Override
    public void setState(Airplane.State state) {
      Pilot.this.parent.setxState(state);
    }

    @Override
    public void setTargetAltitude(int altitude) {
      Pilot.this.parent.setTargetAltitude(altitude);
    }

    @Override
    public void setTargetCoordinate(Coordinate coordinate) {
      Pilot.this.parent.setNavigator(
          new ToCoordinateNavigator(coordinate));
    }

    @Override
    public void setTargetHeading(double targetHeading) {
      Pilot.this.parent.setNavigator(
          new HeadingNavigator(targetHeading));
    }

    @Override
    public void setTargetHeading(double heading, boolean isLeftTurned) {
      Pilot.this.parent.setNavigator(
          new HeadingNavigator(heading,
              isLeftTurned ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right));
    }

    @Override
    public void tuneAtc(Atc atc) {
      assert atc != null;
      Pilot.this.atcModule.changeAtc(atc);
    }
  }

  public class PilotWriteAdvanced implements IPilotWriteAdvanced {
    @Override
    public void abortHolding() {
      if (parent.getFlight().isArrival())
        Pilot.this.pilotWriteSimple.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
      else
        Pilot.this.pilotWriteSimple.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);
      Pilot.this.pilotWriteSimple.adjustTargetSpeed();
    }

    @Override
    public void addExperience(Mood.ArrivalExperience experience) {
      Pilot.this.parent.addExperience(experience);
    }

    @Override
    public void addExperience(Mood.DepartureExperience experience) {
      Pilot.this.parent.addExperience(experience);
    }

    @Override
    public void clearedToApproach(NewApproachInfo newApproachInfo) {
      // abort holding, only if fix was found
      if (parent.getState() == Airplane.State.holding) {
        Pilot.this.pilotWriteAdvanced.abortHolding();
      }

      NewApproachBehavior behavior = new NewApproachBehavior(newApproachInfo);
      Pilot.this.pilotWriteSimple.setBehaviorAndState(behavior, Airplane.State.flyingIaf2Faf);
    }

    @Override
    public void divert(boolean isInvokedByAtc) {
      if (isInvokedByAtc) {
        if (Pilot.this.parent.getEmergencyModule().isEmergency())
          Pilot.this.parent.addExperience(Mood.DepartureExperience.divertedAsEmergency);
        else if (!Acc.isSomeActiveEmergency() == false)
          Pilot.this.parent.addExperience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
        Pilot.this.divertModule.disable();
      } else {
        Pilot.this.parent.addExperience(Mood.ArrivalExperience.divertOrderedByCaptain);
      }

      Navaid divertNavaid = getDivertNavaid();
      Route route = Route.createNewVectoringByFix(divertNavaid);

      Pilot.this.parent.divert();
      Pilot.this.routingModule.setRoute(route);
      Pilot.this.pilotWriteSimple.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);

      if (!isInvokedByAtc)
        Pilot.this.pilotWriteSimple.passMessageToAtc(
            new DivertingNotification(divertNavaid));
    }

    @Override
    public void goAround(GoingAroundNotification.GoAroundReason gaReason) {
      assert gaReason != null;

      boolean isAtcFail = EnumUtils.is(gaReason,
          new GoingAroundNotification.GoAroundReason[]{
              GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
              GoingAroundNotification.GoAroundReason.noLandingClearance,
              GoingAroundNotification.GoAroundReason.incorrectApproachEnter,
              GoingAroundNotification.GoAroundReason.notStabilizedAirplane
          });
      if (isAtcFail)
        Pilot.this.parent.addExperience(
            Mood.ArrivalExperience.goAroundNotCausedByPilot);

      GoingAroundNotification gan = new GoingAroundNotification(gaReason);
      Pilot.this.pilotWriteSimple.passMessageToAtc(gan);

      NewApproachBehavior nab = Pilot.this.behaviorModule.getAs(NewApproachBehavior.class);
      NewApproachInfo nai = nab.getApproachInfo();

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude(parent.getSha().getAltitude());
      parent.setNavigator(
          new HeadingNavigator(nai.getRunwayThreshold().getCourse()));

      SpeechList<IFromAtc> gas = new SpeechList<>(nai.getGaCommands());
      this.prepareGoAroundRouting(gas, nai);
      Pilot.this.routingModule.setRoute(gas);

      Pilot.this.pilotWriteSimple.setBehaviorAndState(
          new TakeOffBehavior(
              Pilot.this.parent.getType().category,
              Pilot.this.getRoutingModule().getAssignedRunwayThreshold()),
          Airplane.State.takeOffGoAround);
    }

    @Override
    public void hold(Navaid navaid, int inboundRadial, boolean leftTurn) {
      HoldBehavior hold = new HoldBehavior(Pilot.this.pilotWriteSimple,
          navaid,
          inboundRadial,
          leftTurn);
      Pilot.this.pilotWriteSimple.setBehaviorAndState(hold, Airplane.State.holding);
    }

    @Override
    public void setRoute(SpeechList route) {
      Pilot.this.routingModule.setRoute(route);
    }

    @Override
    public void setRouting(Route route, ActiveRunwayThreshold activeRunwayThreshold) {
      Pilot.this.routingModule.setRouting(route, activeRunwayThreshold);
    }

    @Override
    public void takeOff(ActiveRunwayThreshold runwayThreshold) {
      Pilot.this.parent.setTakeOffPosition(runwayThreshold.getCoordinate());
      Pilot.this.pilotWriteSimple.setBehaviorAndState(
          new TakeOffBehavior(Pilot.this.parent.getType().category, runwayThreshold),
          Airplane.State.takeOffRoll);
      Pilot.this.parent.setTargetSpeed(
          Pilot.this.parent.getType().getV2());
      Pilot.this.parent.setNavigator(
          new HeadingNavigator(runwayThreshold.getCourse()));
    }

    private boolean isBeforeRunwayThreshold(NewApproachInfo nai) {
      double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), nai.getRunwayThreshold().getCoordinate());
      double hdg = Coordinates.getBearing(parent.getCoordinate(), nai.getRunwayThreshold().getCoordinate());
      boolean ret;
      if (dist < 3)
        ret = false;
      else {
        ret = Headings.isBetween(nai.getRunwayThreshold().getCourse() - 70, hdg, nai.getRunwayThreshold().getCourse() + 70);
      }
      return ret;
    }

    private void prepareGoAroundRouting(SpeechList<IFromAtc> gaRoute, NewApproachInfo nai) {
      ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
      if (gaRoute.get(0) instanceof ChangeAltitudeCommand) {
        cac = (ChangeAltitudeCommand) gaRoute.get(0);
        gaRoute.removeAt(0);
      }
      gaRoute.insert(0, new ChangeHeadingCommand((int) nai.getRunwayThreshold().getCourse(), ChangeHeadingCommand.eDirection.any));

      // check if is before runway threshold.
      // if is far before, then first point will still be runway threshold
      if (isBeforeRunwayThreshold(nai)) {
        String runwayThresholdNavaidName =
            nai.getRunwayThreshold().getParent().getParent().getIcao() + ":" + nai.getRunwayThreshold().getName();
        Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
        gaRoute.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
        gaRoute.insert(1, new ThenCommand());
      }

      if (cac != null)
        gaRoute.insert(0, cac);
    }
  }

  private enum ApproachLocation {
    unset,
    beforeFaf,
    beforeMapt,
    beforeThreshold,
    afterThreshold
  }

  public static final double SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER = 0.007;

  public static Pilot load(XElement tmp, Airplane.Airplane4Pilot parent) {
    throw new ToDoException();
//    Pilot ret = new Pilot();
//
//    ret.parent = parent;
//
//    LoadSave.loadField(tmp, ret, "queue");
//    LoadSave.loadField(tmp, ret, "gaReason");
//    LoadSave.loadField(tmp, ret, "divertInfo");
//    LoadSave.loadField(tmp, ret, "altitudeOrderedByAtc");
//    LoadSave.loadField(tmp, ret, "atc");
//    LoadSave.loadField(tmp, ret, "secondsWithoutRadarContact");
//    LoadSave.loadField(tmp, ret, "targetCoordinate");
//    LoadSave.loadField(tmp, ret, "speedRestriction");
//    LoadSave.loadField(tmp, ret, "altitudeRestriction");
//    LoadSave.loadField(tmp, ret, "assignedRoute");
//    LoadSave.loadField(tmp, ret, "expectedRunwayThreshold");
//    LoadSave.loadField(tmp, ret, "entryExitPoint");
//    LoadSave.loadField(tmp, ret, "afterCommands");
//    LoadSave.loadField(tmp, ret, "saidText");
//
//    {
//      XElement behEl = tmp.getChild("behavior");
//      ret.behavior = getBehaviorInstance(behEl, ret);
//      LoadSave.loadFromElement(behEl, ret.behavior);
//    }
//
//    ret.openRecorder();
//
//    return ret;
  }

  private static Behavior getBehaviorInstance(XElement behEl, Pilot parent) {
    String clsName = behEl.getAttribute("__class");
    Class cls;
    Constructor ctor;
    Behavior ret;
    try {
      cls = Class.forName(clsName);
    } catch (ClassNotFoundException e) {
      throw new EApplicationException("Unable to find behavior class " + clsName + ".", e);
    }
    try {
      ctor = cls.getDeclaredConstructor(Pilot.class);
    } catch (NoSuchMethodException e) {
      throw new EApplicationException("Unable to find parameter-less constructor for " + cls.getName() + ".", e);
    }
    try {
      ctor.setAccessible(true);
      ret = (Behavior) ctor.newInstance(parent);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new EApplicationException("Unable to create new instance of " + cls.getName() + ".", ex);
    }
    return ret;
  }

  private final IPilotWriteAdvanced pilotWriteAdvanced = new PilotWriteAdvanced();
  private final IPilotWriteSimple pilotWriteSimple = new PilotWriteSimple();
  private final IAirplaneWriteSimple parent;
  private final BehaviorModule behaviorModule = new BehaviorModule(this.pilotWriteSimple);
  private final AtcModule atcModule = new AtcModule(this.pilotWriteSimple);
  private final RoutingModule routingModule = new RoutingModule(this.pilotWriteSimple);
  private final DivertModule divertModule = new DivertModule(this.pilotWriteSimple);
  @XmlIgnore
  private PilotRecorderModule recorder;

  public Pilot(IAirplaneWriteSimple parent, Navaid entryExitPoint, @Nullable ETime divertTime) {

    this.parent = parent;
    this.routingModule.init(entryExitPoint);

    if (parent.getFlight().isArrival()) {
      this.atcModule.init(Acc.atcCtr());
      this.pilotWriteSimple.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
      this.divertModule.init(divertTime.clone());
    } else {
      this.atcModule.init(Acc.atcTwr());
      this.pilotWriteSimple.setBehaviorAndState(new HoldingPointBehavior(), Airplane.State.holdingPoint);
      this.divertModule.init(null);
    }

    this.openRecorder();
  }

  public void elapseSecond() {
    this.routingModule.elapseSecond();
    this.behaviorModule.elapseSecond();
    this.atcModule.elapseSecond();
    this.divertModule.elapseSecond();

    //printAfterCommands();
    //this.recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  public AtcModule getAtcModule() {
    return this.atcModule;
  }

  //TODO this should be in flight recorder class
  public String getBehaviorLogString() {
    throw new ToDoException();
//    if (behavior == null) {
//      return "null";
//    } else {
//      return behavior.toLogString();
//    }
  }

  public Navaid getDivertNavaid() {
    IList<Route> rts = Acc
        .atcTwr().getRunwayConfigurationInUse()
        .getDepartures()
        .where(q -> q.isForCategory(Pilot.this.parent.getType().category))
        .getRandom()
        .getThreshold()
        .getRoutes()
        .where(q -> q.getType() == Route.eType.sid);
    Route r = rts.getRandom();
    //TODO here can null-pointer-exception occur when no route is found for threshold and category
    Navaid ret = r.getMainNavaid();
    return ret;
  }

  public RoutingModule getRoutingModule() {
    return this.routingModule;
  }

  public String getStatusAsString() {
    throw new ToDoException();
//    if (this.behavior instanceof BasicBehavior)
//      return this.behavior instanceof ArrivalBehavior ? "Arriving" : "Departing";
//    else if (this.behavior instanceof HoldBehavior)
//      return "Holding";
//    else if (this.behavior instanceof NewApproachBehavior)
//      return "In approach " + this.tryGetAssignedApproach().getThreshold().getName();
//    else if (this.behavior instanceof HoldingPointBehavior)
//      return "Holding point";
//    else if (this.behavior instanceof TakeOffBehavior)
//      return "Take-off";
//    else
//      return "???";
  }

  public boolean isOnWayToPassPoint(Navaid navaid) {
    boolean ret;
    Coordinate targetCoordinate = this.parent.getPlane().getSha().tryGetTargetCoordinate();
    if (targetCoordinate != null && targetCoordinate.equals(navaid.getCoordinate()))
      ret = true;
    else if (this.behaviorModule.is(HoldBehavior.class) && this.behaviorModule.getAs(HoldBehavior.class).navaid.equals(navaid))
      ret = true;
    else
      ret = this.routingModule.isGoingToFlightOverNavaid(navaid);
    return ret;
  }

  public void raiseEmergency() {
    this.divertInfo = null;
    this.afterCommands.clearAll();
    this.behavior = new ArrivalBehavior();
    this.parent.setxState(Airplane.State.arrivingHigh);
    this.say(new EmergencyNotification());
  }

  public void save(XElement tmp) {
    LoadSave.saveField(tmp, this, "queue");
    LoadSave.saveField(tmp, this, "gaReason");
    LoadSave.saveField(tmp, this, "divertInfo");
    LoadSave.saveField(tmp, this, "altitudeOrderedByAtc");
    LoadSave.saveField(tmp, this, "atc");
    LoadSave.saveField(tmp, this, "secondsWithoutRadarContact");
    LoadSave.saveField(tmp, this, "targetCoordinate");
    LoadSave.saveField(tmp, this, "speedRestriction");
    LoadSave.saveField(tmp, this, "altitudeRestriction");
    LoadSave.saveField(tmp, this, "assignedRoute");
    LoadSave.saveField(tmp, this, "expectedRunwayThreshold");
    LoadSave.saveField(tmp, this, "entryExitPoint");
    LoadSave.saveField(tmp, this, "afterCommands");
    LoadSave.saveField(tmp, this, "saidText");
    LoadSave.saveField(tmp, this, "behavior");
  }

  public NewApproachInfo tryGetAssignedApproach() {
    NewApproachInfo ret;
    if (this.behavior instanceof NewApproachBehavior == false)
      ret = null;
    else {
      NewApproachBehavior tmp = (NewApproachBehavior) this.behavior;
      ret = tmp.getApproachInfo();
    }
    return ret;
  }

  private void adjustTargetAltitude() {
    if (altitudeRestriction != null) {
      int minAllowed;
      int maxAllowed;
      switch (altitudeRestriction.direction) {
        case exactly:
          minAllowed = altitudeRestriction.value;
          maxAllowed = altitudeRestriction.value;
          break;
        case atLeast:
          minAllowed = altitudeRestriction.value;
          maxAllowed = Integer.MAX_VALUE;
          break;
        case atMost:
          minAllowed = Integer.MIN_VALUE;
          maxAllowed = altitudeRestriction.value;
          break;
        default:
          throw new EEnumValueUnsupportedException(altitudeRestriction.direction);
      }
      int ta =
          getBoundedValueIn(minAllowed, altitudeOrderedByAtc, maxAllowed);
      parent.setTargetAltitude(ta);
    } else {
      if (parent.getTargetAltitude() != altitudeOrderedByAtc)
        parent.setTargetAltitude(altitudeOrderedByAtc);
    }
  }

  private int getBoundedValueIn(int min, int value, int max) {
    if (value < min)
      value = min;
    if (value > max)
      value = max;
    return value;
  }

  private void openRecorder() {
    this.recorder = new PilotRecorder(
        this.parent.getCallsign().toString() + " - pilot.log",
        new FileSaver(Recorder.getRecorderFileName(parent.getCallsign().toString() + "_pilot.log")),
        " \t ");
  }
}
