/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.EStringBuilder;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.SpeedRestriction;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechDelayer;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.EstablishedOnApproachNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.RequestRadarContactNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.CommandResponse;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.IllegalThenCommandRejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.ShortCutToFixNotOnRoute;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.world.Approach;
import eng.jAtcSim.lib.world.Navaid;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Marek
 */
@SuppressWarnings("unused")
public class Pilot {

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
      Pilot.this.parent.setState(state);
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
            parent.setState(Airplane.State.takeOffGoAround);
          }
          break;
        case takeOffGoAround:
          // keeps last heading
          // altitude already set
          // speed set
          if (parent.getAltitude() > this.accelerationAltitude)
            super.setBehaviorAndState(
                new DepartureBehavior(),
                Airplane.State.departingLow
            );
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
        // TODO if target is too close and there is no next target, alert ATC and continue current heading
        // TODO but watch out for behavior for departures passing last SID navaid
        double heading = Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
        heading = Headings.to(heading);
        if (heading != parent.getTargetHeading()) {
          parent.setTargetHeading(heading);
        }
      }
      _fly();
    }


  }

  class ArrivalBehavior extends BasicBehavior {

    @Override
    void _fly() {
      switch (parent.getState()) {
        case arrivingHigh:
          if (parent.getAltitude() < 11000)
            parent.setState(Airplane.State.arrivingLow);
          break;
        case arrivingLow:
          // TODO this will not work for runways with FAF above FL100
          double distToFaf =
              Coordinates.getDistanceInNM(parent.getCoordinate(), Acc.threshold().getFafCross());
          if (distToFaf < 15) {
            parent.setState(Airplane.State.arrivingCloseFaf);
          }
          break;
        case arrivingCloseFaf:
          break;
        default:
          super.throwIllegalStateException();
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
          if (parent.getAltitude() > 1000) parent.setState(Airplane.State.departingHigh);
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

    private void goAround(String reason) {
      parent.adviceGoAroundToAtc(atc, reason);

      parent.setTargetSpeed(parent.getType().vDep);
      parent.setTargetAltitude(Acc.threshold().getInitialDepartureAltitude());
      // altitude should be set by go-around route in next line
      SpeechList lst = new SpeechList(this.approach.getGaCommands());
      addNewSpeeches(lst);

      super.setBehaviorAndState(
          new TakeOffBehavior(), Airplane.State.takeOffGoAround);
    }

    @Override
    public void fly() {

      if (this.isRunwayVisible == null) {
        if (parent.getAltitude() < this.approach.getDecisionAltitude()) {
          if (canSeeRunwayFromCurrentPosition() == false) {
            this.isRunwayVisible = false;
            goAround("Not runway in sight.");
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
            parent.setState(Airplane.State.approachDescend);
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
            parent.setState(Airplane.State.longFinal);
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
            parent.setState(Airplane.State.shortFinal);
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
            parent.setState(Airplane.State.landed);
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

      sb.appendFormat("APP %s%s in %s",
          this.approach.getType().toString(),
          this.approach.getParent().getName(),
          this.phase.toString());

      return sb.toString();
    }
  }

  private final Airplane.Airplane4Pilot parent;
  private final String routeName;
  private final SpeechDelayer queue = new SpeechDelayer(1, 7); //Min/max speech delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final SpeechList saidText = new SpeechList();
  private SpeedRestriction speedRestriction = null;
  private Atc atc = null;
  private boolean hasRadarContact = true;
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private boolean isConfirmationsNowRequested = false;

  public Pilot(Airplane.Airplane4Pilot parent, String routeName, SpeechList<IAtcCommand> routeCommandQueue) {
    SpeechList<IAtcCommand> speeches = routeCommandQueue.clone(); // need clone to expand "thens"
    this.parent = parent;
    this.routeName = routeName;
    expandThenCommands(speeches);
    this.queue.addNoDelay(speeches);
    if (parent.isArrival()) {
      this.behavior = new ArrivalBehavior();
    } else {
      this.behavior = new TakeOffBehavior();
    }
  }

  public String getRouteName() {
    return this.routeName;
  }

  public Coordinate getTargetCoordinate() {
    return targetCoordinate;
  }

  public void addNewSpeeches(SpeechList speeches) {
    this.queue.newRandomDelay();
    int index = 0;
    expandThenCommands(speeches);
    while (index < speeches.size()) {
      index = addNewSpeeches(speeches, index);
    }
  }

  public void elapseSecond() {
    /*

     1. zpracuji se prikazy ve fronte
     2. zkontroluje se, jestli neni neco "after"
     3. ridi se letadlo
     4. ridicimu se reknou potvrzeni a zpravy

     */
    processStandardQueueCommands();
    processAfterCommands(); // udelat vlastni queue toho co se ma udelat a pak to provest pres processQueueCommands
    endrivePlane();
    flushSaidTextToAtc();
  }

  public Atc getTunedAtc() {
    return this.atc;
  }

  public String getBehaviorLogString() {
    if (behavior == null) {
      return "null";
    } else {
      return behavior.toLogString();
    }
  }

  private void abortHolding() {
    if (parent.isArrival()) {
      this.behavior = new ArrivalBehavior();
      parent.setState(Airplane.State.arrivingHigh);
    } else {
      this.behavior = new DepartureBehavior();
      parent.setState(Airplane.State.departingLow);
    }
  }

  private int addNewSpeeches(SpeechList cmds, int index) {
    int ret = index + 1;
    ISpeech c = cmds.get(index);
    if ((c instanceof ToNavaidCommand)) {
      Navaid n = ((ToNavaidCommand) c).getNavaid();
      // tady musí ještě být sekvenční promazání v afterCommands zpětně celé routy
      this.afterCommands.removeByNavaidConsequentRecursively(n);
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class);
      this.queue.add(c);
    } else if (c instanceof ChangeHeadingCommand) {
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class);
      this.afterCommands.removeByConsequent(ProceedDirectCommand.class);
      this.queue.add(c);
    } else if (c instanceof AfterCommand) {
      if (c instanceof AfterNavaidCommand) {
        // zkontrolovat, jestli se letí přes tenhle navaid
        // jinak to nemá smysl
      }
      this.afterCommands.add((AfterCommand) c, cmds.getAsCommand(index + 1));
      ret += 1;
    } else if ((c instanceof ContactCommand)
        || (c instanceof ChangeSpeedCommand)
        || (c instanceof ChangeAltitudeCommand)
        || (c instanceof ClearedToApproachCommand)
        || (c instanceof HoldCommand)) {
      this.afterCommands.removeByConsequent(c.getClass());
      this.queue.add(c);
    } else if ((c instanceof ClearedForTakeoffCommand) ||
        (c instanceof RadarContactConfirmationNotification)) {
      this.queue.add(c);
    } else {
      throw new ERuntimeException("Pilot cannot deal with speech " + c.getClass().getSimpleName() + " - probably not implemented.");
    }
    return ret;
  }

  private void processStandardQueueCommands() {
    SpeechList current = this.queue.get();
    if (current.isEmpty()) return;

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (hasRadarContact == false && !(current.get(0) instanceof RadarContactConfirmationNotification)) {
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      this.isConfirmationsNowRequested = true;
      processQueueSpeeches(current);
      this.isConfirmationsNowRequested = false;
    }
  }

  private void processAfterCommands() {
    List<ICommand> cmdsToProcess
        = afterCommands.getAndRemoveSatisfiedCommands(parent.getMe(), this.targetCoordinate);

    processQueueSpeeches(cmdsToProcess);
  }

  private void processQueueSpeeches(List<? extends ISpeech> queue) {
    while (!queue.isEmpty()) {
      ISpeech s = queue.get(0);
      if (s instanceof AfterCommand) {
        processAfterCommandFromQueue(queue);
      } else {
        boolean res = tryProcessQueueSpeech(s);
        if (res) {
          queue.remove(0);
        }
      }
    }
  }

  private void processAfterCommandFromQueue(List<? extends ISpeech> queue) {
    AfterCommand af = (AfterCommand) queue.get(0);
    queue.remove(0);
    while (!queue.isEmpty() && !(queue.get(0) instanceof AfterCommand)) {
      afterCommands.add(af, (IAtcCommand) queue.get(0));
      queue.remove(0);
    }
  }

  private boolean tryProcessQueueSpeech(ISpeech s) {
    Method m;
    m = tryGetProcessQueueSpeechMethodToInvoke(s.getClass());

    if (m == null) {
      throw new ERuntimeException("Method \"ProcessQueueSpeech\" for speech type \"" + s.getClass() + "\" not found.");
    }

    boolean ret;
    try {
      ret = (boolean) m.invoke(this, s);
    } catch (Throwable ex) {
      throw new ERuntimeException(
          String.format("processQueueSpeech() execution failed for %s. Reason: %s",
              s.getClass(),
              eng.eSystem.Exceptions.toString(ex), ex));
    }
    return ret;
  }

  private Method tryGetProcessQueueSpeechMethodToInvoke(Class<? extends ISpeech> commandType) {
    Method ret;
    try {
      ret = Pilot.class.getDeclaredMethod("processQueueSpeech", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  private boolean processQueueSpeech(ProceedDirectCommand c) {

    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    if (parent.getState() == Airplane.State.holding) {
      abortHolding();
    }

    targetCoordinate = c.getNavaid().getCoordinate();
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(RadarContactConfirmationNotification c) {
    this.hasRadarContact = true;
    return true;
  }

  private boolean processQueueSpeech(ChangeAltitudeCommand c) {
    //TODO now changing is not possible for approach
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    switch (c.getDirection()) {
      case climb:
        if (parent.getAltitude() > c.getAltitudeInFt()) {
          sayRejection(c, "we are higher.");
          return true;
        }
        break;
      case descend:
        if (parent.getAltitude() < c.getAltitudeInFt()) {
          sayRejection(c, "we are lower.");
          return true;
        }
        break;
    } // switch
    if (c.getAltitudeInFt() > parent.getType().maxAltitude) {
      sayRejection(c, "too high.");
      return true;
    }

    parent.setTargetAltitude(c.getAltitudeInFt());
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ChangeSpeedCommand c) {
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    if (c.isResumeOwnSpeed()) {
      this.speedRestriction = null;
      confirmIfReq(c);
      return true;
    } else {
      // not resume speed

      SpeedRestriction sr = c.getSpeedRestriction();
      boolean isInApproach = parent.getState().is(
          Airplane.State.approachEnter,
          Airplane.State.approachDescend
      );

      int cMax = !isInApproach ? parent.getType().vMaxClean : parent.getType().vMaxApp;
      int cMin = !isInApproach ? parent.getType().vMinClean : parent.getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(this.parent.getCoordinate(), Acc.threshold().getFafCross()) < 10) {
        cMin = (int) (cMin * 0.85);
      }

      if (sr.direction != SpeedRestriction.eDirection.atMost && sr.speedInKts > cMax) {
        sayRejection(c,
            "Unable to reach speed " + c.getSpeedInKts() + " kts, maximum is " + cMax + ".");
        return true;
      } else if (sr.direction != SpeedRestriction.eDirection.atLeast && sr.speedInKts < cMin) {
        sayRejection(c,
            "Unable to reach speed " + c.getSpeedInKts() + " kts, minimum is " + cMin + ".");
        return true;
      }

      this.speedRestriction = sr;
      confirmIfReq(c);
      return true;
    }
  }

  private boolean processQueueSpeech(ChangeHeadingCommand c) {
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    if (parent.getState() == Airplane.State.holding)
      abortHolding();

    targetCoordinate = null;
    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = parent.getHeading();
    } else {
      targetHeading = c.getHeading();
    }
    boolean leftTurn;

    if (c.getDirection() == ChangeHeadingCommand.eDirection.any) {
      leftTurn
          = (Headings.getBetterDirectionToTurn(parent.getHeading(), c.getHeading()) == ChangeHeadingCommand.eDirection.left);
    } else {
      leftTurn
          = c.getDirection() == ChangeHeadingCommand.eDirection.left;
    }

    parent.setTargetHeading((int) targetHeading, leftTurn);

    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ContactCommand c) {
    Atc a;
    switch (c.getAtcType()) {
      case app:
        a = Acc.atcApp();
        break;
      case ctr:
        a = Acc.atcCtr();
        break;
      case twr:
        a = Acc.atcTwr();
        break;
      default:
        throw new ENotSupportedException();
    }
    // confirmation to previous atc
    confirmIfReq(c);
    flushSaidTextToAtc();

    // change of atc
    this.atc = a;
    this.hasRadarContact = false;
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    ISpeech s = new GoodDayNotification(parent.getCallsign(), Acc.toAltS(parent.getAltitude(), true));
    say(s);
    return true;
  }

  private boolean processQueueSpeech(ShortcutCommand c) {
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    int pointIndex = getIndexOfNavaidInCommands(c.getNavaid());
    if (pointIndex < 0) {
      say(new ShortCutToFixNotOnRoute(c));
    } else {
      // hold abort only if fix was found
      if (parent.getState() == Airplane.State.holding) {
        abortHolding();
      }

      for (int i = 0; i < pointIndex; i++) {
        this.queue.removeAt(i);
      }
    }
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ClearedToApproachCommand c) {
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    final int MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM = 17;
    final int MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES = 30;

    // zatim resim jen pozici letadla
    int radFromFix
        = (int) Coordinates.getBearing(parent.getCoordinate(), c.getApproach().getPoint());
    int dist
        = (int) Coordinates.getDistanceInNM(c.getApproach().getPoint(), parent.getCoordinate());
    if (dist > MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM || !Headings.isBetween(
        Headings.add(
            c.getApproach().getRadial(),
            -MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES),
        radFromFix,
        Headings.add(
            c.getApproach().getRadial(),
            MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES))) {
      say(new UnableToEnterApproachFromDifficultPosition(c));
    } else {
      // hold abort only if fix was found
      if (parent.getState() == Airplane.State.holding) {
        abortHolding();
      }

      this.behavior = new ApproachBehavior(c.getApproach());
      confirmIfReq(c);
    }

    return true;
  }

  private boolean processQueueSpeech(HoldCommand c) {
    if (isUnableAndAdvice(c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed))
      return true;

    if (targetCoordinate != null) {
      targetCoordinate = null;
    }
    HoldBehavior hold = new HoldBehavior();
    hold.fix = c.getNavaid().getCoordinate();
    hold.inboundRadial = c.getInboundRadial();
    hold.isLeftTurned = c.isLeftTurn();
    hold.phase = eHoldPhase.beginning;

    parent.setState(Airplane.State.holding);
    this.behavior = hold;

    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ClearedForTakeoffCommand c) {
    this.behavior = new TakeOffBehavior();
    return true;
  }

  private void sayRejection(IAtcCommand c, String rejectionReason) {
    Rejection r = new Rejection(rejectionReason, c);
    say(r);
  }

  private boolean isUnableAndAdvice(IAtcCommand c, Airplane.State... states) {
    boolean ret = false;
    if (parent.getState().is(states)) {
      sayUnable(c);
      ret = true;
    }
    return ret;
  }

  private void sayUnable(IAtcCommand c) {
    sayRejection(c, "Unable to comply the command at current state.");
  }

  private void confirmIfReq(IAtcCommand originalCommandToBeConfirmed) {
    if (isConfirmationsNowRequested) {
      Confirmation cmd = new Confirmation(originalCommandToBeConfirmed);
      sayIfReq(cmd);
    }
  }

  private void sayIfReq(CommandResponse cmd) {
    if (isConfirmationsNowRequested) {
      say(cmd);
    }
  }

  private void say(ISpeech speech) {
    saidText.add(speech);
  }

  private void flushSaidTextToAtc() {

    if (atc == null) {
      // nobody to speak to
      return;
    }

    if (saidText.isEmpty()) {
      return;
    }

    Message m = new Message(parent, atc, saidText.clone());
    Acc.messenger().send(m);

    saidText.clear();
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

  private void expandThenCommands(SpeechList<IAtcCommand> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          Message message = new Message(
              parent, atc,
              new IllegalThenCommandRejection("{Then} command cannot be first or last in queue. The whole command block is ignored."));
          Acc.messenger().send(message);
          speeches.clear();
          return;
        }
        ISpeech prev = speeches.get(i - 1);

        IAtcCommand n; // new
        if (!(prev instanceof IAtcCommand)) {
          Message message = new Message(
              parent, atc,
              new IllegalThenCommandRejection("{Then} command must be after another command. The whole command block is ignored."));
          Acc.messenger().send(message);

        }
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          n = new AfterAltitudeCommand(((ChangeAltitudeCommand) prev).getAltitudeInFt());
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else {
          Message message = new Message(
              parent, atc,
              new IllegalThenCommandRejection("{Then} command is after a strange command, it does not make sense. The whole command block is ignored."));
          Acc.messenger().send(message);
          speeches.clear();
          return;
        }
        speeches.remove(i);
        speeches.add(i, n);
      }
    }
  }

  private void endrivePlane() {
    behavior.fly();
  }

}
