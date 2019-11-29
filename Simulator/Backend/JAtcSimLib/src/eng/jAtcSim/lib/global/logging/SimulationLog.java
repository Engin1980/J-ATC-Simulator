package eng.jAtcSim.lib.global.logging;

import eng.jAtcSim.lib.Acc;

import java.io.OutputStream;

public abstract class SimulationLog extends Log {

  public boolean addSimulationTime;
  public String simulationTimeSeparator = " ";

  public boolean isAddSimulationTime() {
    return addSimulationTime;
  }

  public void setAddSimulationTime(boolean addSimulationTime) {
    this.addSimulationTime = addSimulationTime;
  }

  public String getSimulationTimeSeparator() {
    return simulationTimeSeparator;
  }

  public void setSimulationTimeSeparator(String simulationTimeSeparator) {
    this.simulationTimeSeparator = simulationTimeSeparator;
  }

  public SimulationLog(String name, AbstractSaver ... savers) {
    super(name, true, savers);
  }

  public String getSimulationTimeString() {
    return Acc.sim().getNow().toString();
  }

  @Override
  protected void writeLine(String format, Object... params) {
    if (addSimulationTime){
      format = getSimulationTimeString() + simulationTimeSeparator + format;
    }
    super.writeLine(format, params);
  }
}
