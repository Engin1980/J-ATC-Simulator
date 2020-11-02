package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParser;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParser;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParser;

public class ParsersSet {
  public final IAtcParser atcParser;
  public final IPlaneParser planeParser;
  public final ISystemParser systemParser;

  public ParsersSet(IPlaneParser planeParser, IAtcParser atcParser, ISystemParser systemParser) {
    this.planeParser = planeParser;
    this.atcParser = atcParser;
    this.systemParser = systemParser;
  }
}
