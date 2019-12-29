package eng.jAtcSim.newLib.shared.logging;

public abstract class SimulationLog {
  public abstract void sendTextMessageForUser(String s);
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
