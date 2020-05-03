package eng.jAtcSim.newLib.stats;

public interface IOverallStatsWriter {
  void registerArrival();

  void registerDeparture(int holdingPointSecondsWaiting);
}
