package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.GoAroundCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class GoAroundParser extends TextSpeechParser<GoAroundCommand> {

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
