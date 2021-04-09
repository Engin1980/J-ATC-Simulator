package eng.jAtcSim.newLib.shared.logging;

import eng.jAtcSim.newLib.shared.contextLocal.Context;

public class SimulationLog {

  public SimulationLog() {
//    this.journal = new Journal(
//        "Simulation log",
//        false,
//        new AutoNewLineLogWriter(
//            new RealTimePipeLogWriter(
//                new SimTimePipeLogWriter(
//                    new ConsoleWriter()))));
  }

  public void write(LogItemType type, String message) {
    Context.getApp().getAppLog().write(type, "simulation",
            String.format("%s :: %s",
                    Context.getShared().getNow().toString(),
                    message));
  }

  public void write(LogItemType type, String format, Object... params) {
    String tmp = String.format(format, params);
    this.write(type, tmp);
  }

  //TODEL
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
