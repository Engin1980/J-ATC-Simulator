package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class SharedFactory {
  public static String getAirportIcao() {
    return Factory.getInstance(String.class, "airportIcao");
  }

  public static ApplicationLog getAppLog() {
    return Factory.getInstance(ApplicationLog.class);
  }

  public static EDayTimeRun getNow() {
    return Factory.getInstance(EDayTimeRun.class);
  }

  public static ERandom getRnd() {
    return Factory.getInstance(ERandom.class);
  }

  public static Settings getSettings() {
    return Factory.getInstance(Settings.class);
  }

  public static void setAirportIcao(String icao) {
    Factory.setInstance(String.class, "airportIcao", icao);
  }

  public static void setAppLog(ApplicationLog log) {
    Factory.setInstance(ApplicationLog.class, log);
  }

  public static void setNow(EDayTimeRun eDayTimeRun) {
    Factory.setInstance(EDayTimeRun.class, eDayTimeRun);
  }

  public static void setSimLog(SimulationLog log){
    Factory.setInstance(SimulationLog.class,log);
  }

  public static SimulationLog getSimLog(){
    return Factory.getInstance(SimulationLog.class);
  }

  public static void setRnd(ERandom rnd) {
    Factory.setInstance(ERandom.class, rnd);
  }
}
