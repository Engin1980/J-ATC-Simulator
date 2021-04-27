package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterNavaidCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterNavaidParser extends TextSpeechParser<AfterDistanceCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AH (\\S+)");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After navaid",
            "AN {fixName}",
            "When flying over fix",
            "AN KENOK");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public AfterDistanceCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String ns = groups.getString(1);
    AfterNavaidCommand ret = AfterNavaidCommand.create(ns);
    return ret;
  }

}
