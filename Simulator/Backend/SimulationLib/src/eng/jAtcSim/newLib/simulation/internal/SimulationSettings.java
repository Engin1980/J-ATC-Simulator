package eng.jAtcSim.newLib.simulation.internal;

public class SimulationSettings {
  private final double emergencyPerDayProbability;
  private int simulationSecondLengthInMs;
  private final int statsSnapshotDistanceInMinutes;

  public SimulationSettings(double emergencyPerDayProbability, int simulationSecondLengthInMs, int statsSnapshotDistanceInMinutes) {
    this.emergencyPerDayProbability = emergencyPerDayProbability;
    this.simulationSecondLengthInMs = simulationSecondLengthInMs;
    this.statsSnapshotDistanceInMinutes = statsSnapshotDistanceInMinutes;
  }

  public double getEmergencyPerDayProbability() {
    return emergencyPerDayProbability;
  }

  public int getSimulationSecondLengthInMs() {
    return simulationSecondLengthInMs;
  }

  public int getStatsSnapshotDistanceInMinutes() {
    return statsSnapshotDistanceInMinutes;
  }
}
