package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterHeadingCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterHeadingParser extends TextSpeechParser<AfterHeadingCommand> {

  private static final String[][] patterns = {{"AH", "\\d{1,3}"}};

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After heading",
        "AH {heading}",
        "When (more-less) on specified heading",
        "AH 030");
    return ret;
  }

  @Override
  public AfterHeadingCommand parse(IList<String> blocks) {
    int hdg = super.getInt(blocks, 1);
    AfterHeadingCommand ret = AfterHeadingCommand.create(hdg);
    return ret;
  }

}
