package eng.jAtcSim.newLib.airplanes.other;

import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.GAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public abstract class AirplaneRecorder {
  protected final Callsign callsign;

  public AirplaneRecorder(Callsign callsign) {
    this.callsign = callsign;
  }

  protected void logErrorToAppLog(Exception ex){
    StringBuilder sb = new StringBuilder();
    sb.append(this.callsign.toString())
        .append(":: ");
    if (ex == null)
      sb.append("Exception object is null.");
    else
      sb.append(ex.getMessage()).append(":: " ).append(ExceptionUtils.toFullString(ex));
    GAcc.getAppLog().writeLine(ApplicationLog.eType.critical, sb.toString());
  }
}
