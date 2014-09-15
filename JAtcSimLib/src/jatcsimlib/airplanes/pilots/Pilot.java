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
import jatcsimlib.commands.ClearedToApproachCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.commands.HoldCommand;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.commands.ShortcutCommand;
import jatcsimlib.commands.ThenCommand;
import jatcsimlib.commands.ToNavaidCommand;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.Headings;
import jatcsimlib.world.Approach;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.RunwayThreshold;
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

  public String getRouteName() {
    return this.routeName;
  }

  public Pilot(Airplane parent, String routeName, List<Command> routeCommandQueue) {
    this.parent = parent;
    this.routeName = routeName;
    expandThenCommands(routeCommandQueue);
    this.queue.addAll(routeCommandQueue);
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
        || (c instanceof ClearedToApproachCommand)) {
      this.afterCommands.removeByConsequent(c.getClass());
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
    
     */
    processStandardQueueCommands();
    processAfterCommands(); // udelat vlastni queue toho co se ma udelat a pak to provest pres processQueueCommands
    endrivePlane();
  }

  private void processStandardQueueCommands() {
    processQueueCommands(this.queue);
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
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException("Format-command failed for " + c.getClass() + ". Reason: " + ex.getMessage());
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
    sayIfReq(c, "Proceed direct " + c.getNavaid().getName());
    return true;
  }

  private boolean processQueueCommand(ChangeAltitudeCommand c) {
    switch (c.getDirection()) {
      case climb:
        if (parent.getAltitude() > c.getAltitudeInFt()) {
          sayOrError(c,
              "Cannot perform command to climb to " + Acc.toAltS(c.getAltitudeInFt(), true) + " cos we are higher.");
          return true;
        }
        break;
      case descend:
        if (parent.getAltitude() < c.getAltitudeInFt()) {
          sayOrError(c,
              "Cannot perform command to descend to " + Acc.toAltS(c.getAltitudeInFt(), true) + " cos we are lower.");
          return true;
        }
        break;
    } // switch
    if (c.getAltitudeInFt() > parent.getAirplaneSpecification().maxAltitude) {
      sayOrError(c, "Unable to climb to " + Acc.toAltS(parent.getAirplaneSpecification().maxAltitude, true) + ". Too high.");
      return true;
    }
    parent.setTargetAltitude(c.getAltitudeInFt());
    sayIfReq(c, "Altitude " + Acc.toAltS(c.getAltitudeInFt(), true));
    return true;
  }

  private boolean processQueueCommand(ChangeSpeedCommand c) {
    switch (c.getDirection()) {
      case increase:
        if (parent.getSpeed() > c.getSpeedInKts()) {
          sayOrError(c,
              "Cannot perform command to speed up to " + c.getSpeedInKts() + " cos we are faster.");
          return true;
        }
        break;
      case decrease:
        if (parent.getAltitude() < c.getSpeedInKts()) {
          sayOrError(c,
              "Cannot perform command to descend to " + c.getSpeedInKts() + " cos we are slower.");
          return true;
        }
        break;
    } // switch
    if (app != null) {
      if (c.getSpeedInKts() < parent.getAirplaneSpecification().vMinApp
          || c.getSpeedInKts() > parent.getAirplaneSpecification().vMaxApp) {
        sayOrError(c,
            "At approach, we accept only speed between "
            + parent.getAirplaneSpecification().vMinApp + " kts and "
            + parent.getAirplaneSpecification().vMaxApp + " kts.");
        return true;
      }
    } else {
      if (c.getSpeedInKts() < parent.getAirplaneSpecification().vMinClean
          || c.getSpeedInKts() > parent.getAirplaneSpecification().vMaxClean) {
        sayOrError(c,
            "While cruise, we accept only speed between "
            + parent.getAirplaneSpecification().vMinClean + " kts and "
            + parent.getAirplaneSpecification().vMaxClean + " kts.");
        return true;
      }
    } // if (app != null)
    parent.setTargetSpeed(c.getSpeedInKts());
    sayIfReq(c, "Speed " + c.getSpeedInKts() + " kts.");
    return true;
  }

  private boolean processQueueCommand(ChangeHeadingCommand c) {
    targetCoordinate = null;
    int targetHeading = c.getHeading();
    boolean leftTurn;

    if (c.getDirection() == ChangeHeadingCommand.eDirection.any) {
      leftTurn
          = (Headings.getBetterDirectionToTurn(parent.getHeading(), c.getHeading()) == ChangeHeadingCommand.eDirection.left);
    } else {
      leftTurn
          = c.getDirection() == ChangeHeadingCommand.eDirection.left;
    }

    parent.setTargetHeading(targetHeading, leftTurn);

    sayIfReq(c, "Heading " + c.getHeading());
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
    sayIfReq(c, "Contact " + a.getName() + "."); // at " + a.getFrequency() + ".");

    // change of atc
    atc = a;
    Acc.messenger().addMessage(5, parent, a,
        parent.getCallsign().toString() + " with you at " + Acc.toAltS(parent.getAltitude(), true));

    return true;
  }

  private boolean processQueueCommand(ShortcutCommand c) {
    ShortcutCommand t = c;
    int pointIndex = getIndexOfNavaidInCommands(t.getNavaid());
    if (pointIndex < 0) {
      Acc.messenger().addMessage(
          parent, this.atc, " Unable to shortcut to " + t.getNavaid().getName() + ", fix not on route!");
    } else {
      for (int i = 0; i < pointIndex; i++) {
        this.queue.remove(i);
      }
    }
    return true;
  }

  private boolean processQueueCommand(ClearedToApproachCommand c) {
    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    if (rt == null) {
      sayOrError(c, "There is no runway " + c.getRunwayThresholdName() + " we can approach to.");
      return true;
    }
    Approach a = rt.getApproaches().tryGet(c.getType());
    if (a == null) {
      sayOrError(c, "There is no approach of type " + c.getType() + " for runway " + rt.getName() + ".");
      return true;
    }

    if (hold != null) {
      hold = null;
    }
    this.app = new ApproachInfo(a);
    sayIfReq(c, "Cleared to " + a.getType() + " approach runway " + rt.getName() + ".");
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

    sayIfReq(c, "Proceed direct and hold over " + c.getNavaid().getName());
    return true;
  }

  private void sayOrError(Command c, String text) {
    if (c.isConfirmNeeded()) {
      say(text);
    } else {
      Acc.messenger().addMessage(null, Acc.atcApp(),
          String.format("Plane %0#s refused command. Reason: %1#s.",
              parent.getCallsign(), text));
    }
  }

  private void sayIfReq(Command c, String text) {
    if (c.isConfirmNeeded()) {
      say(text);
    }
  }

  private void say(String text) {
    if (atc != null) {
      Acc.messenger().addMessage(parent, atc, text);
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

  private void removeQueueCommands(Class clazz) {
    boolean isAfterCommand = AfterCommand.class
        .isAssignableFrom(clazz);
    boolean removeNext = false;
    List<Command> tmp = new LinkedList<>();
    for (Command c : this.queue) {
      if (removeNext) {
        tmp.add(c);
        removeNext = false;
      } else if (c.getClass().equals(clazz)) {
        tmp.add(c);
        if (isAfterCommand) {
          removeNext = true;
        }
      }
    }

    queue.removeAll(tmp);
  }

  private void expandThenCommands(List<Command> cmds) {
    for (int i = 0; i < cmds.size(); i++) {
      if (cmds.get(i) instanceof ThenCommand) {
        if (i == 0 || i == cmds.size() - 1) {
          Acc.messenger().addMessage(
              parent, atc, "\"THEN\" command cannot be first or last in queue. Whole command block is ignored.");
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
          Acc.messenger().addMessage(
              parent, atc, "\"THEN\" command is after strange command, it does not make sense. Whole command block is ignored.");
          cmds.clear();
          return;
        }
        cmds.remove(i);
        cmds.add(i, n);
      }
    }
  }

  private void endrivePlane() {
    if (hold != null) {
      // fly hold
      flyHold();
    } else if (app != null) {
      // fly app
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
              ? Headings.add(hold.inboundRadial, -160)
              : Headings.add(hold.inboundRadial, 160);
          parent.setTargetHeading(newHeading, hold.isLeftTurned);
          hold.secondTurnTime = Acc.now().addSeconds(60);

          hold.phase = HoldInfo.ePhase.tearAgainst;
        } else {
          int newHeading = (int) Coordinates.getBearing(parent.getCoordinate(), hold.fix);
          parent.setTargetHeading(newHeading);
        }

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
              ? Headings.add(hold.getOutboundHeading(), 210)
              : Headings.add(hold.getOutboundHeading(), -210);
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
    int y = parent.getHeading();
    int h = hold.inboundRadial;
    int a = Headings.add(h, -75);
    int b = Headings.add(h, 110);
    if (Headings.isBetween(a, y, h)) {
      hold.phase = HoldInfo.ePhase.tearEntry;
    } else if (Headings.isBetween(h, y, b)) {
      hold.phase = HoldInfo.ePhase.parallelEntry;
    } else {
      // direct
      hold.phase = HoldInfo.ePhase.directEntry;
    }
  }
}
