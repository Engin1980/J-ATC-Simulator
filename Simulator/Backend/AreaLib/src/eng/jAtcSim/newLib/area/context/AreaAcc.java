package eng.jAtcSim.newLib.area.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.RunwayConfiguration;

public class AreaAcc {
  private static Producer<Area> areaProducer = null;
  private static Producer<Airport> airportProducer = null;
  private static Producer<RunwayConfiguration> currentRunwayConfigurationProducer = null;
  private static Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer = null;
  private static Producer<NavaidList> navaidListProducer = null;

  public static Airport getAirport() {
    return airportProducer.produce();
  }

  public static Area getArea() {
    return areaProducer.produce();
  }

  public static RunwayConfiguration getCurrentRunwayConfiguration() {
    return currentRunwayConfigurationProducer.produce();
  }

  public static RunwayConfiguration getScheduledRunwayConfiguration() {
    return scheduledRunwayConfigurationProducer.produce();
  }

  public static void setAirportProducer(Producer<Airport> airportProducer) {
    AreaAcc.airportProducer = airportProducer;
  }

  public static void setAreaProducer(Producer<Area> areaProducer) {
    AreaAcc.areaProducer = areaProducer;
    AreaAcc.navaidListProducer = () -> areaProducer.produce().getNavaids();
  }

  public static void setCurrentRunwayConfigurationProducer(Producer<RunwayConfiguration> currentRunwayConfigurationProducer) {
    AreaAcc.currentRunwayConfigurationProducer = currentRunwayConfigurationProducer;
  }

  public static Producer<NavaidList> getNavaidListProducer() {
    return navaidListProducer;
  }

  public static void setNavaidsProducer(Producer<NavaidList> navaidListProducer) {
    AreaAcc.navaidListProducer = navaidListProducer;
  }

  public static void setScheduledRunwayConfigurationProducer(Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer) {
    AreaAcc.scheduledRunwayConfigurationProducer = scheduledRunwayConfigurationProducer;
  }

  public static NavaidList getNavaids(){
    return navaidListProducer.produce();
  }
}
