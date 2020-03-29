package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class SharedInstanceProvider {
  public static String getAirportIcao() {
    return InstanceProviderDictionary.getInstance(String.class, "airportIcao");
  }

  public static ApplicationLog getAppLog() {
    return InstanceProviderDictionary.getInstance(ApplicationLog.class);
  }

  public static EDayTimeRun getNow() {
    return InstanceProviderDictionary.getInstance(EDayTimeRun.class);
  }

  public static ERandom getRnd() {
    return InstanceProviderDictionary.getInstance(ERandom.class);
  }

  public static Settings getSettings() {
    return InstanceProviderDictionary.getInstance(Settings.class);
  }

  public static void setAirportIcao(String icao) {
    InstanceProviderDictionary.setInstance(String.class, "airportIcao", icao);
  }

  public static void setAppLog(ApplicationLog log) {
    InstanceProviderDictionary.setInstance(ApplicationLog.class, log);
  }

  public static void setNow(EDayTimeRun eDayTimeRun) {
    InstanceProviderDictionary.setInstance(EDayTimeRun.class, eDayTimeRun);
  }

  public static void setSimLog(SimulationLog log){
    InstanceProviderDictionary.setInstance(SimulationLog.class,log);
  }

  public static SimulationLog getSimLog(){
    return InstanceProviderDictionary.getInstance(SimulationLog.class);
  }

  public static void setRnd(ERandom rnd) {
    InstanceProviderDictionary.setInstance(ERandom.class, rnd);
  }
}
