package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.utilites.EnumUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.BorderPoint;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class BorderXmlLoader implements IXmlLoader<Border> {

  private static final Border.eType[] TYPES_MUST_HAVE_MAX_ALTITUDE = {
      Border.eType.tma,
      Border.eType.ctr,
      Border.eType.restricted,
      Border.eType.danger,
      Border.eType.mrva,
      Border.eType.other
  };
  private static final Border.eType[] TYPES_MUST_HAVE_MIN_ALTITUDE = {
      Border.eType.tma,
      Border.eType.ctr,
      Border.eType.restricted,
      Border.eType.danger,
      Border.eType.other
  };

  public Border load(XElement source) {
    log(1, "Xml-loading border");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    Border.eType type = SmartXmlLoaderUtils.loadEnum("type", Border.eType.class);
    log(1, "... border '%s (%s)'", name, type.toString());
    boolean enclosed = SmartXmlLoaderUtils.loadBoolean("enclosed");
    int minAltitude;
    int maxAltitude;
    if (EnumUtils.is(type, TYPES_MUST_HAVE_MIN_ALTITUDE))
      minAltitude = SmartXmlLoaderUtils.loadAltitude("minAltitude");
    else
      minAltitude = SmartXmlLoaderUtils.loadAltitude("minAltitude", 0);

    if (EnumUtils.is(type, TYPES_MUST_HAVE_MAX_ALTITUDE))
      maxAltitude = SmartXmlLoaderUtils.loadAltitude("maxAltitude");
    else
      maxAltitude = SmartXmlLoaderUtils.loadAltitude("maxAltitude", 99999);
    Coordinate labelCoordinate = SmartXmlLoaderUtils.loadCoordinate("labelCoordinate", null);

    IReadOnlyList<XElement> pointElements = source.getChild("points").getChildren();
    IList<BorderPoint> points;
    if (pointElements.size() == 1 && pointElements.get(0).getName().equals("circle"))
      points = loadPointsFromCircle(pointElements.get(0));
    else
      points = loadPoints(source.getChild("points").getChildren());

    IList<String> disjoints = new EList<>();
    XElement disjointsElement = source.tryGetChild("disjoints");
    if (disjointsElement != null)
      disjointsElement.getChildren().forEach(q -> disjoints.add(q.getContent()));


    Border ret = Border.create(name, type, enclosed, minAltitude, maxAltitude, labelCoordinate, points, disjoints);
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
          EAssert.isTrue(ret.size() > 0, "Cannot add 'arc' point to border as the first item.");
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
      BorderPoint beforeBorderPoint = ret.get(index - 1);
      BorderPoint afterBorderPoint = index == ret.count() ? ret.get(0) : ret.get(index);
      IList<BorderPoint> arcPoints = generateArcPoints(
          beforeBorderPoint, coordinate, isClockwise, afterBorderPoint);
      ret.insert(index, arcPoints);
    }

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
    ret.addMany(tmp);
    return ret;
  }
}
