package eng.jAtcSim.lib.coordinates;

import eng.eSystem.EStringBuilder;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.xmlSerialization.supports.IValueParser;

class CoordinateValueParser implements IValueParser<Coordinate> {

  @Override
  public eng.eSystem.geo.Coordinate parse(String s) {
    return Coordinate.parse(s);
  }

  @Override
  public String format(Coordinate coordinate) {
    EStringBuilder ret = new EStringBuilder();
    ret.append(
        Double.toString(coordinate.getLatitude().get()));
    ret.append(" ");
    ret.append(
        Double.toString(coordinate.getLongitude().get()));
    return ret.toString();
  }
}
