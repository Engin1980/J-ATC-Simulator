package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc2airplane.DivertCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

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
