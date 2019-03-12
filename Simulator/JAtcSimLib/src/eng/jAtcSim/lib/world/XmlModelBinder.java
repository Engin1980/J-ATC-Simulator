package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.world.xmlModel.*;

public class XmlModelBinder {

  public static class Context{
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

  public static PublishedHold convert(XmlPublishedHold x, Context context){
    Navaid navaid = context.area.getNavaids().get(x.navaidName);
    PublishedHold ret = new PublishedHold(navaid, x.inboundRadial, x.turn.equals("left"), context.airport);
    return ret;
  }

  public static Border convert(XmlBorder x, Context context){
    if (x.points.size() > 1 && x.points.isAny(q -> q instanceof XmlBorderCirclePoint)) {
      throw new EApplicationException("Border " + x.getName() + " is not valid. If <circle> is used, it must be the only element in the <points> list.");
    }
    IList<XmlBorderExactPoint> exactPoints = expandArcsToPoints(x.points);

    this.globalMinLat = exactPoints.minDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMaxLat = exactPoints.maxDouble(q -> q.getCoordinate().getLatitude().get());
    this.globalMinLng = exactPoints.minDouble(q -> q.getCoordinate().getLongitude().get());
    this.globalMaxLng = exactPoints.maxDouble(q -> q.getCoordinate().getLongitude().get());
  }

  private void expandArcsToPoints(XmlBorder x) {
    IList<XmlBorderPoint> points = x.points;

    // expand circle
    if (points.size() > 0 && points.get(0) instanceof BorderCirclePoint) {
      XmlBorderCirclePoint bcp = (XmlBorderCirclePoint) points.get(0);
      x.labelCoordinate = bcp.getCoordinate();
      points.clear();
      points.add(new BorderCrdPoint(bcp.getCoordinate(), 0, bcp.getDistance()));
      points.add(new BorderArcPoint(bcp.getCoordinate(), BorderArcPoint.eDirection.clockwise));
      points.add(new BorderCrdPoint(bcp.getCoordinate(), 180, bcp.getDistance()));
      points.add(new BorderArcPoint(bcp.getCoordinate(), BorderArcPoint.eDirection.clockwise));
      x.enclosed = true;
    }

    if (x.isEnclosed() && !x.points.get(0).equals(x.points.get(x.points.size() - 1)))
      x.points.add(x.points.get(0));

    // replace CRD to Exact
    IList<XmlBorderPoint> lst = new EList<>(x.points);
    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof XmlBorderCrdPoint) {
        BorderCrdPoint bcp = (BorderCrdPoint) lst.get(i);
        Coordinate c = Coordinates.getCoordinate(bcp.getCoordinate(), bcp.getRadial(), bcp.getDistance());
        BorderExactPoint bep = new BorderExactPoint(c);
        lst.set(i, bep);
      }
    }

    this.exactPoints = new EList<>();

    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof BorderExactPoint)
        this.exactPoints.add((BorderExactPoint) lst.get(i));
      else {
        BorderExactPoint prev = (BorderExactPoint) lst.get(i - 1);
        BorderArcPoint curr = (BorderArcPoint) lst.get(i);
        BorderExactPoint next = (BorderExactPoint) lst.get(i + 1);
        IList<BorderExactPoint> tmp = generateArcPoints(prev, curr, next);
        this.exactPoints.add(tmp);
      }
    }
  }

  private IList<BorderExactPoint> generateArcPoints(BorderExactPoint prev, BorderArcPoint curr, BorderExactPoint next) {
    IList<BorderExactPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(curr.getCoordinate(), prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(curr.getCoordinate(), next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(curr.getCoordinate(), prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(curr.getCoordinate(), next.getCoordinate())) / 2;
    double step;
    if (curr.getDirection() == BorderArcPoint.eDirection.clockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else if (curr.getDirection() == BorderArcPoint.eDirection.counterclockwise) {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    } else {
      throw new UnsupportedOperationException("This combination is not supported.");
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(curr.getCoordinate(), pt, dist);
        BorderExactPoint p = new BorderExactPoint(c);
        ret.add(p);
      }
    }

    return ret;
  }
}
