package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class GoAroundParser extends SpeechParser<GoAroundCommand> {

  private static final String[][] patterns = {{"DVT"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public GoAroundCommand parse(IList<String> blocks) {
    return new GoAroundCommand();
  }
}
