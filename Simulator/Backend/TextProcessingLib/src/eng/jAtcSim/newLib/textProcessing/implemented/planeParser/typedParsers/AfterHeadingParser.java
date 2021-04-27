package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterHeadingParser extends TextSpeechParser<AfterHeadingCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AH (\\d{1,3})");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After heading",
            "AH {heading}",
            "When (more-less) on specified heading. Heading must consist of 3 digits.",
            "AH 030");
    return ret;
  }

  @Override
  public AfterHeadingCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    int hdg = groups.getInt(1);
    AfterHeadingCommand ret = AfterHeadingCommand.create(hdg);
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

}
