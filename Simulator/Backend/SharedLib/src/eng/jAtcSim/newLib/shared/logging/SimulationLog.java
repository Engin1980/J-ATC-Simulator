package eng.jAtcSim.newLib.shared.logging;

import eng.jAtcSim.newLib.shared.logging.writers.AutoNewLineLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.ConsoleWriter;
import eng.jAtcSim.newLib.shared.logging.writers.RealTimePipeLogWriter;
import eng.jAtcSim.newLib.shared.logging.writers.SimTimePipeLogWriter;

public class SimulationLog {
//  public void sendTextMessageForUser(String s){
//
//  }

  private final Journal journal;

  public SimulationLog() {
    this.journal = new Journal(
        "Simulation log",
        false,
        new AutoNewLineLogWriter(
            new RealTimePipeLogWriter(
                new SimTimePipeLogWriter(
                    new ConsoleWriter()))));
  }

  //
//  public boolean addSimulationTime;
//  public String simulationTimeSeparator = " ";
//
//  public boolean isAddSimulationTime() {
//    return addSimulationTime;
//  }
//
//  public void setAddSimulationTime(boolean addSimulationTime) {
//    this.addSimulationTime = addSimulationTime;
//  }
//
//  public String getSimulationTimeSeparator() {
//    return simulationTimeSeparator;
//  }
//
//  public void setSimulationTimeSeparator(String simulationTimeSeparator) {
//    this.simulationTimeSeparator = simulationTimeSeparator;
//  }
//
//  public SimulationLog(String name, AbstractSaver ... savers) {
//    super(name, true, savers);
//  }
//
//  public String getSimulationTimeString() {
//    return Acc.sim().getNow().toString();
//  }
//
//  @Override
//  protected void writeLine(String format, Object... params) {
//    if (addSimulationTime){
//      format = getSimulationTimeString() + simulationTimeSeparator + format;
//    }
//    super.writeLine(format, params);
//  }
}
