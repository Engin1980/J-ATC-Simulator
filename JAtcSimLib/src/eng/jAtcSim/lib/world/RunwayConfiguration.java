package eng.jAtcSim.lib.world;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public class RunwayConfiguration {
  private int windFrom;
  private int windTo;
  private int windSpeedFrom;
  private int windSpeedTo;
  private IList<String> arrivals;
  private IList<String> departures;

  public RunwayConfiguration(int windFrom, int windTo, int windSpeedFrom, int windSpeedTo, IList<String> arrivals, IList<String> departures) {
    this.windFrom = windFrom;
    this.windTo = windTo;
    this.windSpeedFrom = windSpeedFrom;
    this.windSpeedTo = windSpeedTo;
    this.arrivals = arrivals;
    this.departures = departures;
  }

  public int getWindFrom() {
    return windFrom;
  }

  public int getWindTo() {
    return windTo;
  }

  public int getWindSpeedFrom() {
    return windSpeedFrom;
  }

  public int getWindSpeedTo() {
    return windSpeedTo;
  }

  public IReadOnlyList<String> getArrivals() {
    return arrivals;
  }

  public IReadOnlyList<String> getDepartures() {
    return departures;
  }
}
