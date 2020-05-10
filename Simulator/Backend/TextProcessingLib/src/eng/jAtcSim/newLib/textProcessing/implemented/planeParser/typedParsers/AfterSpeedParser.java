package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterSpeedCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterSpeedParser extends TextSpeechParser<AfterSpeedCommand> {

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
