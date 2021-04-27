package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterSpeedCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterSpeedParser extends TextSpeechParser<AfterSpeedCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AS (\\d{1,3})");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After speed",
            "AR {speed}",
            "When reaching speed",
            "AS 210\nAS 90");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public AfterSpeedCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    int s = groups.getInt(1);
    AfterSpeedCommand ret = AfterSpeedCommand.create(s, AboveBelowExactly.exactly);
    return ret;
  }

}
