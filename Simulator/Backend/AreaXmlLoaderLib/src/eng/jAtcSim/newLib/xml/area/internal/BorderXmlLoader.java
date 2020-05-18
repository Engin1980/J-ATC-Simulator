package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class BorderXmlLoader implements IXmlLoader<Border> {

  public Border load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    Border.eType type = SmartXmlLoaderUtils.loadEnum("type", Border.eType.class);
    boolean enclosed = SmartXmlLoaderUtils.loadBoolean("enclosed");
    int minAltitude = SmartXmlLoaderUtils.loadInteger("minAltitude");
    int maxAltitude = SmartXmlLoaderUtils.loadInteger("maxAltitude");
    Coordinate labelCoordinate = SmartXmlLoaderUtils.loadCoordinate("labelCoordinate", null);

    IReadOnlyList<XElement> pointElements = source.getChild("points").getChildren();
    IList<BorderPoint> points;
    if (pointElements.size() == 1 && pointElements.get(0).getName().equals("circle"))
      points = loadPointsFromCircle(pointElements.get(0));
    else
      points = loadPoints(source.getChild("points").getChildren());

    IList<String> disjoints = new EList<>();
    source.getChild("disjoints").getChildren().forEach(q -> disjoints.add(q.getContent()));


    Border ret = Border.create(name, type, enclosed, minAltitude, maxAltitude, labelCoordinate, points, disjoints);
    return ret;
  }

  private IList<BorderPoint> loadPointsFromCircle(XElement source) {
    IList<BorderPoint> ret = new EList<>();
    Coordinate coord = SmartXmlLoaderUtils.loadCoordinate(source, "coordinate");
    double dist = SmartXmlLoaderUtils.loadDouble(source, "distance");
    Coordinate pointCoordinate = Coordinates.getCoordinate(coord, 0, dist);
    BorderPoint point = BorderPoint.create(pointCoordinate);
    IList<BorderPoint> tmp = generateArcPoints(point, coord, true, point);
    ret.add(point);
    ret.add(tmp);
    return ret;
  }

  private IList<BorderPoint> loadPoints(IReadOnlyList<XElement> nodes) {
    IList<BorderPoint> ret = new EList<>();
    IList<Tuple<Integer, XElement>> arcTuples = new EList<>();
    BorderPoint point;
    Coordinate coordinate;

    for (XElement node : nodes) {
      switch (node.getName()) {
        case "point":
          coordinate = SmartXmlLoaderUtils.loadCoordinate(node, "coordinate");
          point = BorderPoint.create(coordinate);
          ret.add(point);
          break;
        case "arc":
          arcTuples.add(new Tuple<>(ret.size(), node));
          break;
        case "crd":
          coordinate = SmartXmlLoaderUtils.loadCoordinate(node, "coordinate");
          int radial = SmartXmlLoaderUtils.loadInteger(node, "radial");
          double distance = SmartXmlLoaderUtils.loadDouble(node, "distance");
          Coordinate borderPointCoordinate = Coordinates.getCoordinate(
              coordinate, radial, distance);
          point = BorderPoint.create(borderPointCoordinate);
          ret.add(point);
          break;
        default:
          throw new EApplicationException(sf("Unknown type of point '%s' in border.", node.getName()));
      }
    }

    for (Tuple<Integer, XElement> arcTuple : arcTuples) {
      int index = arcTuple.getA();
      coordinate = SmartXmlLoaderUtils.loadCoordinate(arcTuple.getB(), "coordinate");
      boolean isClockwise = SmartXmlLoaderUtils.loadStringRestricted(arcTuple.getB(), "direction",
          new String[]{"clockwise", "counterclockwise"}).equals("clockwise");
      IList<BorderPoint> arcPoints = generateArcPoints(
          ret.get(index - 1), coordinate, isClockwise, ret.get(index));
      ret.insert(index, arcPoints);
    }

    return ret;
  }

  private IList<BorderPoint> generateArcPoints(BorderPoint prev, Coordinate currCoordinate, boolean isClockwise, BorderPoint next) {
    final int BORDER_ARC_POINT_DRAW_STEP = 10;
    IList<BorderPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(currCoordinate, prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(currCoordinate, next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(currCoordinate, prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(currCoordinate, next.getCoordinate())) / 2;
    double step;
    if (isClockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % BORDER_ARC_POINT_DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(currCoordinate, pt, dist);
        BorderPoint p = BorderPoint.create(c);
        ret.add(p);
      }
    }

    return ret;
  }
}
