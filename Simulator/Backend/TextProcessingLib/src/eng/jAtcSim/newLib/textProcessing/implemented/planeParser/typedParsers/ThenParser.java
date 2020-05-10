package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ThenParser extends TextSpeechParser<ThenCommand> {

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
    ThenCommand ret = ThenCommand.create();
    return ret;
  }
}
