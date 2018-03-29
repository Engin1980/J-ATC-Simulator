package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterNavaidCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

public class AfterNavaidParser extends SpeechParser<AfterNavaidCommand> {

  private static final String[] prefixes = new String[]{"AN"};
  private static final String pattern = "AN (\\S+)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterNavaidCommand parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    AfterNavaidCommand ret = new AfterNavaidCommand(n);
    return ret;
  }
}
