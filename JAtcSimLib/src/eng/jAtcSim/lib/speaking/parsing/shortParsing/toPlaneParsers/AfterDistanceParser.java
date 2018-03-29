package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterDistanceCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

public class AfterDistanceParser extends SpeechParser<AfterDistanceCommand> {
  private static final String[] prefixes = new String[]{"AD"};
  private static final String pattern = "AD (\\S+)/(\\d+(\\.\\d+)?)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterDistanceCommand parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    double d = rg.getDouble(2);
    AfterDistanceCommand ret = new AfterDistanceCommand(n,d);
    return ret;
  }
}
