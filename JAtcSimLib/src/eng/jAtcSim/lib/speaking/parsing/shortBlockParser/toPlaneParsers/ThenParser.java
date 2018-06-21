package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ThenCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ThenParser extends SpeechParser<ThenCommand> {

  private static final String [][]patterns = {{"T"}};

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
