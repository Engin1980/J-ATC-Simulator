package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

class EntryExitPointXmlLoader extends XmlLoaderWithNavaids<EntryExitPoint> {
  private final Navaid airportMainNavaid;
  private final IReadOnlyList<Border> borders;

  EntryExitPointXmlLoader(NavaidList navaids, Navaid airportMainNavaid, IReadOnlyList<Border> borders) {
    super(navaids);
    this.airportMainNavaid = airportMainNavaid;
    this.borders = borders;
  }

  @Override
  public EntryExitPoint load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String navaidName = XmlLoaderUtils.loadString("name");
    Navaid navaid = navaids.get(navaidName);
    EntryExitPoint.Type type = XmlLoaderUtils.loadEnum("type", EntryExitPoint.Type.class);

    Integer maxMrvaAltitude = XmlLoaderUtils.loadInteger("maxMrvaAltitude", null);
    maxMrvaAltitude = evaluateMaxMrvaAltitude(maxMrvaAltitude, navaid);
    int radialFromAirport = (int) Coordinates.getBearing(this.airportMainNavaid.getCoordinate(), navaid.getCoordinate());

    EntryExitPoint ret = EntryExitPoint.create(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private int evaluateMaxMrvaAltitude(Integer customMaxMrvaAltitude, Navaid navaid) {
    int ret;
    IList<Border> mrvas = borders.where(q -> q.getType() == Border.eType.mrva);
    Tuple<Coordinate, Coordinate> line =
        new Tuple<>(
            navaid.getCoordinate(),
            this.airportMainNavaid.getCoordinate()
        );

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (mrva.hasIntersectionWithLine(line))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }

    if (customMaxMrvaAltitude == null)
      ret = maxMrvaAlt;
    else
      ret = Math.min(customMaxMrvaAltitude, maxMrvaAlt);
    return ret;
  }
}
