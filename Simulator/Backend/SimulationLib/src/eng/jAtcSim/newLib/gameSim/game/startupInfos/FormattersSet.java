package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;

public class FormattersSet<T> {
  public final IAtcFormatter<T> atcFormatter;
  public final IPlaneFormatter<T> planeFormatter;
  public final ISystemFormatter<T> systemFormatter;

  public FormattersSet(IPlaneFormatter<T> planeFormatter, IAtcFormatter<T> atcFormatter, ISystemFormatter<T> systemFormatter) {
    this.planeFormatter = planeFormatter;
    this.atcFormatter = atcFormatter;
    this.systemFormatter = systemFormatter;
  }
}
