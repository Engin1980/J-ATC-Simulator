package eng.jAtcSim.newLib.area;

import eng.eSystem.Producer;

public class AreaAcc {
  private static Producer<Area> areaProducer = null;
  private static Producer<Airport> airportProducer = null;
  private static Producer<RunwayConfiguration> currentRunwayConfigurationProducer = null;
  private static Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer = null;

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
  }

  public static void setCurrentRunwayConfigurationProducer(Producer<RunwayConfiguration> currentRunwayConfigurationProducer) {
    AreaAcc.currentRunwayConfigurationProducer = currentRunwayConfigurationProducer;
  }

  public static void setScheduledRunwayConfigurationProducer(Producer<RunwayConfiguration> scheduledRunwayConfigurationProducer) {
    AreaAcc.scheduledRunwayConfigurationProducer = scheduledRunwayConfigurationProducer;
  }

  public static NavaidList getNavaids(){
    return getArea().getNavaids();
  }
}
