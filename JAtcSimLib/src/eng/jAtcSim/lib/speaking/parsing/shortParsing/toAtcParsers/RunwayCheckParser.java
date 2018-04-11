package eng.jAtcSim.lib.speaking.parsing.shortParsing.toAtcParsers;

import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.RunwayCheck;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;
import eng.jAtcSim.lib.world.Runway;
import eng.jAtcSim.lib.world.RunwayThreshold;

public class RunwayCheckParser extends SpeechParser<RunwayCheck> {
  private static final String[] prefixes = new String[]{"RWYCHECK"};
  private static final String pattern = "RWYCHECK( ((TIME)|(DO)))( (.+))?";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public RunwayCheck parse(RegexGrouper line) {
    String action = line.getString(2);
    String rwyName = line.getString(6);

    Runway rwy;
    if (rwyName == null)
      rwy = null;
    else{
      RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(rwyName);
      if (rt == null){
        throw new EApplicationException("Unable to find threshold name " + rwyName + ".");
      } else {
        rwy = rt.getParent();
      }
    }

    RunwayCheck.eType type;
    switch (action){
      case "DO":
        type = RunwayCheck.eType.doCheck;
        break;
      case "TIME":
        type = RunwayCheck.eType.askForTime;
        break;
      default:
        throw new UnsupportedOperationException();
    }

    RunwayCheck ret = new RunwayCheck(rwy, type);
    return ret;
  }
}
