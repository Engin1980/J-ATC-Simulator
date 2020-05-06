package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.textProcessing.base.Formatter;
import eng.jAtcSim.newLib.textProcessing.base.Parser;

public class SimulationSettings {
  private final double emergencyPerDayProbability;
  private int simulationSecondLengthInMs;
  private final int statsSnapshotDistanceInMinutes;
  private final Parser parser;
  private final Formatter formatter;

  public Parser getParser() {
    return parser;
  }

  public Formatter getFormatter() {
    return formatter;
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
