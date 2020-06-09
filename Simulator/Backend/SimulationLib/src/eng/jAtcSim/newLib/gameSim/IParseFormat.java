package eng.jAtcSim.newLib.gameSim;

import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public interface IParseFormat {
  IAtcFormatter<?> getAtcFormatter();

  IAtcParser getAtcParser();

  IPlaneFormatter<?> getPlaneFormatter();

  IPlaneParser getPlaneParser();

  ISystemFormatter<?> getSystemFormatter();

  ISystemParser getSystemParser();
}
