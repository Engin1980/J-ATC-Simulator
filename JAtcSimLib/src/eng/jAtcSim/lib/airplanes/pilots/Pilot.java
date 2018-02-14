/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes.pilots;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.Message;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.SpeechDelayer;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.*;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.EStringBuilder;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.SpeedRestriction;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.EstablishedOnApproachNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.RequestRadarContactNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.CommandResponse;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Confirmation;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.IllegalThenCommandRejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.ShortCutToFixNotOnRoute;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
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

  // <editor-fold defaultstate="collapsed" desc=" Behavior class">
  abstract class Behavior {

    public final Pilot pilot;
    public final Airplane airplane;

    public Behavior() {
      this.pilot = Pilot.this;
      this.airplane = Pilot.this.parent;
    }

    public abstract void fly();

    public abstract String toLogString();
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc=" TakeOffBehavior class">
  class TakeOffBehavior extends Behavior {

    private final static int TAKEOFF_ACCELERATION_ALTITUDE_AGL = 1500;
    private boolean isFirstFly = true;

    @Override
    public void fly() {
      if (isFirstFly) {
        pilot.autoThrust.setMode(AutoThrust.Mode.takeOff, true);
        isFirstFly = false;
      } else {
        if (airplane.getAltitude() > Acc.airport().getAltitude() + TAKEOFF_ACCELERATION_ALTITUDE_AGL) {
          pilot.autoThrust.setMode(AutoThrust.Mode.normalLow, isFirstFly);
          pilot.behavior = null;
        }
      }
    }

    @Override
    public String toLogString() {
      return "TKO";
    }
  }

  // </editor-fold>
  //  // <editor-fold defaultstate="collapsed" desc=" HoldBehavior class">
  class HoldBehavior extends Behavior {

    private static final double NEAR_FIX_DISTANCE = 0.5;

    public Coordinate fix;
    public int inboundRadial;
    public eHoldPhase phase;
    public ETime secondTurnTime;
    public boolean isLeftTurned;

    public double getOutboundHeading() {
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

  //  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc=" ApproachBehavior class">
  class ApproachBehavior extends Behavior {

    private final static int LONG_FINAL_ALTITUDE_AGL = 1000;
    private final static int SHORT_FINAL_ALTITUDE_AGL = 200;
    public final int finalAltitude;
    public final int shortFinalAltitude;
    public Approach approach;
    public eApproachPhase phase;
    public boolean isAtFinalDescend = false;
    public Boolean isRunwayVisible = null;

    public ApproachBehavior(Approach approach) {
      this.approach = approach;
      this.finalAltitude = Acc.airport().getAltitude() + LONG_FINAL_ALTITUDE_AGL;
      this.shortFinalAltitude = Acc.airport().getAltitude() + SHORT_FINAL_ALTITUDE_AGL;
      this.isAtFinalDescend = false;
      this.phase = eApproachPhase.enteringFirst;
    }

    private double getAppHeadingDifference() {
      int heading = (int) Coordinates.getBearing(parent.getCoordinate(), this.approach.getParent().getCoordinate());
      double ret = Headings.subtract(heading, parent.getHeading());
      return ret;
    }

    @Override
    public void fly() {

      if (this.phase != eApproachPhase.touchdownAndLanded && this.phase != eApproachPhase.touchdownAndLandedFirst) {
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
      }

      switch (this.phase) {

        case enteringFirst:
          // this is when app is cleared for approach
          // this only updates speed and changes to "entering"
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);
          pilot.autoThrust.setMode(AutoThrust.Mode.approach, false);
          this.phase = eApproachPhase.entering;
          break;
        case entering:
          // this is all the time when airplane is looking for descend slope
          // when descend slope is achieved, descending is set
          updateHeadingOnApproach();
          boolean isDescending = updateAltitudeOnApproach(false);

          if (isDescending) {
            this.phase = eApproachPhase.descendingFirst;
          }
          break;
        case descendingFirst:
          // plane on descend slope
          // updates speed, then changes to "descending"
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);
          pilot.autoThrust.setMode(AutoThrust.Mode.atFinal, true);
          this.phase = eApproachPhase.descending;
          break;
        case descending:
          // plane on descend slope
          // changes on finalAltitude to "finalEnter"
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);
          if (parent.getAltitude() < this.finalAltitude) {
            this.phase = eApproachPhase.longFinalEnter;
          }
          break;
        case longFinalEnter:
          // plane under final altitude
          // yells if it have not own speed or if not switched to atc
          // TODO see above
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);
          this.phase = eApproachPhase.longFinal;

          // moc nizko, uz pod stabilized altitude
          int MAX_APP_HEADING_DIFF = 3;
          if (Math.abs(parent.getTargetHeading() - this.approach.getRadial()) > MAX_APP_HEADING_DIFF) {
            goAround("Unstabilized approach.");
            return;
          }

          // neni na twr, tak GA
          if (pilot.atc != Acc.atcTwr()) {
            Message m = new Message(parent, atc, new EstablishedOnApproachNotification());
            Acc.messenger().send(m);
          }

          break;
        case longFinal:
          // na final
          updateHeadingOnApproach();
          updateAltitudeOnApproach(false);

          if (parent.getAltitude() < this.shortFinalAltitude) {
            this.phase = eApproachPhase.shortFinalEnter;
          }
          break;
        case shortFinalEnter:
          updateAltitudeOnApproach(true);
          int newxHeading = (int) this.approach.getRadial();
          parent.setTargetHeading(newxHeading);

          // neni na twr, tak GA
          if (pilot.atc != Acc.atcTwr()) {
            goAround("Not cleared to land.");
            return;
          }

          this.phase = eApproachPhase.shortFinal;
          break;
        case shortFinal:
          updateAltitudeOnApproach(true);

          if (parent.getAltitude() == Acc.airport().getAltitude()) {
            this.phase = eApproachPhase.touchdownAndLandedFirst;
          }
          break;
        case touchdownAndLandedFirst:
          pilot.autoThrust.setMode(AutoThrust.Mode.idle, true);
          this.phase = eApproachPhase.touchdownAndLanded;
          break;
        case touchdownAndLanded:
          updateHeadingLanded();
          break;
        default:
          throw new ENotSupportedException();
      }
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
      Message m = new Message(parent, atc,
          new GoingAroundNotification(reason));
      Acc.messenger().send(m);

      parent.setTargetSpeed(parent.getType().vDep);

      SpeechList lst = new SpeechList(this.approach.getGaCommands());
      addNewSpeeches(lst);

      pilot.behavior = new TakeOffBehavior();
      pilot.autoThrust.setMode(AutoThrust.Mode.takeOff, true);
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
  // </editor-fold>
  private final Airplane parent;
  private final String routeName;
  private final SpeechDelayer queue = new SpeechDelayer(1, 7); //Min/max speech delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private final AutoThrust autoThrust;
  private final SpeechList saidText = new SpeechList();
  private Atc atc = null;
  private boolean hasRadarContact = true;
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private boolean isConfirmationsNowRequested = false;

  // </editor-fold>
  public Pilot(Airplane parent, String routeName, SpeechList<IAtcCommand> routeCommandQueue) {
    SpeechList<IAtcCommand> speeches = routeCommandQueue.clone(); // need clone to expand "thens"
    this.parent = parent;
    this.routeName = routeName;
    this.autoThrust = new AutoThrust(parent, AutoThrust.Mode.idle);
    expandThenCommands(speeches);
    this.queue.addNoDelay(speeches);
    if (parent.isArrival()) {
      autoThrust.setMode(AutoThrust.Mode.normalHigh, true);
    } else {
      autoThrust.setMode(AutoThrust.Mode.idle, true);
    }
  }

  // <editor-fold defaultstate="collapsed" desc=" getters/setters ">
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
        = afterCommands.getAndRemoveSatisfiedCommands(parent, this.targetCoordinate);

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
    if (behavior instanceof HoldBehavior || behavior instanceof ApproachBehavior) {
      behavior = null;
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

    if (behavior instanceof ApproachBehavior) {
      behavior = null;
    }

    parent.setTargetAltitude(c.getAltitudeInFt());
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed()) {
      this.autoThrust.cleanOrderedSpeed();
      confirmIfReq(c);
      return true;
    } else {
      // not resume speed

      SpeedRestriction sr = c.getSpeedRestriction();
      boolean isInApproach = behavior instanceof ApproachBehavior;

      int cMax = !isInApproach ? parent.getType().vMaxClean : parent.getType().vMaxApp;
      int cMin = !isInApproach ? parent.getType().vMinClean : parent.getType().vMinApp;
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

      this.autoThrust.setOrderedSpeed(sr);
      confirmIfReq(c);
      return true;
    }
  }

  private boolean processQueueSpeech(ChangeHeadingCommand c) {
    if (behavior instanceof HoldBehavior || behavior instanceof ApproachBehavior) {
      behavior = null;
    }

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
    int pointIndex = getIndexOfNavaidInCommands(c.getNavaid());
    if (pointIndex < 0) {
      say(new ShortCutToFixNotOnRoute(c));
    } else {
      for (int i = 0; i < pointIndex; i++) {
        this.queue.removeAt(i);
      }
    }
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ClearedToApproachCommand c) {

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
      if (behavior instanceof HoldBehavior) {
        behavior = null;
      }

      this.behavior = new ApproachBehavior(c.getApproach());
      confirmIfReq(c);
    }

    return true;
  }

  private boolean processQueueSpeech(HoldCommand c) {

    if (targetCoordinate != null) {
      targetCoordinate = null;
    }
    HoldBehavior hold = new HoldBehavior();
    hold.fix = c.getNavaid().getCoordinate();
    hold.inboundRadial = c.getInboundRadial();
    hold.isLeftTurned = c.isLeftTurn();
    hold.phase = eHoldPhase.beginning;

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
    if (behavior != null) {
      behavior.fly();
    } else if (targetCoordinate != null) {
      double heading = Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
      heading = Headings.to(heading);
      if (heading != parent.getTargetHeading()) {
        parent.setTargetHeading(heading);
      }
    }
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

  public String getAutoThrustLogString() {
    return autoThrust.toLogString();
  }

}
