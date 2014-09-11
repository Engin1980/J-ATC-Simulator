/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.global.KeyItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Route implements KeyItem<String> {

  @Override
  public String getKey() {
    return name;
  }



  public enum eType {
    sid,
    star,
    transition;
    
    public boolean isArrival(){
      return this == star || this == transition;
    }
  }

  private eType type;
  private String name;
  private String route;
  private RunwayThreshold parent;

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

  private List<Command> _routeCommands = null;

  public List<Command> getCommands() {
    if (_routeCommands != null) {
      return _routeCommands;
    }

    _routeCommands = CommandFormat.parseMulti(this.route);
    return _routeCommands;
  }
  
    public List<Command> getCommandsListClone() {
      List<Command> ret = new LinkedList<>();
      ret.addAll(_routeCommands);
      return ret;
  }

  private Navaid _mainFix = null;

  public Navaid getMainFix() {
    if (_mainFix != null) {
      return _mainFix;
    }

    switch (type) {
      case sid:
        _mainFix = tryGetSidMainFix(getCommands());
        break;
      case star:
      case transition:
        _mainFix = tryGetStarMainFix(getCommands());
        break;
      default:
        throw new ERuntimeException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (commands: " + this.route + ")");
    }
    return _mainFix;
  }

  private Navaid tryGetSidMainFix(List<Command> commands) {
    Command c = commands.get(commands.size() - 1);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }

  private Navaid tryGetStarMainFix(List<Command> commands) {
    Command c = commands.get(0);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }
}
