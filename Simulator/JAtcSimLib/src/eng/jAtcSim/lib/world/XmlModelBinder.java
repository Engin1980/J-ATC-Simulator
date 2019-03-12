package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.xmlModel.*;

public class XmlModelBinder {

  public static class Context {
    public final Area area;
    public final Airport airport;

    public Context(Area area, Airport airport) {
      this.area = area;
      this.airport = airport;
    }
  }

  public static Navaid convert(XmlNavaid x) {
    Navaid ret = new Navaid(x.name, x.type, x.coordinate);
    return ret;
  }

  public static PublishedHold convert(XmlPublishedHold x, Context context) {
    Navaid navaid = context.area.getNavaids().get(x.navaidName);
    PublishedHold ret = new PublishedHold(navaid, x.inboundRadial, x.turn.equals("left"), context.airport);
    return ret;
  }

  public static Border convert(XmlBorder x) {
    if (x.points.size() > 1 && x.points.isAny(q -> q instanceof XmlBorderCirclePoint)) {
      throw new EApplicationException("Border " + x.getName() + " is not valid. If <circle> is used, it must be the only element in the <points> list.");
    }
    IList<BorderPoint> exactPoints = expandArcsToPoints(x);

    Border ret = new Border(x.name, x.type,
        exactPoints, x.enclosed,
        x.minAltitude, x.maxAltitude,
        x.labelCoordinate);
    return ret;


  }

  public static Area convert(XmlArea area) {
    IList<Airport> airports = new EList<>();
    IList<Border> borders = new EList<>();
    NavaidList navaids = new NavaidList();
    Area ret = new Area(area.getIcao(), airports, navaids, borders);

    Context context = new Context(ret, null);

    for (XmlNavaid xmlNavaid : area.getNavaids()) {
      Navaid navaid = XmlModelBinder.convert(xmlNavaid);
      navaids.add(navaid);
    }

    for (XmlBorder xmlBorder : area.getBorders()) {
      Border border = XmlModelBinder.convert(xmlBorder);
      borders.add(border);
    }

//    for (XmlAirport xmlAirport : area.getAirports()) {
//      Airport airport = XmlModelBinder.convert(xmlAirport);
//      airports.add(airport);
//    }

    return ret;
  }

  private static BorderPoint create(XmlBorderExactPoint xmlBorderExactPoint) {
    BorderPoint ret = new BorderPoint(xmlBorderExactPoint.getCoordinate());
    return ret;
  }

  private static IList<BorderPoint> expandArcsToPoints(XmlBorder x) {
    IList<XmlBorderPoint> points = x.points;
    IList<XmlBorderExactPoint> exp = new EList<>();
    IList<BorderPoint> ret = new EList<>();

    // expand circle
    if (points.size() > 0 && points.get(0) instanceof XmlBorderCirclePoint) {
      XmlBorderCirclePoint bcp = (XmlBorderCirclePoint) points.get(0);
      x.labelCoordinate = bcp.getCoordinate();
      points.clear();
      points.add(new XmlBorderCrdPoint(bcp.getCoordinate(), 0, bcp.getDistance()));
      points.add(new XmlBorderArcPoint(bcp.getCoordinate(), XmlBorderArcPoint.eDirection.clockwise));
      points.add(new XmlBorderCrdPoint(bcp.getCoordinate(), 180, bcp.getDistance()));
      points.add(new XmlBorderArcPoint(bcp.getCoordinate(), XmlBorderArcPoint.eDirection.clockwise));
      x.enclosed = true;
    }

    if (x.isEnclosed() && !x.points.get(0).equals(x.points.get(x.points.size() - 1)))
      x.points.add(x.points.get(0));

    // replace CRD to Exact
    IList<XmlBorderPoint> lst = new EList<>(x.points);
    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof XmlBorderCrdPoint) {
        XmlBorderCrdPoint bcp = (XmlBorderCrdPoint) lst.get(i);
        Coordinate c = Coordinates.getCoordinate(bcp.getCoordinate(), bcp.getRadial(), bcp.getDistance());
        XmlBorderExactPoint bep = new XmlBorderExactPoint(c);
        lst.set(i, bep);
      }
    }

    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof XmlBorderExactPoint)
        exp.add((XmlBorderExactPoint) lst.get(i));
      else {
        XmlBorderExactPoint prev = (XmlBorderExactPoint) lst.get(i - 1);
        XmlBorderArcPoint curr = (XmlBorderArcPoint) lst.get(i);
        XmlBorderExactPoint next = (XmlBorderExactPoint) lst.get(i + 1);
        IList<XmlBorderExactPoint> tmp = generateArcPoints(prev, curr, next);
        exp.add(tmp);
      }
    }

    for (XmlBorderExactPoint xmlBorderExactPoint : exp) {
      BorderPoint bp = XmlModelBinder.create(xmlBorderExactPoint);
      ret.add(bp);
    }
    return ret;
  }
private static final int BORDER_ARC_POINT_DRAW_STEP = 10;
  private IList<XmlBorderExactPoint> generateArcPoints(XmlBorderExactPoint prev, XmlBorderArcPoint curr, XmlBorderExactPoint next) {
    IList<XmlBorderExactPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(curr.getCoordinate(), prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(curr.getCoordinate(), next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(curr.getCoordinate(), prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(curr.getCoordinate(), next.getCoordinate())) / 2;
    double step;
    if (curr.getDirection() == XmlBorderArcPoint.eDirection.clockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else if (curr.getDirection() == XmlBorderArcPoint.eDirection.counterclockwise) {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    } else {
      throw new UnsupportedOperationException("This combination is not supported.");
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % BORDER_ARC_POINT_DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(curr.getCoordinate(), pt, dist);
        XmlBorderExactPoint p = new XmlBorderExactPoint(c);
        ret.add(p);
      }
    }

    return ret;
  }
}
