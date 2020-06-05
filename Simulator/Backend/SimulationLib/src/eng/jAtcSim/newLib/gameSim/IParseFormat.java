package eng.jAtcSim.newLib.gameSim;

import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;

public interface IParseFormat {
  IAtcFormatter<?> getAtcFormatter();

  IPlaneFormatter<?> getPlaneFormatter();

  ISystemFormatter<?> getSystemFormatter();
}
