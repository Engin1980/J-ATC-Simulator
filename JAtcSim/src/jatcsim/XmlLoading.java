package jatcsim;

import eng.eSystem.xmlSerialization.IValueParser;
import eng.eSystem.xmlSerialization.XmlListItemMapping;
import jatcsimdraw.global.Point;
import jatcsimlib.atcs.AtcTemplate;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.RadarRange;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.world.*;

public class XmlLoading {
  public static void lodaNewArea(String fileName, Area area) {
    eng.eSystem.xmlSerialization.Settings sett = new eng.eSystem.xmlSerialization.Settings();

    // ignores
    sett.getIgnoredFieldsRegex().add("^_.+");
    sett.getIgnoredFieldsRegex().add("^parent$");
    sett.getIgnoredFieldsRegex().add("^binded$");

    // mappings
    sett.getListItemMapping().add(
        new XmlListItemMapping("airports", Airport.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("runways", Runway.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("thresholds", RunwayThreshold.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("approaches", Approach.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("routes", Route.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("atcTemplates", AtcTemplate.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("holds", PublishedHold.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("vfrPoints", VfrPoint.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("navaids", Navaid.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("borders", Border.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("points", "point", BorderExactPoint.class));
    sett.getListItemMapping().add(
        new XmlListItemMapping("points", "arc", BorderArcPoint.class));

    // own parsers
    sett.getValueParsers().add(new CoordinateValueParser());
//    sett.getValueParsers().add(new RadarColorParser());
//    sett.getElementParsers().add(new RadarFontParser());

    eng.eSystem.xmlSerialization.XmlSerializer ser = new eng.eSystem.xmlSerialization.XmlSerializer(sett);

    try {
      ser.fillObject(fileName, area);
    } catch (Exception ex){
      StringBuilder sb = new StringBuilder();
      Throwable t = ex;
      while (t != null){
        sb.append(t.getMessage());
        sb.append(" # # # # # ");
        t = t.getCause();
      }
      throw new ERuntimeException(sb.toString(), ex);
    }
  }
}

class CoordinateValueParser implements IValueParser<Coordinate>{

  @Override
  public String getTypeName() {
    return Coordinate.class.getName();
  }

  @Override
  public Coordinate parse(String s) {
    return Coordinate.parse(s);
  }

  @Override
  public String format(Coordinate coordinate) {
    throw new UnsupportedOperationException();
  }
}
