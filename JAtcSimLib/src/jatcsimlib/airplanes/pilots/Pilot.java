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
import jatcsimlib.commands.AfterNavaidCommand;
import jatcsimlib.commands.AfterSpeedCommand;
import jatcsimlib.commands.ChangeAltitudeCommand;
import jatcsimlib.commands.ChangeHeadingCommand;
import jatcsimlib.commands.ChangeSpeedCommand;
import jatcsimlib.commands.ClearedToApproachCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.ContactCommand;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.commands.ShortcutCommand;
import jatcsimlib.commands.ThenCommand;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.exceptions.ERuntimeException;
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

  private final Airplane parent;
  private final String routeName;
  private final List<Command> queue = new LinkedList<>();
  private final List<Command> afterAltitudeCommands = new LinkedList<>();
  private final List<Command> afterNavaidCommands = new LinkedList<>();
  private final List<Command> afterSpeedCommands = new LinkedList<>();

  private ChangeHeadingCommand.eDirection targetHeadingDirection;
  private int targetHeading;
  private Coordinate targetCoordinate;
  private int targetAltitudeInFt;
  private int targetSpeedInKts;
  private HoldInfo hold;
  private ApproachInfo app;

  public String getRouteName() {
    return this.routeName;
  }

  public Pilot(Airplane parent, String routeName, List<Command> routeCommandQueue) {
    this.parent = parent;
    this.routeName = routeName;
    this.queue.addAll(routeCommandQueue);
  }

  public void processNewCommands(List<Command> cmds) {
    int index = 0;
    expandThenCommands(cmds);
    while (index < cmds.size()) {
      index = processNewCommand(cmds, index);
    }
  }

  private int processNewCommand(List<Command> cmds, int index) {
    int ret = index + 1;
    Command c = cmds.get(index);
    if (c instanceof ProceedDirectCommand) {
      this.queue.clear();
      this.queue.add(c);

    } else if (c instanceof ShortcutCommand) {
      ShortcutCommand t = (ShortcutCommand) c;
      int pointIndex = getIndexOfNavaidInCommands(t.getNavaid());
      if (pointIndex < 0) {
        Acc.messenger().addMessage(
            parent, parent.getAtc(), " Unable to shortcut to " + t.getNavaid().getName() + ", fix not on route!");
      } else {
        for (int i = 0; i < pointIndex; i++) {
          this.queue.remove(i);
        }
      }

    } else if (c instanceof ChangeHeadingCommand) {
      this.queue.clear();
      this.queue.add(c);
    } else if (c instanceof ChangeAltitudeCommand) {
      removeQueueCommands(AfterAltitudeCommand.class);
      removeQueueCommands(ChangeAltitudeCommand.class);
      this.queue.add(0, c);
    } else if (c instanceof ChangeSpeedCommand) {
      removeQueueCommands(AfterSpeedCommand.class);
      removeQueueCommands(ChangeSpeedCommand.class);
      this.queue.add(0, c);

    } else if (c instanceof AfterAltitudeCommand) {
      removeQueueCommands(AfterAltitudeCommand.class);
      this.queue.add(0, c);
      this.queue.add(1, cmds.get(index + 1));
      ret += 1;
    } else if (c instanceof AfterSpeedCommand) {
      removeQueueCommands(AfterSpeedCommand.class);
      this.queue.add(0, c);
      this.queue.add(1, cmds.get(index + 1));
      ret += 1;
    } else if (c instanceof AfterNavaidCommand) {
      removeQueueCommands(AfterNavaidCommand.class);
      this.queue.add(0, c);
      this.queue.add(1, cmds.get(index + 1));
      ret += 1;

    } else if (c instanceof ClearedToApproachCommand) {
      this.queue.clear();
      this.queue.add(0, c);
    } else if (c instanceof ContactCommand) {
      this.queue.add(0, c);
    } else {
      throw new ERuntimeException("Pilot cannot deal with command " + c.getClass().getSimpleName() + " - probably not implemented.");
    }
    return ret;
  }

  public void drivePlane() {
    
    /*
    
    1. zpracuji se prikazy ve fronte
    2. zkontroluje se, jestli neni neco "after"
    3. ridi se letadlo
    
    */
    processQueueCommands();
    processAfterCommands();
    endrivePlane();
  }

  private void processQueueCommands() {
    if (queue.isEmpty()) {
      return;
    }
    Command c = queue.get(0);
    if (c instanceof AfterCommand) {
      if (c instanceof AfterAltitudeCommand) {
        Command n = queue.get(1);
        afterAltitudeCommands.clear();
        afterAltitudeCommands.add(c);
        afterAltitudeCommands.add(n);
      } else if (c instanceof AfterSpeedCommand) {
        Command n = queue.get(1);
        afterSpeedCommands.clear();
        afterSpeedCommands.add(c);
        afterSpeedCommands.add(n);
      } else if (c instanceof AfterNavaidCommand) {
        Command n = queue.get(1);
        afterNavaidCommands.clear();
        afterNavaidCommands.add(c);
        afterNavaidCommands.add(n);
      }
      queue.remove(0);
      queue.remove(0);
    } else {
      boolean res = tryProcessQueueCommand(c);
      if (res) {
        queue.remove(0);
      }
    }
  }

  private boolean tryProcessQueueCommand(Command c) {
    Method m;
    m = tryGetProcessQueueCommandMethodToInvoke(c.getClass());

    boolean ret;
    try {
      ret = (boolean) m.invoke(null, c);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ERuntimeException("Format-command failed for " + c.getClass());
    }
    return ret;
  }

  private Method tryGetProcessQueueCommandMethodToInvoke(Class<? extends Command> commandType) {
    Method ret;
    try {
      ret = Pilot.class.getMethod("processQueueCommand", commandType);
    } catch (NoSuchMethodException | SecurityException ex) {
      ret = null;
    }

    return ret;
  }

  private boolean processQueueCommand(ProceedDirectCommand c) {
    targetHeading = -1;
    if (hold != null) {
      hold = null;
    }
    if (app != null) {
      app = null;
    }
    targetCoordinate = c.getNavaid().getCoordinate();
    return true;
  }

  private boolean processQueueCommand(ChangeAltitudeCommand c) {
    switch (c.getDirection()) {
      case climb:
        if (parent.getAltitude() > c.getAltitudeInFt()) {
          say(
              "Cannot perform command to climb to " + Acc.toAltS(c.getAltitudeInFt(), true) + " cos we are higher.");
          return true;
        }
        break;
      case descend:
        if (parent.getAltitude() < c.getAltitudeInFt()) {
          say(
              "Cannot perform command to descend to " + Acc.toAltS(c.getAltitudeInFt(), true) + " cos we are lower.");
          return true;
        }
        break;
    } // switch
    if (c.getAltitudeInFt() > parent.getAirplaneSpecification().maxAltitude) {
     say("Unable to climb to " + Acc.toAltS(parent.getAirplaneSpecification().maxAltitude, true) + ".");
      return true;
    }
    targetAltitudeInFt = c.getAltitudeInFt();
    say("Altitude " + Acc.toAltS(targetAltitudeInFt, true));
    return true;
  }

  private boolean processQueueCommand(ChangeSpeedCommand c) {
    switch (c.getDirection()) {
      case increase:
        if (parent.getSpeed() > c.getSpeedInKts()) {
          say(
              "Cannot perform command to speed up to " + c.getSpeedInKts() + " cos we are faster.");
          return true;
        }
        break;
      case decrease:
        if (parent.getAltitude() < c.getSpeedInKts()) {
          say(
              "Cannot perform command to descend to " + c.getSpeedInKts() + " cos we are slower.");
          return true;
        }
        break;
    } // switch
    if (app != null) {
      if (c.getSpeedInKts() < parent.getAirplaneSpecification().vMinApp
          || c.getSpeedInKts() > parent.getAirplaneSpecification().vMaxApp) {
        say(
            "At approach, we accept only speed between "
            + parent.getAirplaneSpecification().vMinApp + " kts and "
            + parent.getAirplaneSpecification().vMaxApp + " kts.");
        return true;
      }
    } else {
      if (c.getSpeedInKts() < parent.getAirplaneSpecification().vMinClean
          || c.getSpeedInKts() > parent.getAirplaneSpecification().vMaxClean) {
        say(
            "While cruise, we accept only speed between "
            + parent.getAirplaneSpecification().vMinClean + " kts and "
            + parent.getAirplaneSpecification().vMaxClean + " kts.");
        return true;
      }
    } // if (app != null)
    targetSpeedInKts = c.getSpeedInKts();
    say("Speed " + targetSpeedInKts + " kts.");
    return true;
  }

  private boolean processQueueCommand(ChangeHeadingCommand c) {
    targetCoordinate = null;
    targetHeading = c.getHeading();
    targetHeadingDirection = c.getDirection();
    say ("Heading " + c.getHeading());
    return true;
  }
  
  private boolean processQueueCommand(ContactCommand c){
    Atc a;
    switch (c.getAtcType()){
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
    Acc.messenger().addMessage(parent, a,
        parent.getCallsign().toString() + " with you at " + Acc.toAltS(parent.getAltitude(), true));
    return true;
  }
  
  private boolean processQueueCommand(ClearedToApproachCommand c){
    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    if (rt == null){
      say("There is no runway " + c.getRunwayThresholdName() + " we can approach to.");
      return true;
    }
    Approach a = rt.getApproaches().tryGet(c.getType());
    if (a == null){
      say("There is no approach of type " + c.getType() + " for runway " + rt.getName() + ".");
      return true;
    }
    
    if (hold != null) hold = null;
    this.app = new ApproachInfo(a);
    say("Cleared to " + a.getType() + " approach runway " + rt.getName() + ".");
    return true;
  }
  
  private void say (String text){
    Acc.messenger().addMessage(parent, parent.getAtc(), text);
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
              parent, parent.getAtc(), "\"THEN\" command cannot be first or last in queue. Whole command block is ignored.");
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
              parent, parent.getAtc(), "\"THEN\" command is after strange command, it does not make sense. Whole command block is ignored.");
          cmds.clear();
          return;
        }
        cmds.remove(i);
        cmds.add(i, n);
      }
    }
  }
}
