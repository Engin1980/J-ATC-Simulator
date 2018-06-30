/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.ShortBlockParser;

import java.awt.geom.Line2D;

/**
 * @author Marek
 */
public class Route {

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
  private IList<Navaid> _routeNavaids = null;
  private double _routeLength = -1;
  @XmlOptional
  private Navaid mainFix = null;
  @XmlOptional
  private Integer entryFL = null;
  @XmlIgnore
  private Integer maxMrvaFL = null;

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

  public Integer getEntryFL() {
    return entryFL;
  }

  public int getMaxMrvaAltitude() {
    int ret = maxMrvaFL == null ? 0 : maxMrvaFL * 100;
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
    return _routeCommands;
  }

  public SpeechList<IAtcCommand> getCommandsListClone() {
    SpeechList<IAtcCommand> ret = new SpeechList<>();
    ret.add(_routeCommands);
    return ret;
  }

  public IReadOnlyList<Navaid> getNavaids() {
    return this._routeNavaids;
  }

  public Navaid getMainFix() {
    return mainFix;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }

  public void bind() {
    try {
      Parser p = new ShortBlockParser();
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
            throw new EBindException("\"Vectoring\" route must have set mainFix explicitly.");
          break;
        default:
          throw new EBindException("Failed to obtain main route fix of route " + this.name + ". SID last/STAR first command must be \"proceed direct\" (fromAtc: " + this.route + ")");
      }
    }

    _routeNavaids = new EList<>();
    for (ICommand c : _routeCommands) {
      if (c instanceof ToNavaidCommand) {
        _routeNavaids.add(((ToNavaidCommand) c).getNavaid());
      }
    }

    _routeLength = calculateRouteLength();

    // min alt
    Area area = this.getParent().getParent().getParent().getParent();
    IList<Border> mrvas = area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(this._routeNavaids);

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (hasMrvaIntersection(pointLines, mrva))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }
    if (maxMrvaAlt == 0){
      Navaid routePoint = this.getMainFix();
      Border mrva = mrvas.tryGetFirst(q->q.isIn(routePoint.getCoordinate()));
      if (mrva != null)
          maxMrvaAlt = mrva.getMaxAltitude();
    }
    this.maxMrvaFL = maxMrvaAlt / 100;
  }

  public Route makeClone() {
    Route ret = new Route();
    ret.type = this.type;
    ret.name = this.name;
    ret.route = this.route;
    ret.category = this.category.makeClone();
    ret._routeCommands = new SpeechList<>(this._routeCommands);
    ret._routeNavaids = new EList<>(this._routeNavaids);
    ret._routeLength = this._routeLength;
    ret.mainFix = this.mainFix;
    ret.entryFL = this.entryFL;
    ret.maxMrvaFL = this.maxMrvaFL;
    return ret;
  }

  private boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q->mrva.hasIntersectionWithLine(q));
    return ret;
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

  private IList<Tuple<Coordinate, Coordinate>> convertPointsToLines(IList<Navaid> points) {
    IList<Tuple<Coordinate, Coordinate>> ret = new EList<>();

    for (int i = 1; i < points.size(); i++) {
      Navaid bef = points.get(i - 1);
      Navaid aft = points.get(i);
      ret.add(new Tuple<>(bef.getCoordinate(), aft.getCoordinate()));
    }

    return ret;
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
