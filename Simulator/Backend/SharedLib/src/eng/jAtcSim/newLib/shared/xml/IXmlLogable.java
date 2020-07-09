package eng.jAtcSim.newLib.shared.xml;

import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public interface IXmlLogable {
  boolean VERBOSE_LOG = true;

  //TODO implement somewhere else than in interface
  default void log(int indent, String format, Object... params) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append("  ");
    }
    sb.append(String.format(format, params));
    if (VERBOSE_LOG) {
      SharedAcc.getAppLog().write(
          ApplicationLog.eType.info, sb.toString());
    }
  }
}
