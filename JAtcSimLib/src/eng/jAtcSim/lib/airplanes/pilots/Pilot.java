/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import com.sun.istack.internal.Nullable;
import eng.eSystem.EStringBuilder;
import eng.eSystem.utilites.CollectionUtil;
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
import eng.jAtcSim.lib.global.SpeedRestriction;
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
import eng.jAtcSim.lib.world.Approach;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.Route;
import eng.jAtcSim.lib.world.Routes;

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

    public SpeedRestriction getSpeedRestriction() {
      return speedRestriction;
    }

    public void setSpeedRestriction(SpeedRestriction speedRestriction) {
      Pilot.this.speedRestriction = speedRestriction;
      Pilot.this.adjustTargetSpeed();
    }

    public void setApproachBehavior(Approach app) {
      Pilot.this.behavior = Pilot.this.new ApproachBehavior(app);
      parent.setxState(Airplane.State.approachEnter);
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

    public void setTakeOffBehavior() {
      Pilot.this.behavior = new Pilot.TakeOffBehavior();
      Pilot.this.parent.setTargetSpeed(Pilot.this.parent.getType().vR + 15);
      Pilot.this.parent.setTargetAltitude(Acc.threshold().getInitialDepartureAltitude());
      Pilot.this.parent.setTargetHeading(Acc.threshold().getCourse());
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

  class TakeOffBehavior extends Behavior {

    private final static int TAKEOFF_ACCELERATION_ALTITUDE_AGL = 1500;
    //TODO add to confing the acceleration altitude and use it here
    private final int accelerationAltitude =
        Acc.threshold().getParent().getParent().getAltitude() + TAKEOFF_ACCELERATION_ALTITUDE_AGL;

    @Override
    public void fly() {
      switch (parent.getState()) {
        case holdingPoint:
          break;
        case takeOffRoll:
          double targetHeading = Coordinates.getBearing(
              parent.getCoordinate(), Acc.threshold().getOtherThreshold().getCoordinate());
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
              // after G/A
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
    abstract void _fly();

    @Override
    public void fly() {
      if (targetCoordinate != null) {

        double dist = Coordinates.getDistanceInNM(parent.getCoordinate(), targetCoordinate);
        if (dist < 0.5) {
          say(new PassingClearanceLimitNotification());
          targetCoordinate = null;
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
            if (Acc.threshold().getFafCross() == null) {
              distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getCoordinate());
            } else {
              distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getFafCross());
            }
            if (distToFaf < FAF_SPEED_DOWN_DISTANCE_IN_NM) {
              super.setState(Airplane.State.arrivingCloseFaf);
            }
          }
          break;
        case arrivingLow:
          // TODO this will not work for runways with FAF above FL100
          double distToFaf;
          if (Acc.threshold().getFafCross() == null)
            distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getCoordinate());
          else
            distToFaf = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getFafCross());
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
    public Approach approach;
    private boolean isAfterStateChange;
    private boolean isAtFinalDescend = false;
    private Boolean isRunwayVisible = null;

    public ApproachBehavior(Approach approach) {
      this.approach = approach;
      this.finalAltitude = Acc.airport().getAltitude() + LONG_FINAL_ALTITUDE_AGL;
      this.shortFinalAltitude = Acc.airport().getAltitude() + SHORT_FINAL_ALTITUDE_AGL;
      this.isAfterStateChange = true;
      Pilot.this.gaReason = null;
    }

    public void goAround(String reason) {
      Pilot.this.gaReason = reason;
      parent.adviceGoAroundToAtc(atc, reason);

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude(Acc.threshold().getInitialDepartureAltitude());
      // altitude should be set by go-around route in next line
      SpeechList lst = new SpeechList(this.approach.getGaCommands());
      addNewSpeeches(lst);

      super.setBehaviorAndState(
          new TakeOffBehavior(), Airplane.State.takeOffGoAround);
    }

    private double getAppHeadingDifference() {
      int heading = (int) Coordinates.getBearing(parent.getCoordinate(), this.approach.getParent().getCoordinate());
      double ret = Headings.subtract(heading, parent.getHeading());
      return ret;
    }

    /**
     * Adjusts plane target altitude on approach.
     *
     * @param checkIfIsAfterThreshold
     * @return True if plane is descending to the runway, false otherwise.
     */
    private boolean updateAltitudeOnApproach(boolean checkIfIsAfterThreshold) {
      int currentTargetAlttiude = parent.getTargetAltitude();
      int newAltitude = -1;
      if (checkIfIsAfterThreshold) {
        double diff = getAppHeadingDifference();
        if (diff > 90) {
          newAltitude = Acc.airport().getAltitude();
        }
      }

      if (newAltitude == -1) {
        double distToLand = Coordinates.getDistanceInNM(
            parent.getCoordinate(), this.approach.getParent().getCoordinate());
        newAltitude = (int) (this.approach.getParent().getParent().getParent().getAltitude()
            + this.approach.getGlidePathPerNM() * distToLand);
      }

      newAltitude = (int) Math.min(newAltitude, parent.getTargetAltitude());
      newAltitude = (int) Math.max(newAltitude, Acc.airport().getAltitude());

      parent.setTargetAltitude(newAltitude);
      boolean ret = currentTargetAlttiude > parent.getTargetAltitude();
      return ret;
    }

    private void updateHeadingOnApproach() {
      double newHeading
          = Coordinates.getHeadingToRadial(
          parent.getCoordinate(), this.approach.getPoint(),
          this.approach.getRadial());
      parent.setTargetHeading(newHeading);
    }

    private void updateHeadingLanded() {
      // TODO this should be its own "LandedBehavior"
      int newHeading
          = (int) Coordinates.getHeadingToRadial(
          parent.getCoordinate(), this.approach.getParent().getOtherThreshold().getCoordinate(),
          this.approach.getRadial());
      parent.setTargetHeading(newHeading);
    }

    private boolean canSeeRunwayFromCurrentPosition() {
      Weather w = Acc.weather();
      if (w.getCloudBaseInFt() < parent.getAltitude()) {
        return false;
      }
      double d = Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getCoordinate());
      if (w.getVisibilityInMiles() < d) {
        return false;
      }
      return true;
    }

    @Override
    public void fly() {

      if (this.isRunwayVisible == null) {
        if (parent.getAltitude() < this.approach.getDecisionAltitude()) {
          if (canSeeRunwayFromCurrentPosition() == false) {
            this.isRunwayVisible = false;
            goAround("Not runway in sight.");
            return;
          } else {
            this.isRunwayVisible = true;
          }
        }
      }

      switch (parent.getState()) {

        case approachEnter:
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
            int MAX_APP_HEADING_DIFF = 3;
            if (Math.abs(parent.getTargetHeading() - this.approach.getRadial()) > MAX_APP_HEADING_DIFF) {
              goAround("Not stabilized in approach.");
              return;
            }

            // neni na twr, tak GA
            if (pilot.atc != Acc.atcTwr()) {
              parent.adviceToAtc(atc, new EstablishedOnApproachNotification());
            }
          }
          isAfterStateChange = false;

          if (parent.getAltitude() < this.shortFinalAltitude) {
            isAfterStateChange = true;
            super.setState(Airplane.State.shortFinal);
          }
          break;
        case shortFinal:
          updateAltitudeOnApproach(true);
          if (isAfterStateChange) {
            // here was fixed update in next two lines
//            int newxHeading = (int) this.approach.getRadial();
//            parent.setTargetHeading(newxHeading);
            // now replaced by dynamic looking for runway threshold
            double newHeading = Coordinates.getBearing(
                parent.getCoordinate(), approach.getParent().getCoordinate());
            parent.setTargetHeading(newHeading);

            // neni na twr, tak GA
            if (pilot.atc != Acc.atcTwr()) {
              goAround("Not cleared to land. We expected to be switched to tower controller here.");
              return;
            }
          }
          isAfterStateChange = false;

          if (parent.getAltitude() == Acc.airport().getAltitude()) {
            isAfterStateChange = true;
            super.setState(Airplane.State.landed);
          }
          break;
        case landed:
          isAfterStateChange = false;
          updateHeadingLanded();
          break;
        default:
          super.throwIllegalStateException();
      }
    }


    @Override
    public String toLogString() {

      EStringBuilder sb = new EStringBuilder();

      sb.appendFormat("APP %s%s",
          this.approach.getType().toString(),
          this.approach.getParent().getName());

      return sb.toString();
    }
  }

  private final Airplane.Airplane4Pilot parent;
  private String routeName;
  private final SpeechDelayer queue = new SpeechDelayer(1, 7); //Min/max speech delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final Map<Atc, SpeechList> saidText = new HashMap<>();
  private SpeedRestriction speedRestriction = null;
  private Atc atc;
  private boolean hasRadarContact;
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private String gaReason = null;
  private DivertInfo divertInfo;

  public Pilot(Airplane.Airplane4Pilot parent, String routeName, SpeechList<IAtcCommand> routeCommandQueue, @Nullable ETime divertTime) {

    this.parent = parent;
    this.routeName = routeName;
    {
      SpeechList<IFromAtc> speeches =
          new SpeechList<>(routeCommandQueue); // need clone to expand "thens"
      expandThenCommands(speeches);
      this.queue.addNoDelay(speeches);
    }
    if (parent.isArrival()) {
      this.atc = Acc.atcCtr();
      this.behavior = new ArrivalBehavior();
      this.divertInfo = new DivertInfo(divertTime.clone());
    } else {
      this.atc = Acc.atcTwr();
      this.behavior = new TakeOffBehavior();
      this.divertInfo = null;
    }

    this.hasRadarContact = true;
  }

  public String getRouteName() {
    return this.routeName;
  }

  public Coordinate getTargetCoordinate() {
    return targetCoordinate;
  }

  public void addNewSpeeches(SpeechList<IFromAtc> speeches) {
    this.queue.newRandomDelay();
    int index = 0;
    expandThenCommands(speeches);
    for (ISpeech speech : speeches) {
      this.queue.add(speech);
    }
//    while (index < speeches.size()) {
//      index = addNewSpeeches(speeches, index);
//    }
  }

  public void elapseSecond() {
    /*

     1. zpracuji se prikazy ve fronte
     2. zkontroluje se, jestli neni neco "after"
     3. ridi se letadlo
     4. ridicimu se reknou potvrzeni a zpravy

     */
    processNewSpeeches();
    processAfterSpeeches(); // udelat vlastni queue toho co se ma udelat a pak to provest pres processQueueCommands
    endrivePlane();
    flushSaidTextToAtc();
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
    Iterable<Route> rts = Acc.threshold().getRoutes();
    List<Route> avails = Routes.getByFilter(rts, false, this.parent.getType().category);
    Route r = CollectionUtil.getRandom(avails, Acc.rnd());
    Navaid ret = r.getMainFix();
    return ret;
  }

  private void processDivert() {

    Navaid n = getDivertNavaid();

    this.parent.divert();
    this.afterCommands.clear();
    this.behavior  = new DepartureBehavior();
    this.divertInfo = null;
    this.routeName = n.getName();
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
      processSpeeches(current, true, false);
    }
  }

  private void processAfterSpeeches() {
    List<IAtcCommand> cmdsToProcess
        = afterCommands.getAndRemoveSatisfiedCommands(parent.getMe(), this.targetCoordinate);

    processSpeeches(cmdsToProcess, false, true);
  }

  private void processSpeeches(List<? extends ISpeech> queue, boolean sayConfirmations, boolean isAfterCommandProcessing) {

    Airplane.Airplane4Command plane = this.parent.getPlane4Command();
    while (!queue.isEmpty()) {
      ISpeech s = queue.get(0);
      IFromAtc sa = (IFromAtc) s;
      if (s instanceof AfterCommand) {
        processAfterSpeechWithConsequents(queue, true);
      } else {
        ConfirmationResult cres = ApplicationManager.confirm(plane, sa, true);
        if (cres.rejection != null) {
          // command was rejected
          say(cres.rejection);
        } else {
          if (!isAfterCommandProcessing && (sa instanceof IAtcCommand)) removeUnecessaryAfterCommands((IAtcCommand) sa);
          // command was not confirmed
          // notifications do not have confirmation
          if (sayConfirmations && cres.confirmation != null)
            say(cres.confirmation);
          ApplicationResult ares = ApplicationManager.apply(plane, sa);
          if (ares.rejection != null) {
            throw new ERuntimeException("This should not be rejected as was confirmed a few moments before.");
          } else {
            ares.informations.forEach(q -> say(q));
          }
        }

        queue.remove(0);
      }
    }
  }

  private void removeUnecessaryAfterCommands(IAtcCommand c) {
    if (c instanceof ProceedDirectCommand ||
        c instanceof ChangeHeadingCommand) {
      this.afterCommands.removeByConsequent(ProceedDirectCommand.class, true);
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class, true);
      this.afterCommands.removeByAntecedent(AfterNavaidCommand.class, true);
    } else if (c instanceof ShortcutCommand) {
      this.afterCommands.removeByConsequent(ProceedDirectCommand.class, true);
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class, true);
    } else if (c instanceof ChangeAltitudeCommand) {
      this.afterCommands.removeByConsequent(ChangeAltitudeCommand.class, true);
    } else if ((c instanceof ContactCommand)
        || (c instanceof ChangeSpeedCommand)
        || (c instanceof ChangeAltitudeCommand)
        || (c instanceof ClearedToApproachCommand)
        || (c instanceof HoldCommand)) {
      //this.afterCommands.removeByConsequent(c.getClass());
    }
  }

  private void processAfterSpeechWithConsequents(List<? extends ISpeech> queue, boolean sayConfirmations) {
    Airplane.Airplane4Command plane = this.parent.getPlane4Command();
    AfterCommand af = (AfterCommand) queue.get(0);
    queue.remove(0);

    ConfirmationResult cres;

    cres = ApplicationManager.confirm(plane, af, false);
    if (sayConfirmations) say(cres.confirmation);

    while (queue.isEmpty() == false) {
      IAtcCommand cmd = (IAtcCommand) queue.get(0);
      if (cmd instanceof AfterCommand)
        break;
      queue.remove(0);

      cres = ApplicationManager.confirm(plane, cmd, false);
      if (sayConfirmations) say(cres.confirmation);

      afterCommands.add(af, cmd);

    }
  }

  private void sayAfterCommandConfirmations(List<? extends ISpeech> queue, Airplane.Airplane4Command plane) {
    for (ISpeech iSpeech : queue) {
      IFromAtc sa = (IFromAtc) iSpeech;
      ConfirmationResult cres = ApplicationManager.confirm(plane, sa, false);
      say(cres.confirmation);
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
              new IllegalThenCommandRejection("{Then} command must be after another command. The whole command block is ignored."));
        }
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          n = new AfterAltitudeCommand(((ChangeAltitudeCommand) prev).getAltitudeInFt());
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else {
          parent.passMessageToAtc(atc,
              new IllegalThenCommandRejection("{Then} command is after a strange command, it does not make sense. The whole command block is ignored."));
          speeches.clear();
          return;
        }
        n.setDerivationSource(prev);
        speeches.set(i, n);
      }
    }
  }

  private void adjustTargetSpeed() {
    int minOrdered;
    int maxOrdered;
    if (speedRestriction != null) {
      switch (speedRestriction.direction) {
        case exactly:
          minOrdered = speedRestriction.speedInKts;
          maxOrdered = speedRestriction.speedInKts;
          break;
        case atLeast:
          minOrdered = speedRestriction.speedInKts;
          maxOrdered = Integer.MAX_VALUE;
          break;
        case atMost:
          minOrdered = Integer.MIN_VALUE;
          maxOrdered = speedRestriction.speedInKts;
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
