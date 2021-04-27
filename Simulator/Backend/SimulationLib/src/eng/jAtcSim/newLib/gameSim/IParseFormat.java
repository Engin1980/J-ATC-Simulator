package eng.jAtcSim.newLib.gameSim;

import eng.jAtcSim.newLib.textProcessing.formatting.IAtcFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.formatting.ISystemFormatter;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParsingProvider;

public interface IParseFormat {
  IAtcFormatter<?> getAtcFormatter();

  IAtcParsingProvider getAtcParser();

  IPlaneFormatter<?> getPlaneFormatter();

  IPlaneParsingProvider getPlaneParser();

  ISystemFormatter<?> getSystemFormatter();

  ISystemParsingProvider getSystemParser();
}
