package eng.jAtcSim.lib.stats.read.specific;

public class PlanesViewBlock<T> {
  private T arrivals;
  private T departures;
  private T together;

  public PlanesViewBlock(T arrivals, T departures, T together) {
    this.arrivals = arrivals;
    this.departures = departures;
    this.together = together;
  }

  public T getArrivals() {
    return arrivals;
  }

  public T getDepartures() {
    return departures;
  }

  public T getTogether() {
    return together;
  }
}
