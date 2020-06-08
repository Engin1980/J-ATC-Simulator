package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters.AfterHeadingCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;

public class AfterHeadingParser extends SpeechParser<AfterHeadingCommand> {

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
    AfterHeadingCommand ret = new AfterHeadingCommand(hdg);
    return ret;
  }

}
