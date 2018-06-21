package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class DivertParser extends SpeechParser<DivertCommand> {

  private static final String[][] patterns = {{"DVT"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public DivertCommand parse(IList<String> blocks) {
    return new DivertCommand();
  }
}
