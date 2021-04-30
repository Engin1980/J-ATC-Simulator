package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.AtcId;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AnalysedPlanes {
  public final int arrivals;
  public final int departures;
  public final IMap<AtcId, Integer> appArrivals;
  public final IMap<AtcId, Integer> appDepartures;
  public final int mrvaErrors;
  public final int airproxErrors;
  public final int planesAtHoldingPoint;

  public AnalysedPlanes(int arrivals, int departures, IMap<AtcId, Integer> appArrivals, IMap<AtcId, Integer> appDepartures, int mrvaErrors, int airproxErrors, int planesAtHoldingPoint) {
    this.arrivals = arrivals;
    this.departures = departures;
    this.appArrivals = appArrivals;
    this.appDepartures = appDepartures;
    this.mrvaErrors = mrvaErrors;
    this.airproxErrors = airproxErrors;
    this.planesAtHoldingPoint = planesAtHoldingPoint;
  }
}
