package eng.jAtcSim.newLib.shared;

import eng.eSystem.ERandom;
import eng.eSystem.Producer;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;

public class SharedAcc {
  private static Producer<String> airportIcaoProducer;
  private static Producer<ApplicationLog> applicationLogProducer;
  private static Producer<EDayTimeRun> nowProducer;
  private static Producer<ERandom> randomProducer;
  private static Producer<Settings> settingsProducer;
  private static Producer<SimulationLog> simulationLogProducer;

  public static void setAirportIcaoProducer(Producer<String> airportIcaoProducer) {
    EAssert.Argument.isNotNull(airportIcaoProducer, "airportIcaoProducer");
    SharedAcc.airportIcaoProducer = airportIcaoProducer;
  }

  public static void setApplicationLogProducer(Producer<ApplicationLog> applicationLogProducer) {
    EAssert.Argument.isNotNull(applicationLogProducer, "applicationLogProducer");
    SharedAcc.applicationLogProducer = applicationLogProducer;
  }

  public static void setNowProducer(Producer<EDayTimeRun> nowProducer) {
    EAssert.Argument.isNotNull(nowProducer, "nowProducer");
    SharedAcc.nowProducer = nowProducer;
  }

  public static void setRandomProducer(Producer<ERandom> randomProducer) {
    EAssert.Argument.isNotNull(randomProducer, "randomProducer");
    SharedAcc.randomProducer = randomProducer;
  }

  public static void setSettingsProducer(Producer<Settings> settingsProducer) {
    EAssert.Argument.isNotNull(settingsProducer, "settingsProducer");
    SharedAcc.settingsProducer = settingsProducer;
  }

  public static void setSimulationLogProducer(Producer<SimulationLog> simulationLogProducer) {
    EAssert.Argument.isNotNull(simulationLogProducer, "simulationLogProducer");
    SharedAcc.simulationLogProducer = simulationLogProducer;
  }

  public static String getAirportIcao() {
    return airportIcaoProducer.produce();
  }

  public static ApplicationLog getAppLog() {
    return applicationLogProducer.produce();
  }

  public static EDayTimeRun getNow() {
    return nowProducer.produce();
  }

  public static ERandom getRnd() {
    return randomProducer.produce();
  }

  public static Settings getSettings() {
    return settingsProducer.produce();
  }

  public static SimulationLog getSimLog() {
    return simulationLogProducer.produce();
  }
}