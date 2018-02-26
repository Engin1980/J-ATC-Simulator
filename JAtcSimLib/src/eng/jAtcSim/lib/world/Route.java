/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.KeyItem;
import eng.jAtcSim.lib.global.MustBeBinded;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.ShortParser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Route extends MustBeBinded implements KeyItem<String> {

  public static Route createNewByFix(Navaid n, boolean arrival) {
    Route ret = new Route();
    
    ret.name = n.getName();
    
    ret._routeCommands = new SpeechList<>();
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
    
    for (ICommand cmd : this._routeCommands){
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
  private SpeechList<IAtcCommand> _routeCommands = null;
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
      Parser p = new ShortParser();
      SpeechList<IFromAtc> xlst = p.parseMulti(this.route);
      _routeCommands =  xlst.convertTo();
    } catch (Exception ex) {
      throw new EBindException("Parsing fromAtc failed for route " + this.name + ". Route fromAtc contain error (see cause).", ex);
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
        throw new EBindException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (fromAtc: " + this.route + ")");
    }

    _routeNavaids = new ArrayList<>();
    for (ICommand c : _routeCommands) {
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

  public SpeechList<IAtcCommand> getCommands() {
    super.checkBinded();

    return _routeCommands;
  }

  public SpeechList<IAtcCommand> getCommandsListClone() {
    super.checkBinded();

    SpeechList<IAtcCommand> ret = new SpeechList<>();
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
    ICommand c = _routeCommands.get(_routeCommands.size() - 1);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }

  private Navaid tryGetStarMainFix() {
    ICommand c = _routeCommands.get(0);
    if (c instanceof ProceedDirectCommand) {
      return ((ProceedDirectCommand) c).getNavaid();
    } else {
      throw null;
    }
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }
}
