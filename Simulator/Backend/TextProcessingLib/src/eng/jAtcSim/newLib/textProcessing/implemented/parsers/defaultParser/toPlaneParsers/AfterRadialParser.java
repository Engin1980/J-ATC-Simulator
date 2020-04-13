package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.RegexGrouper;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterRadialCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class AfterRadialParser extends SpeechParser<AfterRadialCommand> {
  private static final String BLOCK_PATTERN = "(\\S+)/(\\d{1,3})";
  private static final String[][] patterns = {{"AR", BLOCK_PATTERN}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After radial",
        "AR {fixName}/{radial}",
        "When passing radial to fix",
        "AR KENOK/030\nAR KENOK/30");
    return ret;
  }


  @Override
  public String [][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterRadialCommand parse(IList<String> blocks) {

    RegexGrouper rg = RegexGrouper.apply(blocks.get(1), BLOCK_PATTERN);
    String ns = rg.getString(1);
    int rad = rg.getInt(2);
    AfterRadialCommand ret = AfterRadialCommand.create(ns, rad);
    return ret;
  }
}
