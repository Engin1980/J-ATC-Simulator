package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterSpeedCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class AfterSpeedParser extends SpeechParser<AfterSpeedCommand> {

  private static final String[][] patterns = {{"AS","\\d{1,3}"}};
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
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterSpeedCommand parse(IList<String> blocks) {
    int s = super.getInt(blocks, 1);
    AfterSpeedCommand ret = AfterSpeedCommand.create(s, AboveBelowExactly.exactly);
    return ret;
  }
}
