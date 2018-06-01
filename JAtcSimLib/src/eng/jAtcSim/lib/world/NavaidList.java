package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;

public class NavaidList extends EList<Navaid> {

  public Navaid getOrGenerate(String name) {
    Navaid ret = this.tryGet(name);
    if (ret == null && name.contains("/")) {
      try {
        PBD pdb = PBD.decode(name);
        Coordinate coord = Coordinates.getCoordinate(pdb.point.getCoordinate(), pdb.bearing, pdb.distance);
        Navaid n = new Navaid(name, Navaid.eType.auxiliary, coord);
        super.add(n);
        ret = n;
      } catch (Exception ex){
        throw new EApplicationException("Failed to getContent / decode navaid with name " + name, ex);
      }
    }
    return ret;
  }

  public Navaid get(String name){
    Navaid ret = this.getFirst(q->q.getName().equals(name));
    return ret;
  }

  public Navaid tryGet(String name){
    Navaid ret = this.tryGetFirst(q->q.getName().equals(name));
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
