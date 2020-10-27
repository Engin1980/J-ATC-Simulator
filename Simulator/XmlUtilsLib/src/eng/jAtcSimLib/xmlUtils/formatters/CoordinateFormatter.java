package eng.jAtcSimLib.xmlUtils.formatters;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSimLib.xmlUtils.Formatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class CoordinateFormatter implements Formatter<Coordinate> {
  @Override
  public String invoke(Coordinate coordinate) {
    return sf("%s;%s", coordinate.getLatitude().toDecimalString(true), coordinate.getLongitude().toDecimalString(true));
  }
}
