package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.RegexGrouper;

public class NavaidList extends EList<Navaid> {

  private final IMap<Coordinate, Double> declinations = new EMap<>();

  public Navaid addRunwayThresholdPoint(String airportIcao, String thresholdName, Coordinate thresholdCoordinate) {
    EAssert.Argument.matchPattern(airportIcao, "^[A-Z]{4}$");
    EAssert.Argument.matchPattern(thresholdName, "^\\d{2}[RLC]?$");
    Navaid navaid = Navaid.create(airportIcao + ":" + thresholdName, Navaid.eType.auxiliary, thresholdCoordinate);
    super.add(navaid);
    return navaid;
  }

  public Navaid get(String name) {
    Navaid ret;
    try {
      ret = this.getFirst(q -> q.getName().equals(name));
    } catch (Exception ex) {
      throw new EApplicationException("Unable to find element " + name + ".");
    }
    return ret;
  }

  public Navaid getOrGenerate(String name, Coordinate coordinate) {
    Navaid ret = this.tryGet(name);
    if (ret == null) {
      ret = Navaid.create(name, Navaid.eType.auxiliary, coordinate);
      this.add(ret);
    }
    return ret;
  }

  public Navaid getWithPBD(String name) {
    Navaid ret = this.tryGet(name);
    if (ret == null) {
      PBD pdb = PBD.decode(name, this);
      double declination = getNearestDeclination(pdb.point.getCoordinate());
      pdb.bearing = (int) Headings.add(pdb.bearing, declination);
      Coordinate coordinate = Coordinates.getCoordinate(pdb.point.getCoordinate(), pdb.bearing, pdb.distance);
      Navaid n = Navaid.create(name, Navaid.eType.auxiliary, coordinate);
      super.add(n);
      ret = n;

//      if (name.contains("/")) {
//        // POINT/BEARING/DISTANCE
//        try {
//          PBD pdb = PBD.decode(name, relativeAirport.getParent().getNavaids());
//          pdb.bearing += getNearestDeclination()
//          Coordinate coord = Coordinates.getCoordinate(pdb.point.getCoordinate(), pdb.bearing, pdb.distance);
//          Navaid n = Navaid.create(name, Navaid.eType.auxiliary, coord);
//          super.add(n);
//          ret = n;
//        } catch (Exception ex) {
//          throw new EApplicationException("Failed to getContent / decode navaid with name " + name, ex);
//        }
//      } else if (name.contains(":")) {
//        // ICAO:RWY
//        try {
//          String[] pts = name.split(":");
//          Airport aip = relativeAirport.getParent().getAirports().tryGetFirst(q -> q.getIcao().equals(pts[0]));
//          if (aip == null)
//            throw new EApplicationException("Airport with code " + pts[0] + " not found.");
//          ActiveRunwayThreshold th = aip.tryGetRunwayThreshold(pts[1]);
//          if (th == null)
//            throw new EApplicationException("Airport with code " + pts[0] + " has no threshold " + pts[1] + ".");
//          Navaid n = Navaid.create(name, Navaid.eType.auxiliary, th.getCoordinate());
//          super.add(n);
//          ret = n;
//        } catch (Exception ex) {
//          throw new EApplicationException("Failed to getContent / decode navaid with name " + name, ex);
//        }
//      }
    }
    return ret;
  }

  public void registerDeclination(Coordinate coordinate, double declination) {
    declinations.set(coordinate, declination);
  }

  public Navaid tryGet(String name) {
    Navaid ret = this.tryGetFirst(q -> q.getName().equals(name)).orElse(null);
    return ret;
  }

  private double getNearestDeclination(Coordinate coordinate) {
    if (declinations.isEmpty())
      throw new ERuntimeException("Unable to get nearest declination. Any declination is registered.");
    Coordinate nearestCoordinate = declinations.getKeys().getMinimal(q ->
        Coordinates.getDistanceInNM(coordinate, q));
    double ret = declinations.get(nearestCoordinate);
    return ret;
  }
}

class PBD {
  private static String pattern = "([A-Z]+)\\/(\\d{1,3})\\/(\\d+(\\.\\d)?)";

  public static PBD decode(String navaidName, NavaidList navaids) {
    RegexGrouper rg = RegexGrouper.apply(navaidName, pattern);

    PBD ret = new PBD();
    String baseNavaidName = rg.getString(1);
    ret.point = navaids.get(baseNavaidName);
    ret.bearing = rg.getInt(2);
    ret.bearing = (int) Headings.to(ret.bearing);
    ret.distance = rg.getDouble(3);

    return ret;
  }

  public Navaid point;
  public int bearing;
  public double distance;
}
