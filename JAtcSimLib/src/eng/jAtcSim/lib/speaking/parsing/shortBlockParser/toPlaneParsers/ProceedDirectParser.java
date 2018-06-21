package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

public class ProceedDirectParser extends SpeechParser<ProceedDirectCommand> {

  private static final String [][]patterns = {{"PD","\\S+"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ProceedDirectCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);

    Navaid n = Acc.area().getNavaids().getOrGenerate(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", blocks.get(1));
    }
    ProceedDirectCommand ret = new ProceedDirectCommand(n);
    return ret;
  }
}
