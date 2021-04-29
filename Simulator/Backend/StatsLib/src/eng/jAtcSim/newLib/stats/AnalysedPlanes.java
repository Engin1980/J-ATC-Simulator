package eng.jAtcSim.newLib.stats;

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

  public AnalysedPlanes(int arrivals, int departures, int appArrivals, int appDepartures, int mrvaErrors, int airproxErrors, int planesAtHoldingPoint) {
    this.arrivals = arrivals;
    this.departures = departures;
    this.appArrivals = appArrivals;
    this.appDepartures = appDepartures;
    this.mrvaErrors = mrvaErrors;
    this.airproxErrors = airproxErrors;
    this.planesAtHoldingPoint = planesAtHoldingPoint;
  }
}
