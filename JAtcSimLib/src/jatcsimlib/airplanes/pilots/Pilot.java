/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.airplanes.pilots;

import jatcsimlib.Acc;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.atcs.Atc;
import jatcsimlib.commands.AfterAltitudeCommand;
import jatcsimlib.commands.AfterCommand;
import jatcsimlib.commands.AfterCommandList;
import jatcsimlib.commands.AfterNavaidCommand;
import jatcsimlib.commands.AfterSpeedCommand;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.ChangeHeadingCommand;
import jatcsimlib.commands.ChangeSpeedCommand;
import jatcsimlib.commands.ClearedForTakeoffCommand;
import jatcsimlib.commands.ClearedToApproachCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.commands.HoldCommand;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.commands.ShortcutCommand;
import jatcsimlib.commands.ThenCommand;
import jatcsimlib.commands.ToNavaidCommand;
import jatcsimlib.commands.formatting.Formatter;
import jatcsimlib.commands.formatting.Formatters;
import jatcsimlib.commands.formatting.LongFormatter;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Headings;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.messaging.GoingAroundStringMessage;
import jatcsimlib.messaging.Message;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Navaid;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Pilot {

  private Atc atc = null;
  private final Airplane parent;
  private final String routeName;
  private final List<Command> queue = new LinkedList<>();  

  private final AfterCommandList afterCommands = new AfterCommandList();

  private Coordinate targetCoordinate;
  private HoldInfo hold;
  private ApproachInfo app;

  private boolean orderedSpeedChanged = false;
  private SpeedRestriction orderedSpeed;
  
  private static final Formatter cmdFmt = new LongFormatter();

  private enum eSpeedState {

    takeOff,
    approachAndFinal,
    departureLow,
    arrivingLow,
    high
  }
  private eSpeedState speedState;

  private final List<String> saidText = new LinkedList<>();
  
  // <editor-fold defaultstate="collapsed" desc=" getters/setters ">
  
    public String getRouteName() {
    return this.routeName;
  }

  public Coordinate getTargetCoordinate() {
    return targetCoordinate;
  }

  /**
   * Returns string for flight recorder.
   * @return 
   */
  public String getHoldLogString() {
    if (hold == null)
      return "no-hold";
    
    EStringBuilder sb = new EStringBuilder();
    
    sb.appendFormat("%s incrs: %03d/%s in: %s",
      hold.fix.toString(),
      hold.inboundRadial,
      hold.isLeftTurned ? "L" : "R",
      hold.phase.toString());
    
    return sb.toString();
  }

  /**
   * Returns string for flight recorder.
   * @return 
   */
  public String getApproachLogString() {
    if (app == null)
      return "no-app";
    
    EStringBuilder sb = new EStringBuilder();
    
    sb.appendFormat("%s%s in %s", 
      app.approach.getType().toString(),
      app.approach.getParent().getName(),
      app.phase.toString());
    
    return sb.toString();
  }

  public String getSpeedLogString() {
    EStringBuilder sb = new EStringBuilder();
    
    sb.appendFormat("%s", speedState.toString());
    if (orderedSpeed != null){
      sb.appendFormat(" (%s %4d)", 
        orderedSpeed.direction.toString(),
        orderedSpeed.speedInKts);
    }
    
    return sb.toString();
  }
    
    
  
// </editor-fold>
  


  public Pilot(Airplane parent, String routeName, List<Command> routeCommandQueue) {
    this.parent = parent;
    this.routeName = routeName;
    expandThenCommands(routeCommandQueue);
    this.queue.addAll(routeCommandQueue);
    speedState = parent.isArrival() ? eSpeedState.high : eSpeedState.takeOff;
  }

  public void addNewCommands(List<Command> cmds) {
    int index = 0;
    expandThenCommands(cmds);
    while (index < cmds.size()) {
      index = addNewCommand(cmds, index);
    }
  }

  private int addNewCommand(List<Command> cmds, int index) {
    int ret = index + 1;
    Command c = cmds.get(index);
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
      this.afterCommands.add((AfterCommand) c, cmds.get(index + 1));
      ret += 1;
    } else if ((c instanceof ContactCommand)
        || (c instanceof ChangeSpeedCommand)
        || (c instanceof ChangeAltitudeCommand)
        || (c instanceof ClearedToApproachCommand)
        || (c instanceof HoldCommand)) {
      this.afterCommands.removeByConsequent(c.getClass());
      this.queue.add(c);
    } else if ((c instanceof ClearedForTakeoffCommand)) {
      this.queue.add(c);
    } else {
      throw new ERuntimeException("Pilot cannot deal with command " + c.getClass().getSimpleName() + " - probably not implemented.");
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
    this.isConfirmationsNowRequested = true;
    processQueueCommands(this.queue);
    this.isConfirmationsNowRequested = false;
  }

  private void processAfterCommands() {
    List<Command> cmdsToProcess
        = afterCommands.getAndRemoveSatisfiedCommands(parent, this.targetCoordinate);

    processQueueCommands(cmdsToProcess);
  }

  private void processQueueCommands(List<Command> queue) {
    while (!queue.isEmpty()) {
      Command c = queue.get(0);
      if (c instanceof AfterCommand) {
        processAfterCommandFromQueue(queue);
      } else {
        boolean res = tryProcessQueueCommand(c);
        if (res) {
          queue.remove(0);
        }
      }
    }
  }

  private void processAfterCommandFromQueue(List<Command> queue) {
    AfterCommand af = (AfterCommand) queue.get(0);
    queue.remove(0);
    while (!queue.isEmpty() && !(queue.get(0) instanceof AfterCommand)) {
      afterCommands.add(af, queue.get(0));
      queue.remove(0);
    }
  }

  private boolean tryProcessQueueCommand(Command c) {
    Method m;
    m = tryGetProcessQueueCommandMethodToInvoke(c.getClass());

    if (m == null) {
      throw new ERuntimeException("Method \"ProcessQueueCommand\" for command type \"" + c.getClass() + "\" not found.");
    }

    boolean ret;
    try {
      ret = (boolean) m.invoke(this, c);
    } catch (Throwable ex) {
      throw new ERuntimeException(
        String.format("processQueueCommand failed for %s. Reason: %s",
          c.getClass(),
          eng.eSystem.Exceptions.toString(ex), ex));
    }
    return ret;
  }

  private Method tryGetProcessQueueCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = Pilot.class.getDeclaredMethod("processQueueCommand", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  private boolean processQueueCommand(ProceedDirectCommand c) {
    if (hold != null) {
      hold = null;
    }
    if (app != null) {
      app = null;
    }
    targetCoordinate = c.getNavaid().getCoordinate();
    sayIfReq(cmdFmt.format(c));
    return true;
  }

  private boolean processQueueCommand(ChangeAltitudeCommand c) {
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
    
    if (app != null)
      app = null;
    
    parent.setTargetAltitude(c.getAltitudeInFt());
    sayIfReq(cmdFmt.format(c));
    return true;
  }

  private boolean processQueueCommand(ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed()) {
      this.orderedSpeed = null;
      this.orderedSpeedChanged = true;
      sayIfReq(cmdFmt.format(c));
      return true;
    } else {
      // not resume speed

      SpeedRestriction sr = c.getSpeedRestriction();
      int cMax = app == null ? parent.getType().vMaxClean : parent.getType().vMaxApp;
      int cMin = app == null ? parent.getType().vMinClean : parent.getType().vMinApp;

      if (sr.direction != SpeedRestriction.eDirection.atMost && sr.speedInKts > cMax) {
        sayOrError(c,
            "Unable to reach speed " + c.getSpeedInKts() + " kts, maximum is " + cMax + ".");
        return true;
      } else if (sr.direction != SpeedRestriction.eDirection.atLeast && sr.speedInKts < cMin) {
        sayOrError(c,
            "Unable to reach speed " + c.getSpeedInKts() + " kts, minimum is " + cMin + ".");
        return true;
      }

      this.orderedSpeed = sr;
      this.orderedSpeedChanged = true;
      sayIfReq(cmdFmt.format(c));
      return true;
    }
  }

  private boolean processQueueCommand(ChangeHeadingCommand c) {
    if (hold != null) {
      hold = null;
    }
    if (app != null) {
      app = null;
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

    sayIfReq(cmdFmt.format(c));
    return true;
  }

  private boolean processQueueCommand(ContactCommand c) {
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
    sayIfReq(cmdFmt.format(c));
    sayToAtc();

    // change of atc
    this.atc = a;
    Message m = Message.create(
      parent, a, 
      parent.getCallsign().toString() + " with you at " + Acc.toAltS(parent.getAltitude(), true), 5);
    Acc.messenger().addMessage(m);

    return true;
  }

  private boolean processQueueCommand(ShortcutCommand c) {
    ShortcutCommand t = c;
    int pointIndex = getIndexOfNavaidInCommands(t.getNavaid());
    if (pointIndex < 0) {
      Message m = Message.create(
        parent, this.atc,
        " Unable to shortcut to " + t.getNavaid().getName() + ", fix not on route!");
      Acc.messenger().addMessage(m);
    } else {
      for (int i = 0; i < pointIndex; i++) {
        this.queue.remove(i);
      }
    }
    sayIfReq(cmdFmt.format(c));
    return true;
  }

  private boolean processQueueCommand(ClearedToApproachCommand c) {

    if (hold != null) {
      hold = null;
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
      Message m = Message.create(parent, atc, "Cannot enter approach now. Difficult position.");
      Acc.messenger().addMessage(m);
    } else {
      this.app = new ApproachInfo(c.getApproach());

      sayIfReq(cmdFmt.format(c));
    }

    return true;
  }

  private boolean processQueueCommand(HoldCommand c) {
    if (app != null) {
      app = null;
    }
    if (targetCoordinate != null) {
      targetCoordinate = null;
    }
    hold = new HoldInfo();
    hold.fix = c.getNavaid().getCoordinate();
    hold.inboundRadial = c.getInboundRadial();
    hold.isLeftTurned = c.isLeftTurn();
    hold.phase = HoldInfo.ePhase.beginning;

    sayIfReq(cmdFmt.format(c));
    return true;
  }

  private boolean processQueueCommand(ClearedForTakeoffCommand c) {
    int s = parent.getType().vDep;
    parent.setTargetSpeed(s);
    this.speedState = eSpeedState.takeOff;
    return true;
  }

  private void sayOrError(Command c, String text) {
    String s =String.format(
      "Not able to process command %s, because %s.",
      Formatters.format(c, cmdFmt),
      text);
    say(s);
  }

  private boolean isConfirmationsNowRequested = false;

  private void sayIfReq(String text) {
    if (isConfirmationsNowRequested) {
      say(text);
    }
  }

  private void say(String text) {
    saidText.add(text);
  }

  private void sayToAtc() {

    if (atc == null) {
      // nobody to speak to
      return;
    }

    if (saidText.isEmpty()) {
      return;
    }

    StringBuilder ret = new StringBuilder();

    for (String s : saidText) {
      ret.append(s);
      ret.append(", ");
    }

    ret.append(parent.getCallsign());
    char c = ret.charAt(0);
    c = Character.toUpperCase(c);
    ret.setCharAt(0, c);

    Message m = Message.create(parent, atc, ret.toString());
    Acc.messenger().addMessage(m);

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

  private void expandThenCommands(List<Command> cmds) {
    if (cmds.isEmpty()) {
      return;
    }

    for (int i = 0; i < cmds.size(); i++) {
      if (cmds.get(i) instanceof ThenCommand) {
        if (i == 0 || i == cmds.size() - 1) {
          Acc.messenger().addMessage(Message.create(
              parent, atc, "\"THEN\" command cannot be first or last in queue. Whole command block is ignored."));
          cmds.clear();
          return;
        }
        Command prev = cmds.get(i - 1);
        Command n; // new
        if (prev instanceof ProceedDirectCommand) {
          n = new AfterNavaidCommand(((ProceedDirectCommand) prev).getNavaid());
        } else if (prev instanceof ChangeAltitudeCommand) {
          n = new AfterAltitudeCommand(((ChangeAltitudeCommand) prev).getAltitudeInFt());
        } else if (prev instanceof ChangeSpeedCommand) {
          n = new AfterSpeedCommand(((ChangeSpeedCommand) prev).getSpeedInKts());
        } else {
          Acc.messenger().addMessage(Message.create(
              parent, atc, "\"THEN\" command is after strange command, it does not make sense. Whole command block is ignored."));
          cmds.clear();
          return;
        }
        cmds.remove(i);
        cmds.add(i, n);
      }
    }
  }

  private void endrivePlane() {
    adjustSpeed();
    if (hold != null) {
      // fly hold
      flyHold();
    } else if (app != null) {
      // fly app
      flyApproach();
    } else if (targetCoordinate != null) {
      int heading = (int) Coordinates.getBearing(parent.getCoordinate(), targetCoordinate);
      heading = Headings.to(heading);
      if (heading != parent.getTargetHeading()) {
        parent.setTargetHeading(heading);
      }
    }
  }

  private static final double NEAR_FIX_DISTANCE = 0.5;

  private void flyHold() {

    if (hold.phase == HoldInfo.ePhase.beginning) {
      setHoldDataByEntry();
    }

    switch (hold.phase) {
      case directEntry:
        if (Coordinates.getDistanceInNM(parent.getCoordinate(), hold.fix) < NEAR_FIX_DISTANCE) {
          parent.setTargetHeading(hold.getOutboundHeading(), hold.isLeftTurned);
          hold.phase = HoldInfo.ePhase.firstTurn;
        } else {
          int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), hold.fix);
          parent.setTargetHeading(newHeading);
        }
        break;
      case inbound:
        if (Coordinates.getDistanceInNM(parent.getCoordinate(), hold.fix) < NEAR_FIX_DISTANCE) {
          parent.setTargetHeading(hold.getOutboundHeading(), hold.isLeftTurned);
          hold.phase = HoldInfo.ePhase.firstTurn;
        } else {
          int newHeading = (int) Coordinates.getHeadingToRadial(
              parent.getCoordinate(), hold.fix, hold.inboundRadial, parent.getHeading());
          parent.setTargetHeading(newHeading);

        }
        break;
      case firstTurn:
        if (parent.getTargetHeading() == parent.getHeading()) {
          hold.secondTurnTime = Acc.now().addSeconds(60);
          hold.phase = HoldInfo.ePhase.outbound;
        }
        break;
      case outbound:
        if (Acc.now().isAfter(hold.secondTurnTime)) {
          parent.setTargetHeading(hold.inboundRadial, hold.isLeftTurned);
          hold.phase = HoldInfo.ePhase.secondTurn;
        }
        break;
      case secondTurn:
        if (parent.getTargetHeading() == parent.getHeading()) {
          hold.phase = HoldInfo.ePhase.inbound;
        }
        break;

      case tearEntry:
        if (Coordinates.getDistanceInNM(parent.getCoordinate(), hold.fix) < NEAR_FIX_DISTANCE) {

          int newHeading;
          newHeading = hold.isLeftTurned
              ? Headings.add(hold.inboundRadial, -150)
              : Headings.add(hold.inboundRadial, 150);
          parent.setTargetHeading(newHeading);
          hold.secondTurnTime = Acc.now().addSeconds(120);

          hold.phase = HoldInfo.ePhase.tearAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), hold.fix);
          parent.setTargetHeading(newHeading);
        }
        break;

      case tearAgainst:
        if (Acc.now().isAfter(hold.secondTurnTime)) {
          hold.secondTurnTime = null;
          parent.setTargetHeading(hold.inboundRadial, hold.isLeftTurned);
          hold.phase = HoldInfo.ePhase.secondTurn;
        }
        break;

      case parallelEntry:
        if (Coordinates.getDistanceInNM(parent.getCoordinate(), hold.fix) < NEAR_FIX_DISTANCE) {
          parent.setTargetHeading(hold.getOutboundHeading(), !hold.isLeftTurned);
          hold.secondTurnTime = Acc.now().addSeconds(60);
          hold.phase = HoldInfo.ePhase.parallelAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), hold.fix);
          parent.setTargetHeading(newHeading);
        }
        break;

      case parallelAgainst:
        if (Acc.now().isAfter(hold.secondTurnTime)) {
          int newHeading = (hold.isLeftTurned)
              ? Headings.add(hold.getOutboundHeading(), -210)
              : Headings.add(hold.getOutboundHeading(), +210);
          parent.setTargetHeading(newHeading, !hold.isLeftTurned);
          hold.phase = HoldInfo.ePhase.parallelTurn;
        }
        break;

      case parallelTurn:
        if (parent.getHeading() == parent.getTargetHeading()) {
          hold.phase = HoldInfo.ePhase.directEntry;
        }
        break;

      default:
        throw new ENotSupportedException();
    }
  }

  private void setHoldDataByEntry() {
    int y = (int) Coordinates.getBearing(parent.getCoordinate(), hold.fix);
    int yy = Headings.add(y, 180);

    int h = hold.inboundRadial;
    int a = Headings.add(h, -75);
    int b = Headings.add(h, 110);

    if (Headings.isBetween(b, yy, a)) {
      hold.phase = HoldInfo.ePhase.directEntry;
    } else if (Headings.isBetween(h, yy, b)) {
      hold.phase = HoldInfo.ePhase.parallelEntry;
    } else {
      hold.phase = HoldInfo.ePhase.tearEntry;
    }
  }

  private void flyApproach() {

    if (app.phase != ApproachInfo.ePhase.touchdownAndLanded) {
      if (app.isRunwayVisible == null) {
        if (parent.getAltitude() < app.approach.getDecisionAltitude()) {
          if (canSeeRunwayFromCurrentPosition() == false) {
            app.isRunwayVisible = false;
            goAround("Runway not visible at decision point.");
          } else {
            app.isRunwayVisible = true;
          }
        }
      }
    }

    switch (app.phase) {
      case approaching:
        updateHeadingOnApproach();
        updateAltitudeOnApproach(false);

        // zpomalim, jen pokud nemam prikazanou rychlost a
        // navic jsem blizko localizeru uz
        if (orderedSpeed == null && app.isAppSpeedSet == false) {
          int diff = getAppHeadingDifference();
          if (diff < 15) {
            parent.setTargetSpeed(parent.getType().vApp);
            app.isAppSpeedSet = true;
          }
        }

        if (parent.getAltitude() < app.finalAltitude) {
          app.phase = ApproachInfo.ePhase.finalEnter;
        }
        break;
      case finalEnter:
        // na final
        if (app.isAppSpeedSet == false) {
          parent.setTargetSpeed(parent.getType().vApp);
          app.isAppSpeedSet = true;
        }

        // neni na twr, tak GA
        if (this.atc != Acc.atcTwr()) {
          goAround("Not at TWR atc.");
          return;
        }

        // moc nizko, uz pod stabilized altitude
        int MAX_APP_HEADING_DIFF = 3;
        if (Math.abs(parent.getTargetHeading() - app.approach.getRadial()) > MAX_APP_HEADING_DIFF) {
          goAround("Unstabilized approach.");
          return;
        }

        updateHeadingOnApproach();
        updateAltitudeOnApproach(false);

        app.phase = ApproachInfo.ePhase.finalOther;
        break;
      case finalOther:

        updateHeadingOnApproach();
        updateAltitudeOnApproach(false);

        if (parent.getAltitude() < app.shortFinalAltitude) {
          app.phase = ApproachInfo.ePhase.shortFinal;
        }
        break;
      case shortFinal:
        updateAltitudeOnApproach(true);
        int newxHeading = (int) app.approach.getRadial();
        parent.setTargetHeading(newxHeading);

        if (parent.getAltitude() == Acc.airport().getAltitude()) {
          app.phase = ApproachInfo.ePhase.touchdownAndLanded;
        }
        break;
      case touchdownAndLanded:
        // is on the ground
//        int newxHeading = (int) app.approach.getRadial();
//        parent.setTargetHeading(newxHeading);

        parent.setTargetSpeed(0);
        break;
      default:
        throw new ENotSupportedException();
    }
  }

  private int getAppHeadingDifference() {
    int heading = (int) Coordinates.getBearing(parent.getCoordinate(), app.approach.getParent().getCoordinate());
    int ret = Headings.diff(heading, parent.getHeading());
    return ret;
  }

  private void updateAltitudeOnApproach(boolean checkIfIsAfterThreshold) {
    int newAltitude = -1;
    if (checkIfIsAfterThreshold) {
      int diff = getAppHeadingDifference();
      if (diff > 90) {
        newAltitude = Acc.airport().getAltitude();
      }
    }

    if (newAltitude == -1) {
      double distToLand
          = Coordinates.getDistanceInNM(parent.getCoordinate(), app.approach.getParent().getCoordinate());
      newAltitude
          = (int) (app.approach.getParent().getParent().getParent().getAltitude()
          + app.approach.getGlidePathPerNM() * distToLand);
    }

    newAltitude = (int) Math.min(newAltitude, parent.getTargetAltitude());
    newAltitude = (int) Math.max(newAltitude, Acc.airport().getAltitude());

    parent.setTargetAltitude(newAltitude);
  }

  private void updateHeadingOnApproach() {
    int newHeading
        = (int) Coordinates.getHeadingToRadial(
            parent.getCoordinate(), app.approach.getPoint(),
            app.approach.getRadial(), parent.getHeading());
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

  private void adjustSpeed() {
    switch (speedState) {
      case takeOff:
        if (parent.getAltitude() > Acc.airport().getAltitude() + 1500) {
          if (parent.isDeparture()) {
            speedState = eSpeedState.departureLow;
          } else {
            speedState = eSpeedState.arrivingLow;
          }
          setBestSpeed();
        }
        break;
      case departureLow:
        if (parent.getAltitude() > 10_000) {
          speedState = eSpeedState.high;
          setBestSpeed();
        }
        break;
      case high:
        if (parent.isArrival() && parent.getAltitude() < 10_000) {
          speedState = eSpeedState.arrivingLow;
          setBestSpeed();
        }
        break;
      case arrivingLow:
        if (app != null) {
          speedState = eSpeedState.approachAndFinal;
          setBestSpeed();
        }
        break;
    }

    if (this.orderedSpeedChanged) {
      setBestSpeed();
    }
  }

  private void setBestSpeed() {
    int s;
    switch (speedState) {
      case high:
        s = parent.getType().vCruise;
        break;
      case departureLow:
      case arrivingLow:
        s = Math.min(parent.getType().vCruise, 250);
        break;
      case approachAndFinal:
        s = parent.getType().vApp;
        break;
      case takeOff:
        s = parent.getType().vDep;
        break;
      default:
        throw new ENotSupportedException();
    }
    s = updateSpeedBySpeedRestriction(s);
    parent.setTargetSpeed(s);
  }

  private int updateSpeedBySpeedRestriction(int speedInKts) {
    if (this.orderedSpeed == null) {
      return speedInKts;
    } else {
      switch (this.speedState) {
        case approachAndFinal:
        case takeOff:
          return speedInKts;
        default:
          switch (this.orderedSpeed.direction) {
            case exactly:
              return this.orderedSpeed.speedInKts;
            case atMost:
              return Math.min(this.orderedSpeed.speedInKts, speedInKts);
            case atLeast:
              return Math.max(this.orderedSpeed.speedInKts, speedInKts);
            default:
              throw new ENotSupportedException();
          }
      }
    }
  }

  private void goAround(String reason) {
    Acc.messenger().addMessage(Message.create(
        parent,
        atc,
        new GoingAroundStringMessage(reason)));
    parent.setTargetSpeed(parent.getType().vDep);
    addNewCommands(app.approach.getGaCommands());
    app = null;

    int s = parent.getType().vDep;
    parent.setTargetSpeed(s); // ok
    this.speedState = eSpeedState.takeOff;
  }

  public Atc getTunedAtc() {
    return this.atc;
  }
}
