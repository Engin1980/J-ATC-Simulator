package eng.jAtcSim.newLib.atcs.internal.tower;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.RunwayConfiguration;
import exml.IXPersistable;
import exml.saving.XSaveContext;

public class RunwaysInUseInfo implements IXPersistable {
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

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    System.out.println("bubla");
  }
}
