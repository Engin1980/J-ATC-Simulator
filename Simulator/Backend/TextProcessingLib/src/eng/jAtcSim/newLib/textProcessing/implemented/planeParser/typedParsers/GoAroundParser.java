package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.GoAroundCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class GoAroundParser extends TextSpeechParser<GoAroundCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "GA");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Go around",
            "GA",
            "Orders airplane to go around. Used when airplane is cleared to approach\nand refuses other commands.",
            "GA");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public GoAroundCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    EAssert.Argument.isTrue(patternIndex == 0);
    return new GoAroundCommand();
  }
}
