package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

public class ThenParser extends SpeechParser<ThenCommand> {

  private static final String [][]patterns = {{"T"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Then",
        "T",
        "Tells that all the following commands will be applied when the previous command is fulfilled.\n"+
        "Previous commands can be only proceed-direct, change-altitude, change-speed or change-heading commands.",
        "T");
    return ret;
  }
  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ThenCommand parse(IList<String> blocks) {
    ThenCommand ret = new ThenCommand();
    return ret;
  }
}
