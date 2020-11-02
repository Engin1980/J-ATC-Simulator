package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class TrafficSettings {
  public double emergencyPerDayProbability;
  public double trafficDelayStepProbability;
  public int trafficDelayStep;
  public int maxTrafficPlanes;
  public double trafficDensityPercentage;
  public boolean useExtendedCallsigns;
}
