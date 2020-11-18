package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.jAtcSim.newLib.area.RunwayConfiguration;

public class RunwaysInUseInfo {
  // TODO make private
  public SchedulerForAdvice scheduler;
  public RunwayConfiguration current;
  public RunwayConfiguration scheduled;

  public RunwayConfiguration getCurrent() {
    return current;
  }

  public RunwayConfiguration getScheduled() {
    return scheduled;
  }
}
