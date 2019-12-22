package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;

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
