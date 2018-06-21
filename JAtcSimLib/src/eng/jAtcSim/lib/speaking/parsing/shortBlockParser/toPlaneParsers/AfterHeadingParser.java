package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterHeadingCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class AfterHeadingParser extends SpeechParser<AfterHeadingCommand> {

  private static final String[][] patterns = {{"AH", "\\d{1,3)"}};


  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterHeadingCommand parse(IList<String> blocks) {
    int hdg = super.getInt(blocks, 1);
    AfterHeadingCommand ret = new AfterHeadingCommand(hdg);
    return ret;
  }

}
