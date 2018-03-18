package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class AfterAltitudeParser extends SpeechParser<AfterAltitudeCommand> {

  private static final String[] prefixes = new String[]{"AA"};
  private static final String pattern = "AA (\\d{1,3})";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public AfterAltitudeCommand parse(RegexGrouper rg) {
    int alt = rg.getInt(1) * 100;
    AfterAltitudeCommand ret = new AfterAltitudeCommand(alt);
    return ret;
  }
}
