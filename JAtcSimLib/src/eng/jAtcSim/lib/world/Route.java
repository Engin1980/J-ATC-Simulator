/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.MustBeBinded;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
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
 * @author Marek
 */
public class Route extends MustBeBinded {

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
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
  private SpeechList<IAtcCommand> _routeCommands = null;
  private List<Navaid> _routeNavaids = null;
  private double _routeLength = -1;
  @XmlOptional
  private Navaid mainFix = null;
  @XmlOptional
  private Integer entryFL;

  public Integer getEntryFL() {
    return entryFL;
  }

  public static Route createNewByFix(Navaid n, boolean arrival) {
    Route ret = new Route();

    ret.name = n.getName();

    ret._routeCommands = new SpeechList<>();
    if (arrival) {
      ret._routeCommands.add(new ProceedDirectCommand(n));
    }
    ret.mainFix = n;
    ret.type = eType.vectoring;
    ret._routeCommands.add(new ProceedDirectCommand(n));
    ret.route = "";

    ret.bind();

    return ret;
  }

  public eType getType() {
    return type;
  }

  public double getRouteLength() {
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

  public PlaneCategoryDefinitions getCategory() {
    return this.category;
  }

  public boolean isValidForCategory(char categoryChar) {
    boolean ret = this.category.contains(categoryChar);
    return ret;
  }

  public SpeechList<IAtcCommand> getCommands() {
    super.checkBinded();

    return _routeCommands;
  }

  public SpeechList<IAtcCommand> getCommandsListClone() {
    super.checkBinded();

    SpeechList<IAtcCommand> ret = new SpeechList<>();
    ret.add(_routeCommands);
    return ret;
  }

  public List<Navaid> getNavaids() {
    return this._routeNavaids;
  }

  public Navaid getMainFix() {
    super.checkBinded();

    return mainFix;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }

  private double calculateRouteLength() {
    double ret = 0;
    Navaid prev = null;

    for (ICommand cmd : this._routeCommands) {
      if ((cmd instanceof ProceedDirectCommand) == false) continue;

      if (prev == null)
        prev = ((ProceedDirectCommand) cmd).getNavaid();
      else {
        Navaid curr = ((ProceedDirectCommand) cmd).getNavaid();
        double dist = Coordinates.getDistanceInNM(prev.getCoordinate(), curr.getCoordinate());
        ret += dist;
        prev = curr;
      }
    }

    return ret;
  }

  @Override
  protected void _bind() {
    try {
      Parser p = new ShortParser();
      SpeechList<IFromAtc> xlst = p.parseMulti(this.route);
      _routeCommands = xlst.convertTo();
    } catch (Exception ex) {
      throw new EBindException("Parsing fromAtc failed for route " + this.name + ". Route fromAtc contains error (see cause).", ex);
    }

    // when "main fix" was not explicitly specified
    if (mainFix == null) {
      switch (type) {
        case sid:
          mainFix = tryGetSidMainFix();
          break;
        case star:
        case transition:
          mainFix = tryGetStarMainFix();
          break;
        case vectoring:
          if (mainFix == null)
            throw new EBindException("\"Vectoing\" route must have set mainFix explicitly.");
          break;
        default:
          throw new EBindException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (fromAtc: " + this.route + ")");
      }
    }

    _routeNavaids = new ArrayList<>();
    for (ICommand c : _routeCommands) {
      if (c instanceof ToNavaidCommand) {
        _routeNavaids.add(((ToNavaidCommand) c).getNavaid());
      }
    }

    _routeLength = calculateRouteLength();
  }

  private Navaid tryGetSidMainFix() {
    int index = _routeCommands.size() - 1;
    while (index >= 0 && !(_routeCommands.get(index) instanceof ProceedDirectCommand)) {
      index--;
    }
    if (index < 0)
      throw new EApplicationException("Failed to find main navaid for route " + this.name + ". Route commands probably not well defined.");

    ProceedDirectCommand c = (ProceedDirectCommand) _routeCommands.get(index);
    return c.getNavaid();
  }

  private Navaid tryGetStarMainFix() {
    int index = 0;
    while (index < _routeCommands.size() && !(_routeCommands.get(index) instanceof ProceedDirectCommand))
      index++;

    if (index >= _routeCommands.size())
      throw new EApplicationException("Failed to find main navaid for route " + this.name + ". Route commands probably not well defined.");

    ProceedDirectCommand c = (ProceedDirectCommand) _routeCommands.get(index);
    return c.getNavaid();
  }
}
