/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.exceptions.EBindException;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.MustBeBinded;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Route extends MustBeBinded implements KeyItem<String> {

  @Override
  public String getKey() {
    return name;
  }

  public enum eType {

    sid,
    star,
    transition;

    public boolean isArrival() {
      return this == star || this == transition;
    }
  }

  private eType type;
  private String name;
  private String route;
  private RunwayThreshold parent;
  private List<Command> _routeCommands = null;
  private Navaid _mainFix = null;

  public eType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getRoute() {
    return route;
  }

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }

  @Override
  protected void _bind() {
    try {
      _routeCommands = CommandFormat.parseMulti(this.route);
    } catch (Exception ex) {
      throw new EBindException("Parsing commands failed. Commands contain error (see cause).", ex);
    }

    switch (type) {
      case sid:
        _mainFix = tryGetSidMainFix();
        break;
      case star:
      case transition:
        _mainFix = tryGetStarMainFix();
        break;
      default:
        throw new EBindException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (commands: " + this.route + ")");
    }
  }

  public List<Command> getCommands() {
    super.checkBinded();

    return _routeCommands;
  }

  public List<Command> getCommandsListClone() {
    super.checkBinded();

    List<Command> ret = new LinkedList<>();
    ret.addAll(_routeCommands);
    return ret;
  }

  public Navaid getMainFix() {
    super.checkBinded();

    return _mainFix;
  }

  private Navaid tryGetSidMainFix() {
    Command c = _routeCommands.get(_routeCommands.size() - 1);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }

  private Navaid tryGetStarMainFix() {
    Command c = _routeCommands.get(0);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }
}
