/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.commands.ChangeHeadingCommand;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimlib.commands.ProceedDirectCommand;
import jatcsimlib.commands.ToNavaidCommand;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.EBindException;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.MustBeBinded;
import jatcsimlib.global.XmlOptional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Route extends MustBeBinded implements KeyItem<String> {

  public static Route createNewByFix(Navaid n, boolean arrival) {
    Route ret = new Route();
    
    ret.name = n.getName();
    
    ret._routeCommands = new ArrayList<>();
    if (arrival){
      ret._routeCommands.add(new ProceedDirectCommand(n));
    }
    ret._mainFix = n;
    ret.type = eType.vectoring;
    ret._routeCommands.add(new ProceedDirectCommand(n));
    ret.route = "";

    ret.bind();
    
    return ret;
  }

  @Override
  public String getKey() {
    return name;
  }

  private double calculateRouteLength() {
    double ret = 0;
    Navaid prev = null;
    
    for (Command cmd : this._routeCommands){
      if ((cmd instanceof ProceedDirectCommand) == false) continue;
      
      if (prev == null)
        prev = ((ProceedDirectCommand) cmd).getNavaid();
      else{
        Navaid curr = ((ProceedDirectCommand)cmd).getNavaid();
        double dist = Coordinates.getDistanceInNM(prev.getCoordinate(), curr.getCoordinate());
        ret += dist;
        prev = curr;
      }
    }

    return ret;
  }

  public enum eType {

    sid,
    star,
    transition,
    vectoring;

    public boolean isArrival() {
      return this == star || this == transition;
    }
  }

  private eType type;
  private String name;
  private String route;
  private RunwayThreshold parent;
  @XmlOptional
  private String category = null;
  private List<Command> _routeCommands = null;
  private List<Navaid> _routeNavaids = null;
  private double _routeLength = -1;
  private Navaid _mainFix = null;

  public eType getType() {
    return type;
  }
  
  public double getRouteLength(){
    return _routeLength;
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

  public String getCategory() {
    return this.category;
  }

  public boolean isValidForCategory(char categoryChar) {
    if (this.category.indexOf(categoryChar) > -1) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  protected void _bind() {
    try {
      _routeCommands = CommandFormat.parseMulti(this.route);
    } catch (Exception ex) {
      throw new EBindException("Parsing commands failed for route " + this.name + ". Route commands contain error (see cause).", ex);
    }

    switch (type) {
      case sid:
        _mainFix = tryGetSidMainFix();
        break;
      case star:
      case transition:
        _mainFix = tryGetStarMainFix();
        break;
      case vectoring:
        if (_mainFix == null)
          throw new EBindException("\"Vectoing\" route must have set _mainFix explicitly.");
        break;
      default:
        throw new EBindException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (commands: " + this.route + ")");
    }

    _routeNavaids = new ArrayList<>();
    for (Command c : _routeCommands) {
      if (c instanceof ToNavaidCommand) {
        _routeNavaids.add(((ToNavaidCommand) c).getNavaid());
      }
    }

    if (this.category == null || this.category.isEmpty()) {
      this.category = "ABCD";
    } else {
      this.category = this.category.toUpperCase();
    }
    
    _routeLength = calculateRouteLength();
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

  public List<Navaid> getNavaids() {
    return this._routeNavaids;
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
