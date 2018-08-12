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
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlOptional;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;
import eng.jAtcSim.lib.speaking.parsing.Parser;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.ShortBlockParser;

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
  private Airport parent;
  @XmlOptional
  private PlaneCategoryDefinitions category = PlaneCategoryDefinitions.getAll();
  private SpeechList<IAtcCommand> _routeCommands = null;
  private IList<Navaid> _routeNavaids = null;
  private double _routeLength = -1;
  @XmlOptional
  private String mainFix = null;
  private Navaid _mainNavaid = null;
  @XmlOptional
  private Integer entryFL = null;
  private Integer _maxMrvaFL = null;

  public static Route createNewVectoringByFix(Navaid n, boolean arrival) {
    Route ret = new Route();

    ret.name = n.getName() + "/v";

    ret._routeCommands = new SpeechList<>();
    if (arrival) {
      ret._routeCommands.add(new ProceedDirectCommand(n));
    }
    ret._mainNavaid = n;
    ret.type = eType.vectoring;
    ret._routeCommands.add(new ProceedDirectCommand(n));
    ret.route = "";
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
    if (type == eType.vectoring)
      System.out.println("\test");

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
    ret._mainNavaid = this._mainNavaid;
    ret.entryFL = this.entryFL;
    ret._maxMrvaFL = this._maxMrvaFL;
    return ret;
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
    else{
      int endIndex = 0;
      while (endIndex < this.name.length()){
        char c = this.name.charAt(endIndex);
        if (Character.isDigit(c)){
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
