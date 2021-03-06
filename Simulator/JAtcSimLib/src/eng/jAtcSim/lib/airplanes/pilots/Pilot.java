/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import com.sun.istack.internal.Nullable;
import eng.eSystem.EStringBuilder;
import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.ConversionUtils;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirproxType;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.lib.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.atcs.Atc;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.*;
import eng.jAtcSim.lib.global.logging.AbstractSaver;
import eng.jAtcSim.lib.global.logging.FileSaver;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.IllegalThenCommandRejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.RunwayThreshold;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.CurrentApproachInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class Pilot {

  static class DivertInfo {
    public ETime divertTime;
    public int lastAnnouncedMinute = Integer.MAX_VALUE;

    @XmlConstructor
    private DivertInfo() {
    }

    public DivertInfo(ETime divertTime) {
      this.divertTime = divertTime;
    }

    public int getMinutesLeft() {
      int diff = divertTime.getTotalMinutes() - Acc.now().getTotalMinutes();
      return diff;
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

    public void setApproachBehavior(CurrentApproachInfo app) {
      Pilot.this.behavior = Pilot.this.new ApproachBehavior(app);
      Pilot.this.adjustTargetSpeed();

    }

    public void setResponsibleAtc(Atc responsibleAtc) {
      Pilot.this.atc = responsibleAtc;
      Pilot.this.secondsWithoutRadarContact = 1;
    }

    public void say(ISpeech s) {
      Pilot.this.say(s);
    }

    public void applyShortcut(Navaid n) {
      SpeechList<IFromAtc> skippedCommands = Pilot.this.afterCommands.doShortcutTo(n);
      Pilot.this.processSpeeches(skippedCommands, CommandSource.procedure);
      Pilot.this.parent.evaluateMoodForShortcut(n);
    }

    public void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn) {
      Pilot.this.setHoldBehavior(navaid, inboundRadial, leftTurn);
    }

    public void setTakeOffBehavior(RunwayThreshold thrs) {
      Pilot.this.behavior = new Pilot.TakeOffBehavior(thrs);
      Pilot.this.parent.setTargetSpeed(Pilot.this.parent.getType().getV2());
      Pilot.this.parent.setTargetHeading(thrs.getCourse());
      Pilot.this.parent.setxState(Airplane.State.takeOffRoll);
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
      ApproachBehavior app = (ApproachBehavior) Pilot.this.behavior;
      app.goAround(GoingAroundNotification.GoAroundReason.atcDecision);
    }

    public int getDivertMinutesLeft() {
      return Pilot.this.divertInfo.getMinutesLeft();
    }

    public void processOrderedDivert() {
      if (Pilot.this.parent.isEmergency())
        Pilot.this.parent.getMood().experience(Mood.DepartureExperience.divertedAsEmergency);
      else if (!Acc.isSomeActiveEmergency() == false)
        Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.divertOrderedByAtcWhenNoEmergency);
      Pilot.this.processDivert();
    }

    public void setTargetAltitude(int targetAltitude) {
      boolean isNowLevelled = Math.abs(
          Pilot.this.parent.getTargetAltitude() - Pilot.this.parent.getAltitude()) < 100;
      if (isNowLevelled && Pilot.this.getTunedAtc().getType() == Atc.eType.app) {
        if (Pilot.this.parent.isArrival() &&
            Pilot.this.parent.getAltitude() < 10000 &&
            !(Pilot.this.behavior instanceof ApproachBehavior))
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

    public void setRoute(Route route, RunwayThreshold expectedRunwayThreshold) {
      Pilot.this.updateAssignedRouting(route, expectedRunwayThreshold);
    }

    protected void setState(Airplane.State state) {
      parent.setxState(state);
      Pilot.this.adjustTargetSpeed();
    }
  }

  abstract class Behavior {

    @XmlIgnore
    public final Pilot pilot;
    @XmlIgnore
    public final Airplane.Airplane4Pilot airplane;

    public Behavior() {
      this.pilot = Pilot.this;
      this.airplane = Pilot.this.parent;
    }

    public abstract void fly();

    public abstract String toLogString();

    protected void setBehaviorAndState(
        Behavior behavior, Airplane.State state) {
      Pilot.this.behavior = behavior;
      this.setState(state);
    }

    protected void setState(Airplane.State state) {
      parent.setxState(state);
      Pilot.this.adjustTargetSpeed();
    }

    protected void throwIllegalStateException() {
      throw new ERuntimeException(
          "Illegal state " + airplane.getState() + " for behavior " + this.getClass().getSimpleName() + "."
      );
    }

  }

  abstract class DivertableBehavior extends Behavior {

    private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};

    protected void adviceDivertTimeIfRequested() {
      DivertInfo di = Pilot.this.divertInfo;
      if (di == null) return;

      for (int dit : divertAnnounceTimes) {
        int minLeft = di.getMinutesLeft();
        if (di.lastAnnouncedMinute > dit && minLeft < dit) {
          Pilot.this.say(
              new DivertTimeNotification(di.getMinutesLeft()));
          di.lastAnnouncedMinute = minLeft;
          break;
        }
      }
    }

    protected boolean divertIfRequested() {
      if (divertInfo != null && divertInfo.getMinutesLeft() <= 0) {
        Pilot.this.processDivert();
        return true;
      } else {
        return false;
      }
    }
  }

  class HoldingPointBehavior extends Behavior {

    @Override
    public void fly() {

    }

    @Override
    public String toLogString() {
      return "{HP}";
    }
  }

  class TakeOffBehavior extends Behavior {

    //TODO add to airport config the acceleration altitude and use it here
    private final int accelerationAltitude;
    private RunwayThreshold toThreshold;

    private TakeOffBehavior() {
      accelerationAltitude = 0;
    }

    public TakeOffBehavior(RunwayThreshold toThreshold) {

      this.toThreshold = toThreshold;
      int accAlt;
      switch (parent.getType().category) {
        case 'A':
          accAlt = 300;
          break;
        case 'B':
          accAlt = 1000;
          break;
        case 'C':
        case 'D':
          accAlt = 1500;
          break;
        default:
          throw new EEnumValueUnsupportedException(parent.getType().category);
      }
      this.accelerationAltitude = Acc.airport().getAltitude() + accAlt;
    }

    @Override
    public void fly() {
      switch (parent.getState()) {
        case holdingPoint:
          break;
        case takeOffRoll:
          double targetHeading = Coordinates.getBearing(
              parent.getCoordinate(), toThreshold.getOtherThreshold().getCoordinate());
          parent.setTargetHeading(targetHeading);

          if (parent.getSpeed() > parent.getType().vR) {
            super.setState(Airplane.State.takeOffGoAround);
          }
          break;
        case takeOffGoAround:
          // keeps last heading
          // altitude already set
          // speed set
          if (parent.getAltitude() > this.accelerationAltitude)
            if (parent.isArrival()) {
              // antecedent G/A
              super.setBehaviorAndState(new ArrivalBehavior(), Airplane.State.arrivingHigh);
            } else {
              super.setBehaviorAndState(
                  new DepartureBehavior(),
                  Airplane.State.departingLow
              );
            }
          break;
        default:
          super.throwIllegalStateException();
      }
    }

    @Override
    public String toLogString() {
      return "TKO";
    }

  }

  abstract class BasicBehavior extends DivertableBehavior {
    private boolean clearanceLimitWarningSent = false;

    abstract void _fly();

    @Override
    public void fly() {
      if (targetCoordinate != null) {

        double warningDistance = Pilot.this.parent.getSpeed() * .02;
        double overNavaidDistance = Pilot.this.parent.getSpeed() * SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER;

        double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), targetCoordinate);
        if (!clearanceLimitWarningSent && dist < warningDistance && !pilot.afterCommands.hasLateralDirectionAfterCoordinate(targetCoordinate)) {
          say(new PassingClearanceLimitNotification());
          clearanceLimitWarningSent = true;
        } else if (dist < overNavaidDistance) {
          if (parent.isArrival() == false) {
            Navaid n = Pilot.this.assignedRoute.getMainNavaid();
            dist = Coordinates.getDistanceInNM(parent.getCoordinate(), n.getCoordinate());
            if (dist < 1.5) {
              int rad = (int) Coordinates.getBearing(Acc.airport().getLocation(), n.getCoordinate());
              rad = rad % 90;
              Pilot.this.setHoldBehavior(n, rad, true);
              return;
            }
          } else {
            targetCoordinate = null;
            clearanceLimitWarningSent = false;
          }
        } else {
          double heading = Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
          heading = Headings.to(heading);
          if (heading != parent.getTargetHeading()) {
            parent.setTargetHeading(heading);
          }
        }
      }
      _fly();
    }


  }

  class ArrivalBehavior extends BasicBehavior {

    private final static double LOW_SPEED_DOWN_ALTITUDE = 11000;
    private final static double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;

    @Override
    void _fly() {
      switch (parent.getState()) {
        case arrivingHigh:
          if (parent.getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
            super.setState(Airplane.State.arrivingLow);
          else {
            double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
                .getArrivals().where(q -> q.isForCategory(Pilot.this.parent.getType().category))
                .minDouble(q -> Coordinates.getDistanceInNM(parent.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
            if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
              super.setState(Airplane.State.arrivingCloseFaf);
            }
          }
          break;
        case arrivingLow:
          // TODO this will not work for runways with FAF above FL100
          double distToFaf = Acc.atcTwr().getRunwayConfigurationInUse()
              .getArrivals().where(q -> q.isForCategory(Pilot.this.parent.getType().category))
              .minDouble(q -> Coordinates.getDistanceInNM(parent.getCoordinate(), q.getThreshold().getEstimatedFafPoint()));
          if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
            super.setState(Airplane.State.arrivingCloseFaf);
          }
          break;
        case arrivingCloseFaf:
          break;
        default:
          super.throwIllegalStateException();
      }

      if (!parent.isEmergency())
        if (this.divertIfRequested() == false)
          this.adviceDivertTimeIfRequested();
    }


    @Override
    public String toLogString() {
      return "ARR";
    }

  }

  class DepartureBehavior extends BasicBehavior {

    @Override
    public void _fly() {
      switch (parent.getState()) {
        case departingLow:
          if (parent.getAltitude() > 10000) super.setState(Airplane.State.departingHigh);
          break;
        case departingHigh:
          break;
        default:
          super.throwIllegalStateException();
      }
    }

    @Override
    public String toLogString() {
      return "DEP";
    }
  }

  class HoldBehavior extends DivertableBehavior {

    private static final double NEAR_FIX_DISTANCE = 0.5;

    public Boolean isBelowFL100 = null;
    public Navaid navaid;
    public int inboundRadial;
    public eHoldPhase phase;
    public ETime secondTurnTime;
    public boolean isLeftTurned;
    private static final int FL100 = 10000;

    public int getAfterSecondTurnHeading() {
      int ret;
      if (isLeftTurned)
        ret = (int) Headings.to(inboundRadial + 30);
      else
        ret = (int) Headings.to(inboundRadial - 30);
      return ret;
    }

    private double getOutboundHeading() {
      return Headings.add(inboundRadial, 180);
    }

    private void setHoldDataByEntry() {
      double y = Coordinates.getBearing(parent.getCoordinate(), this.navaid.getCoordinate());
      y = Headings.add(y, 180);

      int h = this.inboundRadial;
      double a;
      double b;
      if (this.isLeftTurned) {
        a = Headings.add(h, -110);
        b = Headings.add(h, 75);
        if (Headings.isBetween(a, y, h))
          this.phase = eHoldPhase.parallelEntry;
        else if (Headings.isBetween(h, y, b))
          this.phase = eHoldPhase.tearEntry;
        else
          this.phase = eHoldPhase.directEntry;
      } else {
        a = Headings.add(h, -75);
        b = Headings.add(h, 110);
        if (Headings.isBetween(a, y, h))
          this.phase = eHoldPhase.tearEntry;
        else if (Headings.isBetween(h, y, b))
          this.phase = eHoldPhase.parallelEntry;
        else
          this.phase = eHoldPhase.directEntry;
      }
    }

    @Override
    public void fly() {
      if (parent.getState() != Airplane.State.holding)
        super.throwIllegalStateException();

      if (this.phase == eHoldPhase.beginning) {
        setHoldDataByEntry();
      }

      switch (this.phase) {
        case directEntry:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
            this.phase = eHoldPhase.firstTurn;
          } else {
            int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), this.navaid.getCoordinate());
            parent.setTargetHeading(newHeading);
          }
          break;
        case inbound:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
            this.phase = eHoldPhase.firstTurn;
            if (Pilot.this.parent.isArrival())
              Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.holdCycleFinished);
            else
              Pilot.this.parent.getMood().experience(Mood.DepartureExperience.holdCycleFinished);
          } else {
            double newHeading = Coordinates.getHeadingToRadial(
                parent.getCoordinate(), this.navaid.getCoordinate(), this.inboundRadial,
                Coordinates.eHeadingToRadialBehavior.standard);
            parent.setTargetHeading(newHeading);

          }
          break;
        case firstTurn:
          if (parent.getTargetHeading() == parent.getHeading()) {
            this.secondTurnTime = Acc.now().addSeconds(60);
            this.phase = eHoldPhase.outbound;
          }
          break;
        case outbound:
          if (Acc.now().isAfter(this.secondTurnTime)) {
            parent.setTargetHeading(this.getAfterSecondTurnHeading(), this.isLeftTurned);
            this.phase = eHoldPhase.secondTurn;
          }
          break;
        case secondTurn:
          if (parent.getTargetHeading() == parent.getHeading()) {
            this.phase = eHoldPhase.inbound;
          }
          break;

        case tearEntry:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {

            double newHeading;
            newHeading = this.isLeftTurned
                ? Headings.add(this.inboundRadial, -150)
                : Headings.add(this.inboundRadial, 150);
            parent.setTargetHeading(newHeading);
            this.secondTurnTime = Acc.now().addSeconds(120);

            this.phase = eHoldPhase.tearAgainst;
          } else {
            double newHeading = Coordinates.getBearing(parent.getCoordinate(), this.navaid.getCoordinate());
            parent.setTargetHeading(newHeading);
          }
          break;

        case tearAgainst:
          if (Acc.now().isAfter(this.secondTurnTime)) {
            this.secondTurnTime = null;
            parent.setTargetHeading(this.inboundRadial, this.isLeftTurned);
            this.phase = eHoldPhase.secondTurn;
          }
          break;

        case parallelEntry:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.navaid.getCoordinate()) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), !this.isLeftTurned);
            this.secondTurnTime = Acc.now().addSeconds(60);
            this.phase = eHoldPhase.parallelAgainst;
          } else {
            int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), this.navaid.getCoordinate());
            parent.setTargetHeading(newHeading);
          }
          break;

        case parallelAgainst:
          if (Acc.now().isAfter(this.secondTurnTime)) {
            double newHeading = (this.isLeftTurned)
                ? Headings.add(this.getOutboundHeading(), -210)
                : Headings.add(this.getOutboundHeading(), +210);
            parent.setTargetHeading(newHeading, !this.isLeftTurned);
            this.phase = eHoldPhase.parallelTurn;
          }
          break;

        case parallelTurn:
          if (parent.getHeading() == parent.getTargetHeading()) {
            this.phase = eHoldPhase.directEntry;
          }
          break;

        default:
          throw new EEnumValueUnsupportedException(this.phase);
      }

      if (!parent.isEmergency())
        if (this.divertIfRequested() == false)
          this.adviceDivertTimeIfRequested();

      if (isBelowFL100 == null) {
        isBelowFL100 = parent.getAltitude() <= FL100;
      } else if (isBelowFL100 && parent.getAltitude() > FL100)
      {
        Pilot.this.adjustTargetSpeed();
        isBelowFL100 = parent.getAltitude() <= FL100;
      } else if (!isBelowFL100 && parent.getAltitude() <= FL100){
        Pilot.this.adjustTargetSpeed();
        isBelowFL100 = parent.getAltitude() <= FL100;
      }
    }

    @Override
    public String toLogString() {

      EStringBuilder sb = new EStringBuilder();

      sb.appendFormat("HLD %s incrs: %03d/%s in: %s",
          this.navaid.getName(),
          this.inboundRadial,
          this.isLeftTurned ? "L" : "R",
          this.phase.toString());

      return sb.toString();
    }
  }

  class ApproachBehavior extends Behavior {

    public static final double DEGREES_TO_RADS = .0174532925;
    private final static int LONG_FINAL_ALTITUDE_AGL = 1000;
    private final static int SHORT_FINAL_ALTITUDE_AGL = 200;
    private final int finalAltitude;
    private final int shortFinalAltitude;

    public CurrentApproachInfo approach;
    private boolean isAfterStateChange;
    private boolean isAtFinalDescend = false;
    private ApproachLocation location = ApproachLocation.unset;
    private double slope = -1;

    private ApproachBehavior() {
      finalAltitude = 0;
      shortFinalAltitude = 0;
    }

    public ApproachBehavior(CurrentApproachInfo approach) {
      this.approach = approach;
      this.finalAltitude = Acc.airport().getAltitude() + LONG_FINAL_ALTITUDE_AGL;
      this.shortFinalAltitude = Acc.airport().getAltitude() + SHORT_FINAL_ALTITUDE_AGL;
      this.isAfterStateChange = true;
      Pilot.this.gaReason = null;

      if (approach.getIafRoute().isEmpty() == false) {
        expandThenCommands(approach.getIafRoute());
        Pilot.this.processSpeeches(approach.getIafRoute(), CommandSource.procedure);
        this.setState(Airplane.State.flyingIaf2Faf);
      } else {
        SpeechList<IFromAtc> tmp = new SpeechList();
        tmp.add(new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, approach.getInitialAltitude()));
        Pilot.this.processSpeeches(tmp, CommandSource.procedure);
        this.setState(Airplane.State.approachEnter);
      }
    }

    public void goAround(GoingAroundNotification.GoAroundReason reason) {
      assert reason != null;

      Pilot.this.isAfterGoAround = true;
      boolean isAtcFail = EnumUtils.is(reason,
          new GoingAroundNotification.GoAroundReason[]{
              GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach,
              GoingAroundNotification.GoAroundReason.noLandingClearance,
              GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter,
              GoingAroundNotification.GoAroundReason.notStabilizedOnFinal
          });
      if (isAtcFail)
        Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.goAroundNotCausedByPilot);

      Pilot.this.gaReason = reason;
      parent.adviceGoAroundToAtc(atc, reason);

      super.setBehaviorAndState(
          new TakeOffBehavior(null), Airplane.State.takeOffGoAround);

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude((int) parent.getAltitude());
      parent.setTargetHeading(approach.getCourse());

      Pilot.this.afterCommands.clearAll();

      SpeechList<IFromAtc> gas = new SpeechList<>(this.approach.getGaRoute());
      ChangeAltitudeCommand cac = null; // remember climb command and add it as first at the end
      if (gas.get(0) instanceof ChangeAltitudeCommand) {
        cac = (ChangeAltitudeCommand) gas.get(0);
        gas.removeAt(0);
      }
      gas.insert(0, new ChangeHeadingCommand((int) this.approach.getThreshold().getCourse(), ChangeHeadingCommand.eDirection.any));

      // check if is before runway threshold.
      // if is far before, then first point will still be runway threshold
      if (isBeforeRunwayThreshold()) {
        String runwayThresholdNavaidName =
            this.approach.getThreshold().getParent().getParent().getIcao() + ":" + this.approach.getThreshold().getName();
        Navaid runwayThresholdNavaid = Acc.area().getNavaids().getOrGenerate(runwayThresholdNavaidName);
        gas.insert(0, new ProceedDirectCommand(runwayThresholdNavaid));
        gas.insert(1, new ThenCommand());
      }

      if (cac != null)
        gas.insert(0, cac);

      expandThenCommands(gas);
      processSpeeches(gas, CommandSource.procedure);
    }

    private boolean isBeforeRunwayThreshold() {
      double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), this.approach.getThreshold().getCoordinate());
      double hdg = Coordinates.getBearing(parent.getCoordinate(), this.approach.getThreshold().getCoordinate());
      boolean ret;
      if (dist < 3)
        ret = false;
      else {
        ret = Headings.isBetween(this.approach.getCourse() - 70, hdg, this.approach.getCourse() + 70);
      }
      return ret;
    }

    private double getMinimalAllowedAltitudeAfterThisStep() {
      double maxVS;
      switch (Pilot.this.parent.getState()) {
        case longFinal:
          maxVS = -1500;
          break;
        case shortFinal:
          maxVS = -1100;
          break;
        default:
          maxVS = -2500;

      }
      double ret = Pilot.this.parent.getAltitude() + maxVS / 60d;
      return ret;
    }

    private boolean updateAltitudeOnApproach(boolean checkIfIsAfterThreshold) {
      int currentTargetAlttiude = parent.getTargetAltitude();
      double distToLand;
      int newAltitude;
      if (location == ApproachLocation.afterThreshold) {
        newAltitude = Acc.airport().getAltitude() - 100; // I need to lock the airplane on runway
      } else {
        int minAltByState = 0; // (int) getMinimalAllowedAltitudeAfterThisStep();
        switch (approach.getType()) {
          case visual:
            if (location == ApproachLocation.beforeFaf) {
              // TODO check and evaluate
              // experimental, trying to fix descend rate after FAF to lower values
              // newAltitude = parent.getTargetAltitude();
              newAltitude = (int) Math.max(parent.getTargetAltitude(), parent.getAltitude() - 1000);
            } else {
              double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
              double delta = dist * this.approach.getSlope();
              newAltitude = (int) delta + Acc.airport().getAltitude();
            }
            break;
          default:
            if (location == ApproachLocation.beforeFaf)
              newAltitude = parent.getTargetAltitude();
            else {
              double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getMapt());
              double delta = dist * this.approach.getSlope();
              newAltitude = (int) delta + Acc.airport().getAltitude();
            }
        }
        newAltitude = Math.max(newAltitude, minAltByState);
        newAltitude = Math.min(newAltitude, parent.getTargetAltitude());
        if (location == ApproachLocation.beforeMapt)
          newAltitude = Math.max(newAltitude, approach.getDecisionAltitude());
        newAltitude = Math.max(newAltitude, Acc.airport().getAltitude());
      }
      parent.setTargetAltitude(newAltitude);
      boolean ret = (location != ApproachLocation.beforeFaf) && (currentTargetAlttiude > parent.getTargetAltitude());

      return ret;
    }

    private boolean isBehindFaf() {
      double courseToFaf = Coordinates.getBearing(parent.getCoordinate(), approach.getFaf());
      double diff = Headings.getDifference(courseToFaf, approach.getThreshold().getCourse(), true);
      boolean ret = diff > 90;
      return ret;
    }

    private boolean isPassingFaf() {
      double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getFaf());
      boolean ret = dist < 1.0;
      return ret;
    }

    private boolean isBehindThreshold() {
      double course = Coordinates.getBearing(parent.getCoordinate(), approach.getThreshold().getCoordinate());
      boolean ret = Headings.getDifference(course, approach.getThreshold().getCourse(), true) > 90;
      return ret;
    }

    private boolean isBehindMAPt() {
      double courseToFaf = Coordinates.getBearing(parent.getCoordinate(), approach.getMapt());
      boolean ret = Headings.getDifference(courseToFaf, approach.getThreshold().getCourse(), true) > 90;
      return ret;
    }

    private void updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior radialBehavior) {
      double newHeading;
      Coordinate planePos = parent.getCoordinate();
      if (location == ApproachLocation.beforeFaf) {
        newHeading = Coordinates.getBearing(planePos, approach.getFaf());
      } else if (location == ApproachLocation.beforeMapt) {
        Coordinate point = approach.getMapt();
        double course = approach.getFaf2MaptCourse();
        newHeading = Coordinates.getHeadingToRadial(
            planePos, point, course, radialBehavior);
      } else if (location == ApproachLocation.beforeThreshold) {
        double dist = Coordinates.getDistanceInNM(planePos, this.approach.getThreshold().getCoordinate());
        if (dist < 2)
          newHeading = Coordinates.getBearing(planePos, this.approach.getThreshold().getCoordinate());
        else
          newHeading = Coordinates.getHeadingToRadial(planePos, this.approach.getThreshold().getCoordinate(), this.approach.getThreshold().getCourse(), radialBehavior);
      } else {
        // afther threshold
        newHeading = (int) Coordinates.getBearing(planePos, this.approach.getThreshold().getOtherThreshold().getCoordinate());
      }
      parent.setTargetHeading(newHeading);
    }

    private boolean canSeeRunwayFromCurrentPosition() {
      Weather w = Acc.weather();
      if ((w.getCloudBaseInFt() + Acc.airport().getAltitude()) < parent.getAltitude()) {
        return false;
      }
      double d = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
      if (w.getVisibilityInMilesReal() < d) {
        return false;
      }
      return true;
    }

    private void updateApproachLocation(boolean isPrecise) {
      if (location == ApproachLocation.unset) {
        if (isPassingFaf() || isPrecise)
          location = ApproachLocation.beforeMapt;
        else
          location = ApproachLocation.beforeFaf;
      } else if (location == ApproachLocation.beforeFaf && isPassingFaf())
        location = ApproachLocation.beforeMapt;
      else if (location == ApproachLocation.beforeMapt && isBehindMAPt())
        location = ApproachLocation.beforeThreshold;
      else if (location == ApproachLocation.beforeThreshold && isBehindThreshold())
        location = ApproachLocation.afterThreshold;
    }

    private void flyIAFtoFAFPhase() {
      if (targetCoordinate != null) {

        double heading = Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
        heading = Headings.to(heading);
        if (heading != parent.getTargetHeading()) {
          parent.setTargetHeading(heading);
        }
      }

      if (Pilot.this.afterCommands.isRouteEmpty()) {
        this.setState(Airplane.State.approachEnter);
        this.isAfterStateChange = true;
        // TODO here he probably should again check the position against the runway
      }
    }

    private void flyApproachingPhase() {

      switch (parent.getState()) {
        case approachDescend:
        case longFinal:
        case shortFinal:
          if (parent.getAirprox() == AirproxType.full) {
            goAround(GoingAroundNotification.GoAroundReason.lostTrafficSeparationInApproach);
            return;
          }
      }

      ApproachLocation last = this.location;
      updateApproachLocation(this.approach.isPrecise());

      if (last == ApproachLocation.beforeMapt && this.location == ApproachLocation.beforeThreshold) {
        if (canSeeRunwayFromCurrentPosition() == false) {
          goAround(GoingAroundNotification.GoAroundReason.runwayNotInSight);
          return;
        }
      }

      switch (parent.getState()) {

        case flyingIaf2Faf:
          throw new UnsupportedOperationException("Not supposed to be here. See flyIAFtoFAFPhase()");

        case approachEnter:
          if (isAfterStateChange && this.approach.getType() == Approach.ApproachType.visual) {
            if (canSeeRunwayFromCurrentPosition() == false) {
              goAround(GoingAroundNotification.GoAroundReason.runwayNotInSight);
              return;
            }
          }
          isAfterStateChange = false;
          // this is when app is cleared for approach
          // this only updates speed and changes to "entering"
          updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.standard);
          boolean isDescending = updateAltitudeOnApproach(false);
          if (isDescending) {
            isAfterStateChange = true;
            super.setState(Airplane.State.approachDescend);
          }
          break;
        case approachDescend:
          if (isAfterStateChange) {
            if (this.approach.isPrecise()) {
              // check if not descending to ILS path and not yet established in ILS LOC
              if (Headings.getDifference(
                  parent.getTargetHeading(), this.approach.getFaf2MaptCourse(), true) > 15) {
                goAround(GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter);
                return;
              }
              // check if should descend but is not leveled at initial altitude
              if (parent.getAltitude() > this.approach.getInitialAltitude() + 100) {
                goAround(GoingAroundNotification.GoAroundReason.notStabilizedApproachEnter);
                return;
              }
            }
            this.isAfterStateChange = false;
          }
          // plane on descend slope
          // updates speed, then changes to "descending"
          isAfterStateChange = false;
          updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.standard);
          updateAltitudeOnApproach(false);
          if (parent.getAltitude() < this.finalAltitude) {
            isAfterStateChange = true;
            super.setState(Airplane.State.longFinal);
          }
          break;
        case longFinal:
          // plane under final altitude
          // yells if it have not own speed or if not switched to atc
          // TODO see above
          updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.aggresive);
          updateAltitudeOnApproach(false);

          if (isAfterStateChange) {
            // moc nizko, uz pod stabilized altitude
            int MAX_LONG_FINAL_HEADING_DIFF = 30;
            if (Math.abs(parent.getTargetHeading() - this.approach.getCourse()) > MAX_LONG_FINAL_HEADING_DIFF) {
              goAround(GoingAroundNotification.GoAroundReason.notStabilizedOnFinal);
              return;
            }

            // neni na twr, tak GA
            if (pilot.atc != Acc.atcTwr()) {
              parent.adviceToAtc(atc, new EstablishedOnApproachNotification(this.approach.getThreshold()));
            }
            isAfterStateChange = false;
          }

          if (parent.getAltitude() < this.shortFinalAltitude) {
            isAfterStateChange = true;
            super.setState(Airplane.State.shortFinal);
          }
          break;
        case shortFinal:
          updateAltitudeOnApproach(true);
          updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.aggresive);
          if (isAfterStateChange) {
            int MAX_SHORT_FINAL_HEADING_DIFF = 10;
            double diff = Math.abs(parent.getTargetHeading() - this.approach.getCourse());
            if (diff > MAX_SHORT_FINAL_HEADING_DIFF) {
              goAround(GoingAroundNotification.GoAroundReason.notStabilizedOnFinal);
              return;
            }

            // neni na twr, tak GA
            if (pilot.atc != Acc.atcTwr()) {
              goAround(GoingAroundNotification.GoAroundReason.noLandingClearance);
              return;
            }
            isAfterStateChange = false;
          }

          if (parent.getAltitude() == Acc.airport().getAltitude()) {
            double gaProbability = getGoAroundProbabilityDueToWind();
            if (Acc.rnd().nextDouble() < gaProbability) {
              goAround(GoingAroundNotification.GoAroundReason.windGustBeforeTouchdown);
              return;
            } else {
              isAfterStateChange = true;
              super.setState(Airplane.State.landed);
            }
          }
          break;
        case landed:
          if (Pilot.this.parent.isEmergency())
            Pilot.this.parent.getMood().experience(Mood.ArrivalExperience.landedAsEmergency);
          isAfterStateChange = false;
          updateHeadingOnApproach(Coordinates.eHeadingToRadialBehavior.gentle);
          break;
        default:
          super.throwIllegalStateException();
      }
    }

    private double getGoAroundProbabilityDueToWind() {
      double windGustBase =
          Acc.weather().getWindGustSpeedInKts() - Acc.weather().getWindSpeetInKts();
      double windRelativeHeading =
          Headings.getDifference(Pilot.this.parent.getHeading(), Acc.weather().getWindHeading(), true);
      double gaProbability = 0;
      if (windGustBase > 0) {
        double windCauseGoAroundProbability =
            Math.sin(windRelativeHeading * DEGREES_TO_RADS);
        if (windRelativeHeading > 90)
          windCauseGoAroundProbability += 2 * Math.sin((windRelativeHeading - 90) * DEGREES_TO_RADS);
        windCauseGoAroundProbability /= 2;
        windCauseGoAroundProbability *= windGustBase;
        gaProbability = windCauseGoAroundProbability;
      }
      return gaProbability;
    }

    @Override
    public void fly() {
      if (parent.getState() == Airplane.State.flyingIaf2Faf) {
        flyIAFtoFAFPhase();
      } else
        flyApproachingPhase();
    }


    @Override
    public String toLogString() {

      EStringBuilder sb = new EStringBuilder();

      sb.appendFormat("APP %s%s",
          this.approach.getType().toString(),
          this.approach.getThreshold().getName());

      return sb.toString();
    }
  }

  private enum ApproachLocation {
    unset,
    beforeFaf,
    beforeMapt,
    beforeThreshold,
    afterThreshold
  }

  private enum CommandSource {
    procedure,
    atc,
    route,
    extension
  }

  public static final double SPEED_TO_OVER_NAVAID_DISTANCE_MULTIPLIER = 0.007;
  private final DelayedList<ISpeech> queue = new DelayedList<>(2, 7); //Min/max item delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final Map<Atc, SpeechList> saidText = new HashMap<>();
  @XmlIgnore
  private Airplane.Airplane4Pilot parent;
  private GoingAroundNotification.GoAroundReason gaReason = null;
  private DivertInfo divertInfo;
  private int altitudeOrderedByAtc;
  private Atc atc;
  private int secondsWithoutRadarContact;
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private Route assignedRoute;
  private RunwayThreshold expectedRunwayThreshold;
  private Navaid entryExitPoint;
  private Restriction speedRestriction = null;
  private Restriction altitudeRestriction = null;
  private boolean isAfterGoAround = false;
  @XmlIgnore
  private PilotRecorder recorder;

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

  public Pilot(Airplane.Airplane4Pilot parent, Navaid entryExitPoint, @Nullable ETime divertTime) {

    this.parent = parent;
    this.entryExitPoint = entryExitPoint;
    this.secondsWithoutRadarContact = 0;

    if (parent.isArrival()) {
      this.atc = Acc.atcCtr();
      this.behavior = new ArrivalBehavior();
      this.divertInfo = new DivertInfo(divertTime.clone());
    } else {
      this.atc = Acc.atcTwr();
      this.behavior = new HoldingPointBehavior();
      this.divertInfo = null;
    }

    this.openRecorder();
  }

  private Pilot() {

  }

  public Navaid getEntryExitPoint() {
    return this.entryExitPoint;
  }

  public Route getAssignedRoute() {
    return this.assignedRoute;
  }

  public void updateAssignedRouting(Route newRoute, RunwayThreshold expectedRunwayThreshold) {
    this.expectedRunwayThreshold = expectedRunwayThreshold;
    this.assignedRoute = newRoute;
    this.afterCommands.clearRoute();

    SpeechList<IFromAtc> cmds;
    cmds = new SpeechList<>();
    cmds.add(assignedRoute.getCommands());
    expandThenCommands(cmds);
    processSpeeches(cmds, CommandSource.procedure);
  }

  public Coordinate getTargetCoordinate() {
    return targetCoordinate;
  }

  public void addNewSpeeches(SpeechList<IFromAtc> speeches) {
    this.queue.newRandomDelay();
    expandThenCommands(speeches);
    for (ISpeech speech : speeches) {
      this.queue.add(speech);
    }
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

    recorder.logPostponedAfterSpeeches(this.afterCommands);
  }

  public String getStatusAsString() {
    if (behavior instanceof BasicBehavior)
      return behavior instanceof ArrivalBehavior ? "Arriving" : "Departing";
    else if (behavior instanceof HoldBehavior)
      return "Holding";
    else if (behavior instanceof ApproachBehavior)
      return "In approach " + this.tryGetAssignedApproach().getThreshold().getName();
    else if (behavior instanceof HoldingPointBehavior)
      return "Holding point";
    else if (behavior instanceof TakeOffBehavior)
      return "Take-off";
    else
      return "???";
  }

  public Atc getTunedAtc() {
    return this.atc;
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
    if (this.targetCoordinate != null && this.targetCoordinate.equals(navaid.getCoordinate()))
      ret = true;
    else if ((this.behavior instanceof HoldBehavior) && ((HoldBehavior) this.behavior).navaid.equals(navaid))
      ret = true;
    else
      ret = this.afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
    return ret;
  }

  public boolean isOnWayToPassPointInFuture(Navaid navaid) {
    boolean ret = this.afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
    return ret;
  }

  public CurrentApproachInfo tryGetAssignedApproach() {
    CurrentApproachInfo ret;
    ApproachBehavior ap = ConversionUtils.tryConvert(this.behavior, ApproachBehavior.class);
    if (ap != null)
      ret = ap.approach;
    else
      ret = null;
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

  public boolean hasRadarContact() {
    return secondsWithoutRadarContact == 0;
  }

  private void printAfterCommands() {
    System.out.println("## -- route ");
    for (Tuple<AfterCommand, IAtcCommand> afterCommandIAtcCommandTuple : afterCommands.getAsList(AfterCommandList.Type.route)) {
      System.out.println("  IF " + afterCommandIAtcCommandTuple.getA().toString());
      System.out.println("  THEN " + afterCommandIAtcCommandTuple.getB().toString());
    }
    System.out.println("## -- ex ");
    for (Tuple<AfterCommand, IAtcCommand> afterCommandIAtcCommandTuple : afterCommands.getAsList(AfterCommandList.Type.extensions)) {
      System.out.println("  IF " + afterCommandIAtcCommandTuple.getA().toString());
      System.out.println("  THEN " + afterCommandIAtcCommandTuple.getB().toString());
    }
  }

  private void setHoldBehavior(Navaid navaid, int inboundRadial, boolean leftTurn) {
    Pilot.HoldBehavior hold = new Pilot.HoldBehavior();
    hold.navaid = navaid;
    hold.inboundRadial = inboundRadial;
    hold.isLeftTurned = leftTurn;
    hold.phase = eHoldPhase.beginning;

    parent.setxState(Airplane.State.holding);
    Pilot.this.behavior = hold;
  }

  private void openRecorder() {
    this.recorder = new PilotRecorder(
        parent.getCallsign().toString() + " - pilot.log",
        new FileSaver(Recorder.getRecorderFileName(parent.getCallsign().toString() + "_pilot.log")),
        " \t ");
  }

  private void requestRadarContactIfRequired() {
    if (secondsWithoutRadarContact > 0) {
      secondsWithoutRadarContact++;
      if (secondsWithoutRadarContact % Global.REPEATED_RADAR_CONTACT_REQUEST_SECONDS == 0) {
        this.say(
            new GoodDayNotification(
                this.parent.getCallsign(), this.parent.getAltitude(), this.parent.getTargetAltitude(), this.parent.isEmergency(), true));
      }
    }
  }

  public RunwayThreshold getExpectedRunwayThreshold() {
    return expectedRunwayThreshold;
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
    this.assignedRoute = Route.createNewVectoringByFix(n, false);
    this.parent.setxState(Airplane.State.departingLow); // here must be departureLow, this us later used to evaluate delay

    this.say(new DivertingNotification(n));

  }

  private void abortHolding() {
    if (parent.isArrival()) {
      this.behavior = new ArrivalBehavior();
      parent.setxState(Airplane.State.arrivingHigh);
    } else {
      this.behavior = new DepartureBehavior();
      parent.setxState(Airplane.State.departingLow);
    }
    adjustTargetSpeed();
  }

  private void processNewSpeeches() {
    SpeechList current = new SpeechList(this.queue.getAndElapse());

    if (current.isEmpty()) return;

    recorder.logProcessedCurrentSpeeches(current);

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (secondsWithoutRadarContact > 0 && !(current.get(0) instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  private void processAfterSpeeches() {

    SpeechList<IAtcCommand> cmds;

    Coordinate targetCoordinate = this.targetCoordinate;
    if (targetCoordinate == null && this.behavior instanceof HoldBehavior)
      targetCoordinate = ((HoldBehavior) this.behavior).navaid.getCoordinate();

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), targetCoordinate, AfterCommandList.Type.extensions);
    recorder.logProcessedAfterSpeeches(cmds, "extensions");
    processSpeeches(cmds, CommandSource.extension);

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), targetCoordinate, AfterCommandList.Type.route);
    recorder.logProcessedAfterSpeeches(cmds, "route");
    processSpeeches(cmds, CommandSource.route);
  }

  private void processSpeeches(SpeechList<? extends IFromAtc> queue, CommandSource cs) {

    Airplane.Airplane4Command plane = this.parent.getPlane4Command();
    while (!queue.isEmpty()) {
      IFromAtc cmd = queue.get(0);
      if (cmd instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, cs);
      } else {
        processNormalSpeech(queue, cmd, cs, plane);
      }
    }
  }

  private void processNormalSpeech(
      SpeechList<? extends IFromAtc> queue, IFromAtc cmd,
      CommandSource cs, Airplane.Airplane4Command plane) {

    ConfirmationResult cres = ApplicationManager.confirm(plane, cmd, cs == CommandSource.atc, true);
    if (cres.rejection != null) {
      // command was rejected
      say(cres.rejection);
    } else {
      affectAfterCommands(cmd, cs);
      // new commands from atc when needs to be confirmed, are confirmed
      if (cs == CommandSource.atc && cres.confirmation != null)
        say(cres.confirmation);
      // command is applied
      ApplicationResult ares = ApplicationManager.apply(plane, cmd);
      assert ares.rejection == null : "This should not be rejected as was confirmed a few moments before.";
      ares.informations.forEach(q -> say(q));
    }

    queue.removeAt(0);
  }


  private void affectAfterCommands(IFromAtc cmd, CommandSource cs) {
    final Class[] lateralCommands = new Class[]{ProceedDirectCommand.class, ChangeHeadingCommand.class, HoldCommand.class};
    switch (cs) {
      case procedure:
        // nothing
        break;
      case route:
        // nothing
        break;
      case atc:

        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 2
          this.afterCommands.clearRoute();
          this.afterCommands.clearExtensionsByConsequent(lateralCommands);
        } else if (cmd instanceof ShortcutCommand) {
          // rule 3
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof ChangeAltitudeCommand) {
          // rule 4
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 5
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.route);
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 6
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 12
          this.afterCommands.clearAll();
        }
        break;
      case extension:
        if (ConversionUtils.isInstanceOf(cmd, lateralCommands)) {
          // rule 7
          this.afterCommands.clearRoute();
        } else if (cmd instanceof ShortcutCommand) {
          // rule 8
          // does nothing as everything is done in ShortcutCommandApplication
        } else if (cmd instanceof AfterAltitudeCommand) {
          // rule 9
          ChangeAltitudeCommand tmp = (ChangeAltitudeCommand) cmd;
          this.afterCommands.clearChangeAltitudeClass(tmp.getAltitudeInFt(), this.parent.isArrival());
        } else if (cmd instanceof ChangeSpeedCommand) {
          ChangeSpeedCommand tmp = (ChangeSpeedCommand) cmd;
          if (tmp.isResumeOwnSpeed() == false) {
            // rule 10
            this.afterCommands.clearChangeSpeedClass(tmp.getSpeedInKts(), this.parent.isArrival(), AfterCommandList.Type.extensions);
          } else {
            // rule 11
            this.afterCommands.clearChangeSpeedClassOfRouteWithTransferConsequent(
                null, this.parent.isArrival());
            this.afterCommands.clearExtensionsByConsequent(ChangeSpeedCommand.class);
          }
        } else if (cmd instanceof ClearedToApproachCommand) {
          // rule 13
          this.afterCommands.clearAll();
        }
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  private void processAfterSpeechWithConsequents(IList<? extends ISpeech> queue, CommandSource cs) {
    Airplane.Airplane4Command plane = this.parent.getPlane4Command();

    Airplane.State[] unableProcessAfterCommandsStates = {
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround
    };

    AfterCommand af = (AfterCommand) queue.get(0);
    queue.removeAt(0);

    if (cs == CommandSource.atc && plane.getState().is(unableProcessAfterCommandsStates)) {
      ISpeech rej = new Rejection("Unable to process after-command during approach/take-off.", af);
      say(rej);
      return;
    }

    ConfirmationResult cres;
    boolean sayConfirmations = cs == CommandSource.atc;

    cres = ApplicationManager.confirm(plane, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      ISpeech sp = queue.get(0);
      if (sp instanceof AfterCommand)
        break;
      else {
        assert sp instanceof IAtcCommand : "Instance of " + sp.getClass().getName() + " is not IAtcCommand";
        IAtcCommand cmd = (IAtcCommand) sp;
        if (cmd instanceof AfterCommand)
          break;

        queue.removeAt(0);
        cres = ApplicationManager.confirm(plane, cmd, true, false);
        if (sayConfirmations) say(cres.confirmation);

        if (cs == CommandSource.procedure) {
          afterCommands.addRoute(af, cmd);
        } else
          afterCommands.addExtension(af, cmd);
      }
    }
  }

  private void say(ISpeech speech) {
    // if no tuned atc, nothing is said
    if (atc == null) return;

    if (saidText.containsKey(atc) == false) {
      saidText.put(atc, new SpeechList());
    }

    saidText.get(atc).add(speech);
  }

  private void flushSaidTextToAtc() {
    for (Atc a : saidText.keySet()) {
      SpeechList saidTextToAtc = saidText.get(a);
      if (!saidTextToAtc.isEmpty()) {
        parent.passMessageToAtc(a, saidText.get(a));
        saidText.put(a, new SpeechList());
        // here new list must be created
        // the old one is send to messenger for further processing
      }
    }
  }

  private int getIndexOfNavaidInCommands(Navaid navaid) {
    for (int i = 0; i < this.queue.size(); i++) {
      if (this.queue.get(i) instanceof ProceedDirectCommand) {
        ProceedDirectCommand pdc = (ProceedDirectCommand) this.queue.get(i);
        if (pdc.getNavaid() == navaid) {
          return i;
        }
      }
    }
    return -1;

  }

  private void expandThenCommands(SpeechList<IFromAtc> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          parent.passMessageToAtc(
              atc,
              new IllegalThenCommandRejection("{Then} command cannot be first or last in queue. The whole command block is ignored.")
          );
          speeches.clear();
          return;
        }
        IAtcCommand prev = (IAtcCommand) speeches.get(i - 1);

        AfterCommand n; // new
        if (!(prev instanceof IAtcCommand)) {
          parent.passMessageToAtc(atc,
              new IllegalThenCommandRejection("{Then} command must be antecedent another command. The whole command block is ignored."));
        }
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          ChangeAltitudeCommand ca = (ChangeAltitudeCommand) prev;
          AfterAltitudeCommand.ERestriction restriction;
          switch (ca.getDirection()) {
            case any:
              restriction = AfterAltitudeCommand.ERestriction.exact;
              break;
            case climb:
              restriction = AfterAltitudeCommand.ERestriction.andAbove;
              break;
            case descend:
              restriction = AfterAltitudeCommand.ERestriction.andBelow;
              break;
            default:
              throw new UnsupportedOperationException();
          }
          n = new AfterAltitudeCommand(ca.getAltitudeInFt(), restriction);
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else if (prev instanceof ChangeHeadingCommand) {
          n = new AfterHeadingCommand(((ChangeHeadingCommand) prev).getHeading());
        } else {
          parent.passMessageToAtc(atc,
              new IllegalThenCommandRejection("{Then} command is antecedent a strange command, it does not make sense. The whole command block is ignored."));
          speeches.clear();
          return;
        }
        n.setDerivationSource(prev);
        speeches.set(i, n);
      }
    }
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
        if (parent.getTargetAltitude() > 10000)
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
    parent.setTargetSpeed(ts);
  }

  private int getBoundedValueIn(int min, int value, int max) {
    if (value < min)
      value = min;
    if (value > max)
      value = max;
    return value;
  }

  private void endrivePlane() {
    behavior.fly();
  }

}

class PilotRecorder extends Recorder {

  public PilotRecorder(String recorderName, AbstractSaver os, String fromTimeSeparator) {
    super(recorderName, os, fromTimeSeparator);
  }

  public void logPostponedAfterSpeeches(AfterCommandList afterCommands) {
    IReadOnlyList<Tuple<AfterCommand, IAtcCommand>> tmp;
    tmp = afterCommands.getAsList(AfterCommandList.Type.route);
    _logPosponed(tmp, "route");
    tmp = afterCommands.getAsList(AfterCommandList.Type.extensions);
    _logPosponed(tmp, "extensions");
  }

  public void logProcessedAfterSpeeches(SpeechList<IAtcCommand> cmds, String extensions) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Processed after speeches of " + extensions);
    for (int i = 0; i < cmds.size(); i++) {
      IAtcCommand cmd = cmds.get(i);
      sb.appendLine("\t").appendLine(cmd.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

  public void logProcessedCurrentSpeeches(SpeechList current) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Current processed speeches");
    for (int i = 0; i < current.size(); i++) {
      ISpeech sp = current.get(i);
      sb.append("\t").append(sp.toString()).appendLine();
    }
    super.writeLine(sb.toString());
  }

  private void _logPosponed(IReadOnlyList<Tuple<AfterCommand, IAtcCommand>> tmp, String type) {
    EStringBuilder sb = new EStringBuilder();
    sb.appendLine("Postponed " + type + " after commands");
    for (Tuple<AfterCommand, IAtcCommand> tuple : tmp) {
      sb.append("\t");
      sb.append(tuple.getA().toString());
      sb.append(" -> ");
      sb.append(tuple.getB().toString());
      sb.appendLine();
    }
    super.writeLine(sb.toString());
  }
}