package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ThenParser extends TextSpeechParser<ThenCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "T");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Then",
            "T",
            "Tells that all the following commands will be applied when the previous command is fulfilled.\n" +
                    "Previous commands can be only proceed-direct, change-altitude, change-speed or change-heading commands.",
            "T");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ThenCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    ThenCommand ret = ThenCommand.create();
    return ret;
  }

}
