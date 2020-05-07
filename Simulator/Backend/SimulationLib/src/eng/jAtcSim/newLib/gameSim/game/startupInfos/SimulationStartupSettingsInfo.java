package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class SimulationStartupSettingsInfo {
  public ETimeStamp startTime;
  public int secondLengthInMs;
  /**
   * Snapshot length in minutes for stats aggregation
   */
  public int statsSnapshotDistanceInMinutes;
  public String logPath;
}
