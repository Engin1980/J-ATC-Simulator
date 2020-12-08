package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterNavaidCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterNavaidParser extends TextSpeechParser<AfterDistanceCommand> {

  private static final String[][] patterns = {{"AN", "\\S+"}};

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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterDistanceCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);
    AfterNavaidCommand ret = AfterNavaidCommand.create(ns);
    return ret;
  }
}
