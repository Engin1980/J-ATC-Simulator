/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;

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
  @XmlIgnore
  private Airport parent;
  @XmlOptional
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
  @XmlIgnore
  private SpeechList<IAtcCommand> _routeCommands = null;
  @XmlIgnore
  private IList<Navaid> _routeNavaids = null;
  @XmlIgnore
  private double _routeLength = -1;
  @XmlOptional
  private String mainFix = null;
  @XmlIgnore
  private Navaid _mainNavaid = null;
  @XmlOptional
  private Integer entryFL = null;
  @XmlIgnore
  private Integer _maxMrvaFL = null;

  public static Route createNewVectoringByFix(Navaid n, boolean arrival) {
    Route ret = new Route();

    ret.name = n.getName() + "/v";

    ret._routeCommands = new SpeechList<>();
    if (arrival)
      ret.route = "PD " + n.getName();
    else
      ret.route = "FH";
    ret._mainNavaid = n;
    ret.type = eType.vectoring;
    ret.parent = Acc.airport(); // only formal for binding
    ret.bind();

    return ret;
  }

  public Integer getEntryFL() {
    return entryFL;
  }

  public int getMaxMrvaAltitude() {
    int ret = _maxMrvaFL == null ? 0 : _maxMrvaFL * 100;
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

  public Airport getParent() {
    return parent;
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

    _routeNavaids = _routeCommands
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaid());

    Navaid customFix = null;
    if (this.mainFix != null)
      customFix = Acc.area().getNavaids().get(mainFix);
    switch (type) {
      case sid:
        this._mainNavaid = customFix == null ? getFixByRouteName() : customFix;
        if (!_routeNavaids.isEmpty() && !_routeNavaids.getLast().equals(this._mainNavaid))
          _routeNavaids.add(this._mainNavaid);
        break;
      case star:
      case transition:
        this._mainNavaid = customFix == null ? getFixByRouteName() : customFix;
        if (!_routeNavaids.isEmpty() && !_routeNavaids.getFirst().equals(this._mainNavaid))
          _routeNavaids.insert(0, this._mainNavaid);
        break;
      case vectoring:
        // nothing
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }


    _routeLength = calculateRouteLength();


    // min alt
    Area area = this.getParent().getParent();
    IList<Border> mrvas = area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(this._routeNavaids);

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (hasMrvaIntersection(pointLines, mrva))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }
    if (maxMrvaAlt == 0) {
      Navaid routePoint = this._mainNavaid;
      Border mrva = mrvas.tryGetFirst(q -> q.isIn(routePoint.getCoordinate()));
      if (mrva != null)
        maxMrvaAlt = mrva.getMaxAltitude();
    }
    this._maxMrvaFL = maxMrvaAlt / 100;

    // hold at the end of SID via main point
    if (this.type == eType.sid) {
      ToNavaidCommand tnc = (ToNavaidCommand) this._routeCommands.tryGetLast(q -> q instanceof ToNavaidCommand);
      assert tnc != null : "No ToNavaidCommand in SID???";
      if (tnc instanceof HoldCommand == false) {
        this._routeCommands.add(new HoldCommand(tnc.getNavaid(), 270, true));
      }
    }

  }

  public Navaid getMainNavaid() {
    return _mainNavaid;
  }

  public void setParent(Airport airport) {
    this.parent = airport;
  }

  private boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
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

  private Navaid getFixByRouteName() {
    Navaid ret;
    String name;
    if (mainFix != null)
      name = mainFix;
    else {
      int endIndex = 0;
      while (endIndex < this.name.length()) {
        char c = this.name.charAt(endIndex);
        if (Character.isDigit(c)) {
          break;
        }
        endIndex++;
      }
      name = this.name.substring(0, endIndex);
    }
    ret = Acc.area().getNavaids().get(name);
    return ret;
  }
}


