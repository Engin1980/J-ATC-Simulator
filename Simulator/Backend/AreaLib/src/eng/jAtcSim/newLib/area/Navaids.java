package eng.jAtcSim.newLib.area;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.RegexGrouper;

import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class Navaids {

  private static final String pbdRegex = "^([A-Z]+)/(\\d{3})/(\\d+(\\.\\d+)?)$";

  public static class PBDInfo{
    public final String name;
    public final int bearing;
    public final double distance;

    public PBDInfo(String name, int heading, double distance) {
      this.name = name;
      this.bearing = heading;
      this.distance = distance;
    }
  }

  public static boolean isPdbName(String name){
    return Pattern.compile(pbdRegex).
  }

  public static Navaid tryGeneratePbdNavaid(String name, NavaidList navaids, int declination){

  }

  private static PBDInfo tryExpandPBD(String pdb){

    RegexGrouper rg = RegexGrouper.apply(pdb, regex);
    PBDInfo ret = new PBDInfo(
        rg.getString(1),
        rg.getInt(2),
        rg.getDouble(3)
    );
    return ret;
  }
}
