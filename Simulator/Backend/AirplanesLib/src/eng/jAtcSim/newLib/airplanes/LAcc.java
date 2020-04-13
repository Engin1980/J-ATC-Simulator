package eng.jAtcSim.newLib.airplanes;

import eng.jAtcSim.newLib.area.*;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.shared.InstanceProviderDictionary;
import eng.jAtcSim.newLib.speeches.atc2airplane.ToNavaidCommand;
import eng.jAtcSim.newLib.weather.Weather;

public class LAcc {

  public static class Smart{
    public static Navaid getNavaid(ToNavaidCommand command) {
      return getArea().getNavaids().get(command.getNavaidName());
    }
  }

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

  public static Weather getWeather(){
    return InstanceProviderDictionary.getInstance(Weather.class);
  }

  public static NavaidList getNavaids(){
    return getArea().getNavaids();
  }
}
