package eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.SpeechParser;

public class DivertParser extends SpeechParser<DivertCommand> {

  private static final String[][] patterns = {{"DVT"}};
  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Divert",
        "DVT",
        "Orders airplane divert from your airport",
        "DVT");
    return ret;
  }
  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public DivertCommand parse(IList<String> blocks) {
    return new DivertCommand();
  }
}
