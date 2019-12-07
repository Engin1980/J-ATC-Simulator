package eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.afters.AfterDistanceCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.textProcessing.parsing.RegexGrouper;
import eng.jAtcSim.newLib.world.Navaid;

public class AfterDistanceParser extends SpeechParser<AfterDistanceCommand> {
  private static final String BLOCK_PATTERN= "(\\S+)/(\\d+(\\.\\d+)?)";
  private static final String[][] patterns = {
      {"AD", BLOCK_PATTERN}
  };

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After distance from fix", "AD {fixName}/{distance",
        "After distance (in nm) from specified fix. Distance can be fractional.",
        "AD KENOK/10.8");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterDistanceCommand parse(IList<String> blocks) {
    RegexGrouper rg = RegexGrouper.apply(blocks.get(1), BLOCK_PATTERN);
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    double d = rg.getDouble(2);
    AfterDistanceCommand ret = new AfterDistanceCommand(n, d);
    return ret;
  }
}
