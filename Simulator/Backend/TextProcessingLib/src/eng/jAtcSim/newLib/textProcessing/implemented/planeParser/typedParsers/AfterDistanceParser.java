package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.RegexGrouper;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class AfterDistanceParser extends SpeechParser<AfterDistanceCommand> {
  private static final String BLOCK_PATTERN = "(\\S+)/(\\d+(\\.\\d+)?)";
  private static final String[][] patterns = {
      {"AD", BLOCK_PATTERN}
  };

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After distance from fix", "AD {fixName}/{distance",
        "After (= at) distance (in nm) from specified fix. Distance can be fractional.",
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
    double d = rg.getDouble(2);
    AfterDistanceCommand ret = AfterDistanceCommand.create(ns, d, AboveBelowExactly.exactly);
    return ret;
  }
}
