package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.DivertCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class DivertParser extends TextSpeechParser<DivertCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "DVT");

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "Divert",
            "DVT",
            "Orders airplane divert from your airport",
            "DVT");
    return ret;
  }

  @Override
  public DivertCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    EAssert.Argument.isTrue(patternIndex == 0);
    return new DivertCommand();
  }
}
