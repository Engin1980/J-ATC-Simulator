package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;

public class LocalInstanceProvider {
  public static Area getArea() {
    return InstanceProviderDictionary.getInstance(Area.class, "area");
  }

  public static Messenger getMessenger() {
    return InstanceProviderDictionary.getInstance(Messenger.class);
  }

  public static void setMessenger(Messenger messenger) {
    InstanceProviderDictionary.setInstance(Messenger.class, messenger);
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

  public static RunwayConfiguration getCurrentRunwayConfiguration(){
    return InstanceProviderDictionary.getInstance(RunwayConfiguration.class);
  }

  public static void setCurrentRunwayConfiguration(RunwayConfiguration value){
    InstanceProviderDictionary.setInstance(RunwayConfiguration.class, value);
  }
}
