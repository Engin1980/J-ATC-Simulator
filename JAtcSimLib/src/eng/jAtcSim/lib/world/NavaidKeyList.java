package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;

public class NavaidKeyList extends KeyList<Navaid, String> {

  public Navaid getOrGenerate(String name) {
    Navaid ret = super.tryGet(name);
    if (ret == null && name.contains("/")) {
      try {
        PBD pdb = PBD.decode(name);
        Coordinate coord = Coordinates.getCoordinate(pdb.point.getCoordinate(), pdb.bearing, pdb.distance);
        Navaid n = new Navaid(name, Navaid.eType.auxiliary, coord);
        super.add(n);
        ret = n;
      } catch (Exception ex){
        throw new ERuntimeException("Failed to get / decode navaid with name " + name);
      }
    }
    return ret;
  }
}

class PBD {
  private static String pattern = "([A-Z]+)\\/(\\d{1,3})\\/(\\d+(\\.\\d)?)";
  public Navaid point;
  public int bearing;
  public double distance;

  public static PBD decode(String text) {
    RegexGrouper rg = RegexGrouper.apply(text, pattern);

    PBD ret = new PBD();
    String navaidName = rg.getString(1);
    ret.point = Acc.area().getNavaids().get(navaidName);
    ret.bearing = rg.getInt(2);
    ret.bearing = (int) Headings.to(ret.bearing);
    ret.distance = rg.getDouble(3);

    return ret;
  }
}
