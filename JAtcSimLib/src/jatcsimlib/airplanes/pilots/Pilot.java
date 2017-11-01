/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes.pilots;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Headings;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.messaging.GoingAroundStringMessageContent;
import jatcsimlib.newMessaging.Message;
import jatcsimlib.speaking.Speech;
import jatcsimlib.speaking.SpeechDelayer;
import jatcsimlib.speaking.SpeechList;
import jatcsimlib.speaking.commands.*;
import jatcsimlib.speaking.commands.afters.AfterAltitudeCommand;
import jatcsimlib.speaking.commands.afters.AfterNavaidCommand;
import jatcsimlib.speaking.commands.afters.AfterSpeedCommand;
import jatcsimlib.speaking.commands.specific.*;
import jatcsimlib.speaking.notifications.Confirmation;
import jatcsimlib.speaking.notifications.Rejection;
import jatcsimlib.speaking.notifications.specific.*;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Navaid;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
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

    private boolean isFirstFly = true;

    private final static int TAKEOFF_ACCELERATION_ALTITUDE_AGL = 1500;

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

    public int getOutboundHeading() {
      return Headings.add(inboundRadial, 180);
    }

    private void setHoldDataByEntry() {
      int y = (int) Coordinates.getBearing(parent.getCoordinate(), this.fix);
      int yy = Headings.add(y, 180);

      int h = this.inboundRadial;
      int a = Headings.add(h, -75);
      int b = Headings.add(h, 110);

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
            int newHeading = (int) Coordinates.getHeadingToRadial(
              parent.getCoordinate(), this.fix, this.inboundRadial, parent.getHeading());
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

            int newHeading;
            newHeading = this.isLeftTurned
              ? Headings.add(this.inboundRadial, -150)
              : Headings.add(this.inboundRadial, 150);
            parent.setTargetHeading(newHeading);
            this.secondTurnTime = Acc.now().addSeconds(120);

            this.phase = eHoldPhase.tearAgainst;
          } else {
            int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), this.fix);
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
            int newHeading = (this.isLeftTurned)
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

    public Approach approach;
    public eApproachPhase phase;
    public final int finalAltitude;
    public final int shortFinalAltitude;
    public boolean isAtFinalDescend = false;
    public Boolean isRunwayVisible = null;

    private final static int LONG_FINAL_ALTITUDE_AGL = 1000;
    private final static int SHORT_FINAL_ALTITUDE_AGL = 200;

    public ApproachBehavior(Approach approach) {
      this.approach = approach;
      this.finalAltitude = Acc.airport().getAltitude() + LONG_FINAL_ALTITUDE_AGL;
      this.shortFinalAltitude = Acc.airport().getAltitude() + SHORT_FINAL_ALTITUDE_AGL;
      this.isAtFinalDescend = false;
      this.phase = eApproachPhase.enteringFirst;
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
            Message m = new Message(parent, atc, new StringNotification("Established on final."));
            Acc.newMessenger().add(m);
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

    private int getAppHeadingDifference() {
      int heading = (int) Coordinates.getBearing(parent.getCoordinate(), this.approach.getParent().getCoordinate());
      int ret = Headings.diff(heading, parent.getHeading());
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
        int diff = getAppHeadingDifference();
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
      int newHeading
        = (int) Coordinates.getHeadingToRadial(
          parent.getCoordinate(), this.approach.getPoint(),
          this.approach.getRadial(), parent.getHeading());
      parent.setTargetHeading(newHeading);
    }

    private void updateHeadingLanded() {
      int newHeading
        = (int) Coordinates.getHeadingToRadial(
          parent.getCoordinate(), this.approach.getParent().getOtherThreshold().getCoordinate(),
          this.approach.getRadial(), parent.getHeading());
      parent.setTargetHeading(newHeading);
    }

    private boolean canSeeRunwayFromCurrentPosition() {
      Weather w = Acc.weather();

      if ((w.getCloudBaseInFt() + Acc.airport().getAltitude()) < parent.getAltitude()) {
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
      Acc.newMessenger().add(m);

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

  private Atc atc = null;
  private boolean hasRadarContact = true;
  private final Airplane parent;
  private final String routeName;
  //private final SpeechList queue = new SpeechList();
  private final SpeechDelayer queue = new SpeechDelayer(3, 10); //Min/max speech delay
  private final AfterCommandList afterCommands = new AfterCommandList();
  private Coordinate targetCoordinate;
  private Behavior behavior;
  private final AutoThrust autoThrust;
  private final SpeechList saidText = new SpeechList();

  private static final int SPEECH_DELAY = 7;

  // <editor-fold defaultstate="collapsed" desc=" getters/setters ">
  public String getRouteName() {
    return this.routeName;
  }

  public Coordinate getTargetCoordinate() {
    return targetCoordinate;
  }

// </editor-fold>
  public Pilot(Airplane parent, String routeName, CommandList routeCommandQueue) {
    SpeechList speeches = new SpeechList(routeCommandQueue); // incoming as command list, transfered to speech list
    this.parent = parent;
    this.routeName = routeName;
    this.autoThrust = new AutoThrust(parent, AutoThrust.Mode.idle);
    expandThenCommands(speeches);
    this.queue.add(speeches, 0);
    if (parent.isArrival()) {
      autoThrust.setMode(AutoThrust.Mode.normalHigh, true);
    } else {
      autoThrust.setMode(AutoThrust.Mode.idle, true);
    }
  }

  public void addNewSpeeches(SpeechList speeches) {
    int index = 0;
    expandThenCommands(speeches);
    while (index < speeches.size()) {
      index = addNewSpeeches(speeches, index);
    }
  }

  private int addNewSpeeches(SpeechList cmds, int index) {
    int ret = index + 1;
    Speech c = cmds.get(index);
    if ((c instanceof ToNavaidCommand)) {
      Navaid n = ((ToNavaidCommand) c).getNavaid();
      // tady musí ještě být sekvenční promazání v afterCommands zpětně celé routy
      this.afterCommands.removeByNavaidConsequentRecursively(n);
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class);
      this.queue.add(c);
    } else if (c instanceof ChangeHeadingCommand) {
      this.queue.clear();
      this.afterCommands.removeByConsequent(ChangeHeadingCommand.class);
      this.afterCommands.removeByConsequent(ProceedDirectCommand.class);
      this.queue.add(c);
    } else if (c instanceof AfterCommand) {
      if (c instanceof AfterNavaidCommand) {
        // zkontrolovat, jestli se letí přes tenhle navaid
        // jinak to nemá smysl
      }
      this.afterCommands.add((AfterCommand) c, cmds.getCommand(index + 1));
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
    sayToAtc();
  }

  private void processStandardQueueCommands() {
    SpeechList current = this.queue.get();
    if (current.isEmpty()) return;

    // if has not confirmed radar contact and the first command in the queue is not radar contact confirmation
    if (hasRadarContact == false && !(current.get(0) instanceof RadarContactConfirmationNotification )){
      say(new RequestRadarContactNotification());
      this.queue.clear();
    } else {
      this.isConfirmationsNowRequested = true;
      processQueueSpeeches(current);
      this.isConfirmationsNowRequested = false;
    }
  }

  private void processAfterCommands() {
    List<Command> cmdsToProcess
      = afterCommands.getAndRemoveSatisfiedCommands(parent, this.targetCoordinate);

    processQueueSpeeches(cmdsToProcess);
  }

  private void processQueueSpeeches(List<? extends Speech> queue) {
    while (!queue.isEmpty()) {
      Speech s = queue.get(0);
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

  private void processAfterCommandFromQueue(List<? extends Speech> queue) {
    AfterCommand af = (AfterCommand) queue.get(0);
    queue.remove(0);
    while (!queue.isEmpty() && !(queue.get(0) instanceof AfterCommand)) {
      afterCommands.add(af, (Command) queue.get(0));
      queue.remove(0);
    }
  }

  private boolean tryProcessQueueSpeech(Speech s) {
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

  private Method tryGetProcessQueueSpeechMethodToInvoke(Class<? extends Speech> commandType) {
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

  private boolean processQueueSpeech(RadarContactConfirmationNotification c){
    this.hasRadarContact = true;
    return true;
  }

  private boolean processQueueSpeech(ChangeAltitudeCommand c) {
    switch (c.getDirection()) {
      case climb:
        if (parent.getAltitude() > c.getAltitudeInFt()) {
          sayOrError(c, "we are higher.");
          return true;
        }
        break;
      case descend:
        if (parent.getAltitude() < c.getAltitudeInFt()) {
          sayOrError(c, "we are lower.");
          return true;
        }
        break;
    } // switch
    if (c.getAltitudeInFt() > parent.getType().maxAltitude) {
      sayOrError(c, "too high.");
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
        sayOrError(c,
          "Unable to reach speed " + c.getSpeedInKts() + " kts, maximum is " + cMax + ".");
        return true;
      } else if (sr.direction != SpeedRestriction.eDirection.atLeast && sr.speedInKts < cMin) {
        sayOrError(c,
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
    int targetHeading;
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

    parent.setTargetHeading(targetHeading, leftTurn);

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
    sayToAtc();

    // change of atc
    this.atc = a;
    this.hasRadarContact = false;
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    Speech s = new GoodDayNotification(parent.getCallsign(), Acc.toAltS(parent.getAltitude(), true));
    say(s);
    return true;
  }

  private boolean processQueueSpeech(ShortcutCommand c) {
    int pointIndex = getIndexOfNavaidInCommands(c.getNavaid());
    if (pointIndex < 0) {
      Message m = new Message(
          parent, this.atc,
          new StringNotification(" Unable to shortcut to " + c.getNavaid().getName() + ", fix not on our route!")      );
      Acc.newMessenger().add(m);
    } else {
      for (int i = 0; i < pointIndex; i++) {
        this.queue.removeAt(i);
      }
    }
    confirmIfReq(c);
    return true;
  }

  private boolean processQueueSpeech(ClearedToApproachCommand c) {
    if (behavior instanceof HoldBehavior) {
      behavior = null;
    }

    // zatim resim jen pozici letadla
    int radFromFix
      = (int) Coordinates.getBearing(parent.getCoordinate(), c.getApproach().getPoint());
    int dist
      = (int) Coordinates.getDistanceInNM(c.getApproach().getPoint(), parent.getCoordinate());
    if (dist > 17 || !Headings.isBetween(
      Headings.add(c.getApproach().getRadial(), -30),
      radFromFix,
      Headings.add(c.getApproach().getRadial(), 30))) {
      Message m = new Message(parent, atc,
          new StringNotification("Cannot enter approach now. Difficult position."));
      Acc.newMessenger().add(m);
    } else {
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

  private void sayOrError(Command c, String rejectionReason) {
    Rejection r = new Rejection(rejectionReason, c);
    say(r);
  }

  private boolean isConfirmationsNowRequested = false;

  private void confirmIfReq(Command originalCommandToBeConfirmed) {
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

  private void say(Speech speech) {
    saidText.add(speech);
  }

  private void sayToAtc() {

    if (atc == null) {
      // nobody to speak to
      return;
    }

    if (saidText.isEmpty()) {
      return;
    }

    Message m = new Message(parent, atc, saidText.clone());
    Acc.newMessenger().add(m);

    System.out.println("Saying to " + this.getTunedAtc().getName() + ": " + m.toString());

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

  private void expandThenCommands(List<Speech> speeches) {
    if (speeches.isEmpty()) {
      return;
    }

    for (int i = 0; i < speeches.size(); i++) {
      if (speeches.get(i) instanceof ThenCommand) {
        if (i == 0 || i == speeches.size() - 1) {
          Message message = new Message(
              parent, atc,
              new StringNotification("{Then} command cannot be first or last in queue. The whole command block is ignored."));
          Acc.newMessenger().add(message);
          speeches.clear();
          return;
        }
        Speech prev = speeches.get(i - 1);

        Command n; // new
        if (!(prev instanceof Command)){
          Message message = new Message(
              parent, atc,
              new StringNotification("{Then} command must be after another command. The whole command block is ignored."));
          Acc.newMessenger().add(message);

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
              new StringNotification("{Then} command is after a strange command, it does not make sense. The whole command block is ignored."));
          Acc.newMessenger().add(message);
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
      int heading = (int) Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
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
