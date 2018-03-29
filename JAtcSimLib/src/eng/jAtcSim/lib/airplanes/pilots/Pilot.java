/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import com.sun.istack.internal.Nullable;
import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.utilites.CollectionUtils;
import eng.eSystem.utilites.ConversionUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationManager;
import eng.jAtcSim.lib.airplanes.commandApplications.ApplicationResult;
import eng.jAtcSim.lib.airplanes.commandApplications.ConfirmationResult;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechDelayer;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.*;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.IllegalThenCommandRejection;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Routes;
import eng.jAtcSim.lib.world.RunwayThreshold;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.CurrentApproachInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marek
 */
@SuppressWarnings("unused")
public class Pilot {

  static class DivertInfo {
    public ETime divertTime;
    public int lastAnnouncedMinute = Integer.MAX_VALUE;

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
      Pilot.this.hasRadarContact = false;
    }

    public void say(ISpeech s) {
      Pilot.this.say(s);
    }

    public int getIndexOfNavaidInCommands(Navaid navaid) {
      for (int i = 0; i < Pilot.this.queue.size(); i++) {
        if (Pilot.this.queue.get(i) instanceof ProceedDirectCommand) {
          ProceedDirectCommand pdc = (ProceedDirectCommand) Pilot.this.queue.get(i);
          if (pdc.getNavaid() == navaid) {
            return i;
          }
        }
      }
      return -1;
    }

    public void removeAllItemsInQueueUntilIndex(int pointIndex) {
      for (int i = 0; i < pointIndex; i++) {
        Pilot.this.queue.removeAt(i);
      }
    }

    public void setHoldBehavior(Coordinate coordinate, int inboundRadial, boolean leftTurn) {
      Pilot.HoldBehavior hold = new Pilot.HoldBehavior();
      hold.fix = coordinate;
      hold.inboundRadial = inboundRadial;
      hold.isLeftTurned = leftTurn;
      hold.phase = eHoldPhase.beginning;

      parent.setxState(Airplane.State.holding);
      Pilot.this.behavior = hold;
    }

    public void setTakeOffBehavior(RunwayThreshold thrs) {
      Pilot.this.behavior = new Pilot.TakeOffBehavior(thrs);
      Pilot.this.parent.setTargetSpeed(Pilot.this.parent.getType().vR + 15);
      this.setTargetAltitude(thrs.getInitialDepartureAltitude());
      Pilot.this.parent.setTargetHeading(thrs.getCourse());
      Pilot.this.parent.setxState(Airplane.State.takeOffRoll);
    }

    public void setHasRadarContact() {
      Pilot.this.hasRadarContact = true;
    }

    public void adviceGoAroundReasonToAtcIfAny() {
      if (gaReason != null) {
        GoingAroundNotification gan = new GoingAroundNotification(gaReason);
        say(gan);
      }
    }

    public void processOrderedGoAround() {
      ApproachBehavior app = (ApproachBehavior) Pilot.this.behavior;
      app.goAround(null);
    }

    public int getDivertMinutesLeft() {
      return Pilot.this.divertInfo.getMinutesLeft();
    }

    public void processOrderedDivert() {
      Pilot.this.processDivert();
    }

    public void setTargetAltitude(int targetAltitude) {
      Pilot.this.altitudeOrderedByAtc = targetAltitude;
      adjustTargetAltitude();
    }

    public void setAltitudeRestriction(Restriction altitudeRestriction) {
      Pilot.this.altitudeRestriction = altitudeRestriction;
      throw new UnsupportedOperationException("AF");
//      if (altitudeRestriction == null)
//        Pilot.this.afterCommands.removeByConsequent(
//            SetAltitudeRestriction.class, false);
//      adjustTargetAltitude();
    }

    public void setTargetHeading(double value, boolean useLeftTurn) {
      parent.setTargetHeading(value, useLeftTurn);
    }

    protected void setState(Airplane.State state) {
      parent.setxState(state);
      Pilot.this.adjustTargetSpeed();
    }
  }

  abstract class Behavior {

    public final Pilot pilot;
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

    private final static int TAKEOFF_ACCELERATION_ALTITUDE_AGL = 1500;
    //TODO add to confing the acceleration altitude and use it here
    private final int accelerationAltitude =
        Acc.airport().getAltitude() + TAKEOFF_ACCELERATION_ALTITUDE_AGL;
    private RunwayThreshold toThreshold;

    public TakeOffBehavior(RunwayThreshold toThreshold) {
      this.toThreshold = toThreshold;
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

  abstract class BasicBehavior extends Behavior {
    private boolean clearanceLimitWarningSent = false;
    abstract void _fly();

    @Override
    public void fly() {
      if (targetCoordinate != null) {

        double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), targetCoordinate);
        if (!clearanceLimitWarningSent && dist < 5 && !pilot.afterCommands.hasLateralDirectionAfterCoordinate(targetCoordinate)) {
          say(new PassingClearanceLimitNotification());
          clearanceLimitWarningSent = true;
        } else if (dist < 1){
          targetCoordinate = null;
          clearanceLimitWarningSent = false;
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

    private final double LOW_SPEED_DOWN_ALTITUDE = 11000;
    private final double FAF_SPEED_DOWN_DISTANCE_IN_NM = 15;
    private int[] divertAnnounceTimes = new int[]{30, 15, 10, 5};

    @Override
    void _fly() {
      switch (parent.getState()) {
        case arrivingHigh:
          if (parent.getAltitude() < LOW_SPEED_DOWN_ALTITUDE)
            super.setState(Airplane.State.arrivingLow);
          else {
            double distToFaf;
            RunwayThreshold anyActiveThreshold = Acc.thresholds().get(0);
            distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), anyActiveThreshold.getEstimatedFafPoint());
            if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
              super.setState(Airplane.State.arrivingCloseFaf);
            }
          }
          break;
        case arrivingLow:
          // TODO this will not work for runways with FAF above FL100
          double distToFaf;
          RunwayThreshold anyActiveThreshold = Acc.thresholds().get(0);
          distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), anyActiveThreshold.getEstimatedFafPoint());
          if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
            super.setState(Airplane.State.arrivingCloseFaf);
          }
          break;
        case arrivingCloseFaf:
          break;
        default:
          super.throwIllegalStateException();
      }

      if (this.divertIfRequested() == false)
        this.adviceDivertTimeIfRequested();
    }

    private void adviceDivertTimeIfRequested() {
      DivertInfo di = Pilot.this.divertInfo;

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

    private boolean divertIfRequested() {
      if (divertInfo.getMinutesLeft() <= 0) {
        Pilot.this.processDivert();
        return true;
      } else {
        return false;
      }
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

  class HoldBehavior extends Behavior {

    private static final double NEAR_FIX_DISTANCE = 0.5;

    public Coordinate fix;
    public int inboundRadial;
    public eHoldPhase phase;
    public ETime secondTurnTime;
    public boolean isLeftTurned;

    private double getOutboundHeading() {
      return Headings.add(inboundRadial, 180);
    }

    private void setHoldDataByEntry() {
      double y = Coordinates.getBearing(parent.getCoordinate(), this.fix);
      double yy = Headings.add(y, 180);

      int h = this.inboundRadial;
      double a = Headings.add(h, -75);
      double b = Headings.add(h, 110);

      if (Headings.isBetween(b, yy, a)) {
        this.phase = eHoldPhase.directEntry;
      } else if (Headings.isBetween(h, yy, b)) {
        this.phase = eHoldPhase.parallelEntry;
      } else {
        this.phase = eHoldPhase.tearEntry;
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
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.fix) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
            this.phase = eHoldPhase.firstTurn;
          } else {
            int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), this.fix);
            parent.setTargetHeading(newHeading);
          }
          break;
        case inbound:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.fix) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), this.isLeftTurned);
            this.phase = eHoldPhase.firstTurn;
          } else {
            double newHeading = Coordinates.getHeadingToRadial(
                parent.getCoordinate(), this.fix, this.inboundRadial);
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
            parent.setTargetHeading(this.inboundRadial, this.isLeftTurned);
            this.phase = eHoldPhase.secondTurn;
          }
          break;
        case secondTurn:
          if (parent.getTargetHeading() == parent.getHeading()) {
            this.phase = eHoldPhase.inbound;
          }
          break;

        case tearEntry:
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.fix) < NEAR_FIX_DISTANCE) {

            double newHeading;
            newHeading = this.isLeftTurned
                ? Headings.add(this.inboundRadial, -150)
                : Headings.add(this.inboundRadial, 150);
            parent.setTargetHeading(newHeading);
            this.secondTurnTime = Acc.now().addSeconds(120);

            this.phase = eHoldPhase.tearAgainst;
          } else {
            double newHeading = Coordinates.getBearing(parent.getCoordinate(), this.fix);
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
          if (Coordinates.getDistanceInNM(parent.getCoordinate(), this.fix) < NEAR_FIX_DISTANCE) {
            parent.setTargetHeading(this.getOutboundHeading(), !this.isLeftTurned);
            this.secondTurnTime = Acc.now().addSeconds(60);
            this.phase = eHoldPhase.parallelAgainst;
          } else {
            int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), this.fix);
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
          throw new ENotSupportedException();
      }
    }

    @Override
    public String toLogString() {

      EStringBuilder sb = new EStringBuilder();

      sb.appendFormat("HLD %s incrs: %03d/%s in: %s",
          this.fix.toString(),
          this.inboundRadial,
          this.isLeftTurned ? "L" : "R",
          this.phase.toString());

      return sb.toString();
    }
  }

  class ApproachBehavior extends Behavior {

    private final static int LONG_FINAL_ALTITUDE_AGL = 1000;
    private final static int SHORT_FINAL_ALTITUDE_AGL = 200;
    private final int finalAltitude;
    private final int shortFinalAltitude;

    public CurrentApproachInfo approach;
    private boolean isAfterStateChange;
    private boolean isAtFinalDescend = false;
    private ApproachLocation location = ApproachLocation.unset;
    private double slope = -1;

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
        this.setState(Airplane.State.approachEnter);
      }
    }

    public void goAround(String reason) {
      Pilot.this.gaReason = reason;
      parent.adviceGoAroundToAtc(atc, reason);

      super.setBehaviorAndState(
          new TakeOffBehavior(null), Airplane.State.takeOffGoAround);

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude(0);
      parent.setTargetHeading(approach.getCourse());

      Pilot.this.afterCommands.clearAll();
      SpeechList<IFromAtc> gas = new SpeechList<>(this.approach.getGaRoute());
      expandThenCommands(gas);
      processSpeeches(gas, CommandSource.procedure);


    }

    private boolean updateAltitudeOnApproach(boolean checkIfIsAfterThreshold) {
      int currentTargetAlttiude = parent.getTargetAltitude();
      double distToLand;
      int newAltitude = -1;
      if (location == ApproachLocation.afterThreshold) {
        newAltitude = Acc.airport().getAltitude() - 100; // I need to lock the airplane on runway
      }

      if (newAltitude == -1) {
        switch (approach.getType()) {
          case visual:
            if (location == ApproachLocation.beforeFaf)
              newAltitude = parent.getTargetAltitude();
            else{
              double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
              double delta = dist * this.approach.getSlope();
              newAltitude = (int) delta + Acc.airport().getAltitude();
            }
            break;
          default:
            if (location == ApproachLocation.beforeFaf)
              newAltitude = parent.getTargetAltitude();
            else{
              double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getMapt() );
              double delta = dist * this.approach.getSlope();
              newAltitude = (int) delta + Acc.airport().getAltitude();
            }
        }
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

    private void updateHeadingOnApproach() {
      double newHeading;
      Coordinate planePos = parent.getCoordinate();
      if (location == ApproachLocation.beforeFaf) {
        newHeading = Coordinates.getBearing(planePos, approach.getFaf());
      } else if (location == ApproachLocation.beforeMapt) {
        Coordinate point = approach.getMapt();
        double course = approach.getFaf2MaptCourse();
        newHeading = Coordinates.getHeadingToRadial(
            planePos, point, course);
      } else if (location == ApproachLocation.beforeThreshold) {
        double dist = Coordinates.getDistanceInNM(planePos, this.approach.getThreshold().getCoordinate());
        if (dist < 2)
          newHeading = Coordinates.getBearing(planePos, this.approach.getThreshold().getCoordinate());
        else
          newHeading = Coordinates.getHeadingToRadial(planePos, this.approach.getThreshold().getCoordinate(), this.approach.getThreshold().getCourse() );
      } else {
        // afther threshold
        newHeading = (int) Coordinates.getBearing(planePos, this.approach.getThreshold().getOtherThreshold().getCoordinate());
      }
      parent.setTargetHeading(newHeading);
    }

    private boolean canSeeRunwayFromCurrentPosition() {
      Weather w = Acc.weather();
      if (w.getCloudBaseInFt() < parent.getAltitude()) {
        return false;
      }
      double d = Coordinates.getDistanceInNM(parent.getCoordinate(), approach.getThreshold().getCoordinate());
      if (w.getVisibilityInMilesReal() < d) {
        return false;
      }
      return true;
    }

    private void updateApproachLocation() {
      if (location == ApproachLocation.unset) {
        if (isPassingFaf())
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

      ApproachLocation last = this.location;
      updateApproachLocation();

      if (last == ApproachLocation.beforeMapt && this.location == ApproachLocation.beforeThreshold) {
        if (canSeeRunwayFromCurrentPosition() == false) {
          goAround("Not runway in sight.");
          return;
        }
      }

      switch (parent.getState()) {

        case flyingIaf2Faf:
          throw new UnsupportedOperationException("Not supposed to be here. See flyIAFtoFAFPhase()");

        case approachEnter:
          if (isAfterStateChange && this.approach.getType() == Approach.ApproachType.visual ){
            if (canSeeRunwayFromCurrentPosition() == false) {
              goAround("Not airport in sight.");
              return;
            }
          }
          isAfterStateChange = false;
          // this is when app is cleared for approach
          // this only updates speed and changes to "entering"
          updateHeadingOnApproach();
          boolean isDescending = updateAltitudeOnApproach(false);

          if (isDescending) {
            isAfterStateChange = true;
            super.setState(Airplane.State.approachDescend);
          }
          break;
        case approachDescend:
          // plane on descend slope
          // updates speed, then changes to "descending"
          isAfterStateChange = false;
          updateHeadingOnApproach();
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
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);

          if (isAfterStateChange) {
            // moc nizko, uz pod stabilized altitude
            int MAX_LONG_FINAL_HEADING_DIFF = 30;
            if (Math.abs(parent.getTargetHeading() - this.approach.getCourse()) > MAX_LONG_FINAL_HEADING_DIFF) {
              System.out.println("stab-fail " + (Math.abs(parent.getTargetHeading() - this.approach.getCourse()) > MAX_LONG_FINAL_HEADING_DIFF));
              goAround("Not stabilized in approach.");
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
          updateHeadingOnApproach();
          if (isAfterStateChange) {
            int MAX_SHORT_FINAL_HEADING_DIFF = 10;
            double diff = Math.abs(parent.getTargetHeading() - this.approach.getCourse());
            if (diff > MAX_SHORT_FINAL_HEADING_DIFF) {
              System.out.println("stab-fail " + diff);
              goAround("Not stabilized in approach.");
              return;
            }

            // neni na twr, tak GA
            if (pilot.atc != Acc.atcTwr()) {
              goAround("Not cleared to land. We expected to be switched to tower controller here.");
              return;
            }
            isAfterStateChange = false;
          }

          if (parent.getAltitude() == Acc.airport().getAltitude()) {
            isAfterStateChange = true;
            super.setState(Airplane.State.landed);
          }
          break;
        case landed:
          isAfterStateChange = false;
          updateHeadingOnApproach();
          break;
        default:
          super.throwIllegalStateException();
      }
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

  private enum SpeechProcessingType {
    normal,
    afterCommands,
    goAround,
    routeInsert
  }

  private enum CommandSource {
    procedure,
    atc,
    route,
    extension
  }

  private final Airplane.Airplane4Pilot parent;
  private final SpeechDelayer queue = new SpeechDelayer(2, 7); //Min/max speech delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final Map<Atc, SpeechList> saidText = new HashMap<>();
  private String gaReason = null;
  private DivertInfo divertInfo;
  private int altitudeOrderedByAtc;
  private Atc atc;
  private boolean hasRadarContact;
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private Route assignedRoute;
  private Restriction speedRestriction = null;
  private Restriction altitudeRestriction = null;

  public Pilot(Airplane.Airplane4Pilot parent, Route assignedRoute, @Nullable ETime divertTime) {

    this.parent = parent;
    this.assignedRoute = assignedRoute;

    if (parent.isArrival()) {
      this.atc = Acc.atcCtr();
      this.behavior = new ArrivalBehavior();
      this.divertInfo = new DivertInfo(divertTime.clone());
    } else {
      this.atc = Acc.atcTwr();
      this.behavior = new HoldingPointBehavior();
      this.divertInfo = null;
    }

    this.hasRadarContact = true;
  }

  public void initSpeeches(SpeechList<IAtcCommand> initialCommands) {
    SpeechList<IFromAtc> cmds;

    // route
    cmds = new SpeechList<>();
    cmds.add(assignedRoute.getCommands());
    expandThenCommands(cmds);
    processSpeeches(cmds, CommandSource.procedure);

    // initial ATC commands
    cmds = new SpeechList<>(initialCommands);
    expandThenCommands(cmds);
    processSpeeches(cmds, CommandSource.atc);
  }

  public Route getAssignedRoute() {
    return this.assignedRoute;
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
    flushSaidTextToAtc();

    this.afterCommands.consolePrint();
    System.out.println(" / / / / / ");
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
    Iterable<Route> rts = Acc.thresholds().get(0).getRoutes(); // get random active threshold
    List<Route> avails = Routes.getByFilter(rts, false, this.parent.getType().category);
    Route r = CollectionUtils.getRandom(avails, Acc.rnd());
    Navaid ret = r.getMainFix();
    return ret;
  }

  public boolean isOnWayToPassPoint(Navaid navaid) {
    boolean ret;
    if (this.targetCoordinate != null && this.targetCoordinate.equals(navaid.getCoordinate())) {
      ret = true;
    } else {
      ret = this.afterCommands.hasProceedDirectToNavaidAsConseqent(navaid);
    }
    return ret;
  }

  public CurrentApproachInfo tryGetAssignedApproach() {
    CurrentApproachInfo ret;
    ApproachBehavior ap = ConversionUtils.tryConvert(this.behavior);
    if (ap != null)
      ret = ap.approach;
    else
      ret = null;
    return ret;
  }

  private void processDivert() {

    Navaid n = getDivertNavaid();

    this.parent.divert();
    this.afterCommands.clearAll();
    this.behavior = new DepartureBehavior();
    this.divertInfo = null;
    this.assignedRoute = Route.createNewByFix(n, false);
    this.parent.setxState(Airplane.State.departingLow);

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
    SpeechList current = this.queue.get();
    if (current.isEmpty()) return;

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (hasRadarContact == false && !(current.get(0) instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      processSpeeches(current, CommandSource.atc);
    }
  }

  private void processAfterSpeeches() {

    SpeechList<IAtcCommand> cmds;

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), this.targetCoordinate, AfterCommandList.Type.extensions);
    processSpeeches(cmds, CommandSource.extension);

    cmds = afterCommands.getAndRemoveSatisfiedCommands(
        parent.getMe(), this.targetCoordinate, AfterCommandList.Type.route);
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
          throw new UnsupportedOperationException();
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
                tmp.getSpeedInKts(), this.parent.isArrival());
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
          throw new UnsupportedOperationException();
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
                tmp.getSpeedInKts(), this.parent.isArrival());
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

  private void removeUnecessaryAfterCommands(IAtcCommand c) {
    throw new UnsupportedOperationException("AF");
//    if (c instanceof ProceedDirectCommand ||
//        c instanceof ChangeHeadingCommand) {
//      this.afterCommands.removeByConsequent(ProceedDirectCommand.class, true);
//      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class, true);
//      this.afterCommands.removeByAntecedent(AfterNavaidCommand.class, true);
//    } else if (c instanceof ShortcutCommand) {
//      this.afterCommands.removeByConsequent(ProceedDirectCommand.class, true);
//      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class, true);
//    } else if (c instanceof ChangeAltitudeCommand) {
//      this.afterCommands.removeByConsequent(ChangeAltitudeCommand.class, true);
//    } else if ((c instanceof ContactCommand)
//        || (c instanceof ChangeSpeedCommand)
//        || (c instanceof ChangeAltitudeCommand)
//        || (c instanceof ClearedToApproachCommand)
//        || (c instanceof HoldCommand)) {
//      //this.afterCommands.removeByConsequent(c.getClass());
//    }
  }

  private void processAfterSpeechWithConsequents(IList<? extends ISpeech> queue, CommandSource cs) {
    Airplane.Airplane4Command plane = this.parent.getPlane4Command();
    AfterCommand af = (AfterCommand) queue.get(0);
    queue.removeAt(0);

    ConfirmationResult cres;
    boolean sayConfirmations = cs == CommandSource.atc;

    cres = ApplicationManager.confirm(plane, af, true, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      IAtcCommand cmd = (IAtcCommand) queue.get(0);
      if (cmd instanceof AfterCommand)
        break;
      queue.removeAt(0);

      cres = ApplicationManager.confirm(plane, cmd, true, false);
      if (sayConfirmations) say(cres.confirmation);

      if (cs == CommandSource.procedure){
        afterCommands.addRoute(af, cmd);
      } else
        afterCommands.addExtension(af, cmd);

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
        } else if (prev instanceof ChangeHeadingCommand){
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
          throw new ERuntimeException("Not supported.");
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
          throw new ERuntimeException("Not supported.");
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
        ts = parent.getTargetSpeed();
        break;
      case landed:
        ts = 0;
        break;
      default:
        throw new UnsupportedOperationException();
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
