package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;

public class AirplaneInstanceProvider {
  public static Area getArea() {
    return InstanceProviderDictionary.getInstance(Area.class, "area");
  }

  public static void setArea(Area area) {
    InstanceProviderDictionary.setInstance(Area.class, "area", area);
  }

  public static Airport getAirport() {
    return InstanceProviderDictionary.getInstance(Airport.class, "airport");
  }

  public static void setAirport(Airport airport) {
    InstanceProviderDictionary.setInstance(Airport.class, "airport", airport);
  }
}
