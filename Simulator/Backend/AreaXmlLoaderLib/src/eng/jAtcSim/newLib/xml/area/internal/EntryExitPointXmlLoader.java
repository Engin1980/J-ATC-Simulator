package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.Tuple;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

class EntryExitPointXmlLoader extends XmlLoader<EntryExitPoint> {

  EntryExitPointXmlLoader(Context context) {
    super(context);
  }

  @Override
  public EntryExitPoint load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String navaidName = XmlLoaderUtils.loadString("name");
    Navaid navaid = context.area.navaids.get(navaidName);
    EntryExitPoint.Type type = XmlLoaderUtils.loadEnum("type", EntryExitPoint.Type.class);

    Integer maxMrvaAltitude = XmlLoaderUtils.loadInteger("maxMrvaAltitude", null);
    maxMrvaAltitude = evaluateMaxMrvaAltitude(maxMrvaAltitude, navaid);

    EntryExitPoint ret = EntryExitPoint.create(navaid, type, maxMrvaAltitude);
    return ret;
  }

  private int evaluateMaxMrvaAltitude(Integer customMaxMrvaAltitude, Navaid navaid) {
    int ret;
    IList<Border> mrvas = context.area.borders.where(q -> q.getType() == Border.eType.mrva);
    Tuple<Coordinate, Coordinate> line =
        new Tuple<>(
            navaid.getCoordinate(),
            context.airport.mainNavaid.getCoordinate()
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
