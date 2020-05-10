package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.DivertCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class DivertParser extends TextSpeechParser<DivertCommand> {

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
