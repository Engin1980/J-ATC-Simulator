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
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.modules.ShaModule;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.*;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forAirplane.IAirplaneRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilot5Behavior;
import eng.jAtcSim.lib.airplanes.pilots.modules.*;
import eng.jAtcSim.lib.airplanes.pilots.navigators.HeadingNavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.INavigator;
import eng.jAtcSim.lib.airplanes.pilots.navigators.ToCoordinateNavigator;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.global.logging.FileSaver;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
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
import eng.jAtcSim.lib.world.newApproaches.Approach;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Pilot {

  public abstract class Pilot5 {
    public void passMessageToAtc(IFromAirplane speech) {
      this.passMessageToAtc(atcModule.getTunedAtc(), speech);
    }

    public void passMessageToAtc(Atc atc, SpeechList saidText) {
      parent.passMessage(atc, saidText);
    }

    public void passMessageToAtc(Atc atc, IFromAirplane content) {
      SpeechList saidText = new SpeechList();
      saidText.add(content);
      passMessageToAtc(atc, saidText);
    }

    public void updateAssignedRouting(Route newRoute, ActiveRunwayThreshold expectedRunwayThreshold) {
      routingModule.updateAssignedRouting(newRoute, expectedRunwayThreshold);
    }
  }

  public class Pilot5Command extends Pilot5 {

    public void setTargetAltitude(int altitudeInFt) {
      parent.getSha().setTargetAltitude(altitudeInFt);
    }

    public void abortHolding() {
      if (parent.getPlane().getFlight().isArrival())
        Pilot.this.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
      else
        Pilot.this.setBehaviorAndState(new DepartureBehavior(), Airplane.State.departingLow);
      adjustTargetSpeed();
    }

    public void setTargetCoordinate(Coordinate coordinate) {
      parent.getSha().setNavigator(new ToCoordinateNavigator(coordinate));
    }

    public void setTargetCoordinate(Navaid navaid) {
      assert navaid != null;
      this.setTargetCoordinate(navaid.getCoordinate());
    }

    public void setTargetHeading(double targetHeading, boolean leftTurn) {
      parent.getSha().setNavigator(
          new HeadingNavigator(
              targetHeading,
              leftTurn ? HeadingNavigator.Turn.left : HeadingNavigator.Turn.right)
      );
    }

    public void setSpeedRestriction(Restriction speedRestriction) {
      parent.getSha().setSpeedRestriction(speedRestriction);
    }

    public void processOrderedGoAround() {
      NewApproachBehavior app = Pilot.this.behaviorModule.getAs(NewApproachBehavior.class);
      app.goAround(pilot4Behavior, GoingAroundNotification.GoAroundReason.atcDecision);
    }

    public boolean isFlyingOverNavaidInFuture(Navaid navaid) {
      boolean ret = Pilot.this.isOnWayToPassPoint(navaid);
      return ret;
    }

    public void applyShortcut(Navaid navaid) {
      Pilot.this.routingModule.applyShortcut(navaid);
      Pilot.this.parent.evaluateMoodForShortcut(navaid);
    }

    public void startTakeOff(ActiveRunwayThreshold runwayThreshold) {
      Pilot.this.parent.setTakeOffPosition(runwayThreshold.getCoordinate());
      Pilot.this.pilot4Behavior.setBehaviorAndState(
          new TakeOffBehavior(Pilot.this.pilot4Behavior, runwayThreshold),
          Airplane.State.takeOffRoll);
      Pilot.this.parent.getSha().setTargetSpeed(
          Pilot.this.parent.getPlane().getType().getV2());
      Pilot.this.parent.getSha().setNavigator(
          new HeadingNavigator(runwayThreshold.getCourse()));
    }

    public void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn) {
      HoldBehavior hold = new HoldBehavior(Pilot.this.pilot4Behavior,
          navaid,
          inboundRadial,
          leftTurn);

      Pilot.this.setBehaviorAndState(hold, Airplane.State.holding);
    }

    public void setHasRadarContact() {
      atcModule.setHasRadarContact();
    }

    public void setAltitudeRestriction(Restriction restriction) {
      parent.getSha().setAltitudeRestriction(restriction);
    }

    public void processOrderedDivert() {
      if (Pilot.this.parent.getPlane().getEmergencyModule().isEmergency())
        Pilot.this.parent.getMood().experience(Mood.DepartureExperience.divertedAsEmergency);
      else if (!Acc.isSomeActiveEmergency() == false)
        Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
      Pilot.this.processDivert();
    }

    public void setResponsibleAtc(Atc atc) {
      atcModule.changeAtc(atc);
    }

    public void adviceGoAroundReasonToAtcIfAny() {
      if (gaReason != null) {
        GoingAroundNotification gan = new GoingAroundNotification(gaReason);
        passMessageToAtc(gan);
      }
    }

    public void setRoute(Route route, ActiveRunwayThreshold expectedRunwayThreshold) {
    }

    public void setApproachBehavior(NewApproachInfo nai) {
    }
  }

  public class Pilot5Module extends Pilot5 {

    public PilotRecorderModule getRecorderModule() {
      return Pilot.this.recorder;
    }

    public AtcModule getAtcModule() {
      return Pilot.this.atcModule;
    }

    public ShaModule getSha() {
      return parent.getSha();
    }

    public BehaviorModule getBehaviorModule() {
      return null;
    }

    public Pilot5Command getPilot5Command() {
      return Pilot.this.pilot5Command;
    }

    public IAirplaneRO getPlane() {
      return parent.getPlane();
    }
  }

  public class Pilot4Command {
    public void abortHolding() {
      Pilot.this.abortHolding();
    }

    public void setTargetCoordinate(Coordinate coordinate) {
      Pilot.this.targetCoordinate = coordinate;
    }

    public void setTargetCoordinate(Navaid navaid) {
      assert navaid != null;
      Pilot.this.targetCoordinate = navaid.getCoordinate();
    }

    public Restriction getSpeedRestriction() {
      return speedRestriction;
    }

    public void setSpeedRestriction(Restriction speedRestriction) {
      Pilot.this.speedRestriction = speedRestriction;
      Pilot.this.adjustTargetSpeed();
    }

    public void setApproachBehavior(NewApproachInfo approachInfo) {
      NewApproachBehavior newApproachBehavior = new NewApproachBehavior(approachInfo);
      Pilot.this.behavior = newApproachBehavior;
      Pilot.this.adjustTargetSpeed();
    }

    public void setResponsibleAtc(Atc responsibleAtc) {
      Pilot.this.atc = responsibleAtc;
      Pilot.this.secondsWithoutRadarContact = 1;
    }

    public void say(ISpeech s) {
      Pilot.this.say(s);
    }

    public void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn) {
      Pilot.this.pilot4Behavior.setHoldBehavior(navaid, inboundRadial, leftTurn);
    }

    public void setHasRadarContact() {
      Pilot.this.secondsWithoutRadarContact = 0;
    }

    public void adviceGoAroundReasonToAtcIfAny() {
      if (gaReason != null) {
        GoingAroundNotification gan = new GoingAroundNotification(gaReason);
        say(gan);
      }
    }

    public void processOrderedGoAround() {
      NewApproachBehavior app = (NewApproachBehavior) Pilot.this.beahavior;
      app.goAround(Pilot.this.pilot4Behavior, GoingAroundNotification.GoAroundReason.atcDecision);
    }


    public void setTargetAltitude(int targetAltitude) {
      boolean isNowLevelled = Math.abs(
          Pilot.this.parent.getTargetAltitude() - Pilot.this.parent.getAltitude()) < 100;
      if (isNowLevelled && Pilot.this.getTunedAtc().getType() == Atc.eType.app) {
        if (Pilot.this.parent.isArrival() &&
            Pilot.this.parent.getAltitude() < 10000 &&
            !(Pilot.this.behavior instanceof NewApproachBehavior))
          Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.leveledFlight);
        else if (Pilot.this.parent.isArrival() == false)
          Pilot.this.parent.getMood().experience(Mood.DepartureExperience.leveledFlight);
      }
      if (Pilot.this.parent.isArrival()) {
        if (targetAltitude > Pilot.this.parent.getAltitude() && !Pilot.this.isAfterGoAround)
          Pilot.this.parent.getMood().experience(Mood.SharedExperience.incorrectAltitudeChange);
      } else {
        if (targetAltitude < Pilot.this.parent.getAltitude())
          Pilot.this.parent.getMood().experience(Mood.SharedExperience.incorrectAltitudeChange);
      }

      Pilot.this.altitudeOrderedByAtc = targetAltitude;
      adjustTargetAltitude();
    }

    public void setAltitudeRestriction(Restriction altitudeRestriction) {
      Pilot.this.altitudeRestriction = altitudeRestriction;

      if (altitudeRestriction == null) {
        boolean wasAny = Pilot.this.afterCommands.clearAllAltitudeRestrictions();
        if (Pilot.this.parent.isArrival() == false && wasAny)
          Pilot.this.parent.getMood().experience(Mood.DepartureExperience.departureAltitudeRestrictionCanceled);
      }
      adjustTargetAltitude();
    }

    public void setTargetHeading(double value, boolean useLeftTurn) {
      parent.setTargetHeading(value, useLeftTurn);
    }

    public boolean isFlyingOverNavaid(Navaid navaid) {
      boolean ret = Pilot.this.isOnWayToPassPoint(navaid);
      return ret;
    }

    public boolean isFlyingOverNavaidInFuture(Navaid navaid) {
      boolean ret = Pilot.this.isOnWayToPassPointInFuture(navaid);
      return ret;
    }

    public void setRoute(Route route, ActiveRunwayThreshold expectedRunwayThreshold) {
      Pilot.this.updateAssignedRouting(route, expectedRunwayThreshold);
    }

    protected void setState(Airplane.State state) {
      parent.setxState(state);
      Pilot.this.adjustTargetSpeed();
    }
  }

  public class Pilot4Behavior implements IPilot5Behavior {
    @Override
    public void setBehaviorAndState(Behavior behavior, Airplane.State state) {
      Pilot.this.parent.setxState(state);
      Pilot.this.behavior = behavior;
      Pilot.this.adjustTargetSpeed();
    }

    @Override
    public Airplane.State getState() {
      return Pilot.this.parent.getState();
    }

    @Override
    public void setState(Airplane.State state) {
      this.setBehaviorAndState(Pilot.this.behavior, state);
    }

    @Override
    public DivertInfo getDivertInfo() {
      return Pilot.this.divertInfo;
    }

    @Override
    public void say(ISpeech speech) {
      Pilot.this.say(speech);
    }

    @Override
    public void processDivert() {
      Pilot.this.processDivert();
    }

    @Override
    public AirplaneType getAirplaneType() {
      return Pilot.this.parent.getType();
    }

    @Override
    public Coordinate getCoordinate() {
      return Pilot.this.parent.getCoordinate();
    }

    @Override
    public void setRoute(SpeechList route) {
      expandThenCommands(route);
      Pilot.this.processSpeeches(route, CommandSource.procedure);
    }

    @Override
    public boolean hasEmptyRoute() {
      return Pilot.this.afterCommands.isRouteEmpty();
    }

    @Override
    public void goAround(GoingAroundNotification.GoAroundReason reason, double initialCourse, SpeechList gaRoute) {
      ActiveRunwayThreshold threshold = Pilot.this.tryGetAssignedApproach().getThreshold();

      Pilot.this.isAfterGoAround = true;
      Pilot.this.gaReason = reason;
      Pilot.this.parent.adviceGoAroundToAtc(atc, reason);

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude((int) parent.getAltitude());
      parent.setTargetHeading(initialCourse);

      TakeOffBehavior takeOffBehavior = new TakeOffBehavior(Pilot.this.pilot4Behavior, threshold);
      this.setBehaviorAndState(takeOffBehavior, Airplane.State.takeOffGoAround);

      Pilot.this.afterCommands.clearAll();

      SpeechList<IFromAtc> gas = new SpeechList<>(gaRoute);
      ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
      if (gas.get(0) instanceof ChangeAltitudeCommand) {
        cac = (ChangeAltitudeCommand) gas.get(0);
        gas.removeAt(0);
      }
      gas.insert(0, new ChangeHeadingCommand((int) threshold.getCourse(), ChangeHeadingCommand.eDirection.any));

      // check if is before runway threshold.
      // if is far before, then first point will still be runway threshold
      if (isBeforeRunwayThreshold()) {
        String runwayThresholdNavaidName =
            threshold.getParent().getParent().getIcao() + ":" + threshold.getName();
        Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
        gas.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
        gas.insert(1, new ThenCommand());
      }

      if (cac != null)
        gas.insert(0, cac);

      expandThenCommands(gas);
      processSpeeches(gas, Pilot.CommandSource.procedure);
    }

    @Override
    public int getTargetAltitude() {
      return parent.getTargetAltitude();
    }

    @Override
    public void setTargetAltitude(double altitudeInFt) {
      Pilot.this.parent.setTargetAltitude((int) altitudeInFt);
    }

    @Override
    public void setNavigator(INavigator navigator) {
      Pilot.this.parent.setNavigator(navigator);
    }

    @Override
    public double getSpeed() {
      return Pilot.this.parent.getSpeed();
    }

    @Override
    public double getAltitude() {
      return Pilot.this.parent.getAltitude();
    }

    @Override
    public boolean isArrival() {
      return Pilot.this.parent.isArrival();
    }

    @Override
    public Coordinate getTargetCoordinate() {
      return Pilot.this.targetCoordinate;
    }

    @Override
    public void setTargetCoordinate(Coordinate coordinate) {
      Pilot.this.targetCoordinate = coordinate;
    }

    @Override
    public boolean hasLateralDirectionAfterCoordinate() {
      return Pilot.this.afterCommands.hasLateralDirectionAfterCoordinate(Pilot.this.targetCoordinate);
    }

    @Override
    public Route getAssignedRoute() {
      return Pilot.this.assignedRoute;
    }

    @Override
    public Approach getAssignedApproach() {
      return Pilot.this.tryGetAssignedApproach().getApproach();
    }

    @Override
    public double getTargetHeading() {
      return Pilot.this.parent.getTargetHeading();
    }

    @Override
    public void setTargetHeading(double targetHeading) {
      Pilot.this.parent.setTargetHeading(targetHeading);
    }

    @Override
    public boolean isEmergency() {
      return Pilot.this.parent.isEmergency();
    }

    @Override
    public void setTargetHeading(double heading, boolean isLeftTurned) {
      Pilot.this.parent.setTargetHeading(heading, isLeftTurned);
    }

    @Override
    public void experience(Mood.ArrivalExperience experience) {
      Pilot.this.parent.getMood().experience(experience);
    }

    @Override
    public void experience(Mood.DepartureExperience experience) {
      Pilot.this.parent.getMood().experience(experience);
    }

    @Override
    public double getHeading() {
      return Pilot.this.parent.getHeading();
    }

    @Override
    public void adjustTargetSpeed() {
      Pilot.this.adjustTargetSpeed();
    }

    private boolean isBeforeRunwayThreshold() {
      NewApproachInfo ai = Pilot.this.tryGetAssignedApproach();
      double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), ai.getThreshold().getCoordinate());
      double hdg = Coordinates.getBearing(parent.getCoordinate(), ai.getThreshold().getCoordinate());
      boolean ret;
      if (dist < 3)
        ret = false;
      else {
        ret = Headings.isBetween(ai.getThreshold().getCourse() - 70, hdg, ai.getThreshold().getCourse() + 70);
      }
      return ret;
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
    Pilot ret = new Pilot();

    ret.parent = parent;

    LoadSave.loadField(tmp, ret, "queue");
    LoadSave.loadField(tmp, ret, "gaReason");
    LoadSave.loadField(tmp, ret, "divertInfo");
    LoadSave.loadField(tmp, ret, "altitudeOrderedByAtc");
    LoadSave.loadField(tmp, ret, "atc");
    LoadSave.loadField(tmp, ret, "secondsWithoutRadarContact");
    LoadSave.loadField(tmp, ret, "targetCoordinate");
    LoadSave.loadField(tmp, ret, "speedRestriction");
    LoadSave.loadField(tmp, ret, "altitudeRestriction");
    LoadSave.loadField(tmp, ret, "assignedRoute");
    LoadSave.loadField(tmp, ret, "expectedRunwayThreshold");
    LoadSave.loadField(tmp, ret, "entryExitPoint");
    LoadSave.loadField(tmp, ret, "afterCommands");
    LoadSave.loadField(tmp, ret, "saidText");

    {
      XElement behEl = tmp.getChild("behavior");
      ret.behavior = getBehaviorInstance(behEl, ret);
      LoadSave.loadFromElement(behEl, ret.behavior);
    }

    ret.openRecorder();

    return ret;
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

  private Airplane.Airplane4Pilot parent;
  private final Pilot5Command pilot5Command = new Pilot5Command();
  private final Pilot5Module pilot5Module = new Pilot5Module();
  private final BehaviorModule behaviorModule = new BehaviorModule(this.pilot5Module);
  private final AtcModule atcModule = new AtcModule(this.pilot5Module);
  private final RoutingModule routingModule = new RoutingModule(this.pilot5Module);
  private final DivertModule divertModule = new DivertModule(this.pilot5Module);
  @XmlIgnore
  public final Pilot4Command pilot4Command = new Pilot4Command();
  @XmlIgnore
  public final IPilot5Behavior pilot4Behavior = new Pilot4Behavior();
  @XmlIgnore
  private GoingAroundNotification.GoAroundReason gaReason = null;
  private boolean isAfterGoAround = false;
  @XmlIgnore
  private PilotRecorderModule recorder;
  public Pilot(Airplane.Airplane4Pilot parent, Navaid entryExitPoint, @Nullable ETime divertTime) {

    this.parent = parent;
    this.routingModule.init(entryExitPoint);

    if (parent.getPlane().getFlight().isArrival()) {
      this.atcModule.init(Acc.atcCtr());
      this.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
      this.divertModule.init(divertTime.clone());
    } else {
      this.atcModule.init(Acc.atcTwr());
      this.setBehaviorAndState(new HoldingPointBehavior(), Airplane.State.holdingPoint);
      this.divertModule.init(null);
    }

    this.openRecorder();
  }

  private Pilot() {

  }

  public RoutingModule getRoutingModule() {
    return this.routingModule;
  }

  public AtcModule getAtcModule() {
    return this.atcModule;
  }

  public void elapseSecond() {
    /*

     1. zpracuji se prikazy ve fronte
     2. zkontroluje se, jestli neni neco "antecedent"
     3. ridi se letadlo
     4. ridicimu se reknou potvrzeni a zpravy

     */

    processNewSpeeches();
    processAfterSpeeches(); // udelat vlastni queue toho co se ma udelat a pak to provest pres processQueueCommands
    endrivePlane();
    requestRadarContactIfRequired();
    flushSaidTextToAtc();

    //printAfterCommands();

    this.recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  public String getStatusAsString() {
    if (this.behavior instanceof BasicBehavior)
      return this.behavior instanceof ArrivalBehavior ? "Arriving" : "Departing";
    else if (this.behavior instanceof HoldBehavior)
      return "Holding";
    else if (this.behavior instanceof NewApproachBehavior)
      return "In approach " + this.tryGetAssignedApproach().getThreshold().getName();
    else if (this.behavior instanceof HoldingPointBehavior)
      return "Holding point";
    else if (this.behavior instanceof TakeOffBehavior)
      return "Take-off";
    else
      return "???";
  }

  //TODO this should be in flight recorder class
  public String getBehaviorLogString() {
    if (behavior == null) {
      return "null";
    } else {
      return behavior.toLogString();
    }
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

  public boolean isOnWayToPassPoint(Navaid navaid) {
    boolean ret;
    Coordinate targetCoordinate = this.parent.getPlane().getSha().tryGetTargetCoordinate();
    if (targetCoordinate != null && targetCoordinate.equals(navaid.getCoordinate()))
      ret = true;
    else if (this.behaviorModule.is(HoldBehavior.class) && this.behaviorModule.getAs(HoldBehavior.class).navaid.equals(navaid))
      ret = true;
    else
      ret = this.routingModule.isOnWayToPassPoint(navaid);
    return ret;
  }

  @Deprecated//("Use routing module directly instead")
  public boolean isOnWayToPassPointInFuture(Navaid navaid) {
    boolean ret = this.routingModule.isOnWayToPassPoint(navaid);
    return ret;
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

  private void setBehaviorAndState(Behavior behavior, Airplane.State state) {
    this.behaviorModule.setBehavior(behavior);
    this.parent.setxState(state);
  }

  private void openRecorder() {
    this.recorder = new PilotRecorder(
        this.parent.getCallsign().toString() + " - pilot.log",
        new FileSaver(Recorder.getRecorderFileName(parent.getCallsign().toString() + "_pilot.log")),
        " \t ");
  }

  /**
   * Divert ordered by the captain of the airplane (when time runs out)
   */
  private void processDivert() {

    Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.divertOrderedByCaptain);

    Navaid n = getDivertNavaid();

    this.parent.divert();
    this.afterCommands.clearAll();
    this.behavior = new DepartureBehavior();
    this.divertInfo = null;
    this.assignedRoute = Route.createNewVectoringByFix(n);
    this.parent.setxState(Airplane.State.departingLow); // here must be departureLow, this us later used to evaluate delay

    this.say(new DivertingNotification(n));

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

  private void adjustTargetSpeed() {
    int minOrdered;
    int maxOrdered;
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
        ts = 0;
        break;
      case takeOffRoll:
        ts = parent.getType().vR + 10;
        break;
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
        if (this.parent.getTargetAltitude() > 10000)
          ts = getBoundedValueIn(minOrdered, Math.min(250, parent.getType().vCruise), maxOrdered);
        else
          ts = getBoundedValueIn(minOrdered, Math.min(220, parent.getType().vCruise), maxOrdered);
        break;
      case landed:
        ts = 0;
        break;
      default:
        throw new EEnumValueUnsupportedException(parent.getState());
    }
    this.parent.setTargetSpeed(ts);
  }

  private int getBoundedValueIn(int min, int value, int max) {
    if (value < min)
      value = min;
    if (value > max)
      value = max;
    return value;
  }

  private void endrivePlane() {
    this.behavior.fly(this.pilot4Behavior);
  }

}
