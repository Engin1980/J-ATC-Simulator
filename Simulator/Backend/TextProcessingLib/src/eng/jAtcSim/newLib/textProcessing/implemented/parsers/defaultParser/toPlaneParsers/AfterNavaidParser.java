package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class AfterNavaidParser extends SpeechParser<AfterDistanceCommand> {

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
    AfterDistanceCommand ret = AfterDistanceCommand.create(ns, 0, AboveBelowExactly.exactly);
    return ret;
  }
}
