package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

public class GoAroundParser extends SpeechParser<GoAroundCommand> {

  private static final String[][] patterns = {{"GA"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Go around",
        "GA",
        "Orders airplane to go around. Used when airplane is cleared to approach\nand refuses other commands.",
        "GA");
    return ret;
  }
  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public GoAroundCommand parse(IList<String> blocks) {
    return new GoAroundCommand();
  }
}
