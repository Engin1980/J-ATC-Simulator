package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterRadialCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;

public class AfterRadialParser extends SpeechParser<AfterRadialCommand> {
  private static final String[] prefixes = new String[]{"AR"};
  private static final String pattern = "AR (\\S+)/(\\d{1,3})";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterRadialCommand parse(RegexGrouper rg) {
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    int rad = rg.getInt(2);
    AfterRadialCommand ret = new AfterRadialCommand(n, rad);
    return ret;
  }
}
