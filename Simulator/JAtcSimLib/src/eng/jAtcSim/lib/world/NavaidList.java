package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.xmlSerialization.annotations.XmlItemElement;
import eng.jAtcSim.lib.Acc;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.textProcessing.parsing.RegexGrouper;

@XmlItemElement(elementName="navaid", type=Navaid.class)
public class NavaidList extends EList<Navaid> {

  public Navaid getOrGenerate(String name) {
    Navaid ret = this.tryGet(name);
    if (ret == null) {
      if (name.contains("/")) {
        try {
          PBD pdb = PBD.decode(name);
          Coordinate coord = Coordinates.getCoordinate(pdb.point.getCoordinate(), pdb.bearing, pdb.distance);
          Navaid n = new Navaid(name, Navaid.eType.auxiliary, coord);
          super.add(n);
          ret = n;
        } catch (Exception ex) {
          throw new EApplicationException("Failed to getContent / decode navaid with name " + name, ex);
        }
      } else if (name.contains(":")){
        try{
          String[] pts = name.split(":");
          Airport aip = Acc.area().getAirports().tryGetFirst(q->q.getIcao().equals(pts[0]));
          if (aip == null)
            throw new EApplicationException("Airport with code " + pts[0] + " not found.");
          RunwayThreshold th = aip.tryGetRunwayThreshold(pts[1]);
          if (th == null)
            throw new EApplicationException("Airport with code " + pts[0] + " has no threshold " + pts[1]+".");
          Navaid n = new Navaid(name, Navaid.eType.auxiliary, th.getCoordinate());
          super.add(n);
          ret = n;
        } catch (Exception ex){
          throw new EApplicationException("Failed to getContent / decode navaid with name " + name, ex);
        }
      }
    }
    return ret;
  }

  public Navaid get(String name) {
    Navaid ret;
    try {
      ret = this.getFirst(q -> q.getName().equals(name));
    }catch (Exception ex){
      throw new EApplicationException("Unable to find element " + name + ".");
    }
    return ret;
  }

  public Navaid tryGet(String name) {
    Navaid ret = this.tryGetFirst(q -> q.getName().equals(name));
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
