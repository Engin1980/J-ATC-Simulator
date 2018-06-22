package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterSpeedCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

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
