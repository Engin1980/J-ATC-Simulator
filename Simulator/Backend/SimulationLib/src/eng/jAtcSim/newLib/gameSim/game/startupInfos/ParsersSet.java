package eng.jAtcSim.newLib.gameSim.game.startupInfos;

import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.IPlaneParsingProvider;
import eng.jAtcSim.newLib.textProcessing.parsing.ISystemParsingProvider;

public class ParsersSet {
  public final IAtcParsingProvider atcParser;
  public final IPlaneParsingProvider planeParser;
  public final ISystemParsingProvider systemParser;

  public ParsersSet(IPlaneParsingProvider planeParser, IAtcParsingProvider atcParser, ISystemParsingProvider systemParser) {
    this.planeParser = planeParser;
    this.atcParser = atcParser;
    this.systemParser = systemParser;
  }
}
