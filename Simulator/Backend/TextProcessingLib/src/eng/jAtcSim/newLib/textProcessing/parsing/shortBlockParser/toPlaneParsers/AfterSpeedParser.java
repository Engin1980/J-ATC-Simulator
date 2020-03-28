package eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters.AfterSpeedCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;

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
    AfterSpeedCommand ret = new AfterSpeedCommand(s);
    return ret;
  }
}
