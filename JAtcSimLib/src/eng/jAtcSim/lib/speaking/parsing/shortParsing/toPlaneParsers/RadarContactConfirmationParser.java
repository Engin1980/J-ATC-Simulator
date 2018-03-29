package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class RadarContactConfirmationParser extends SpeechParser<RadarContactConfirmationNotification> {

  private static final String[] prefixes = new String[]{"RC"};
  private static final String pattern = "RC";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public RadarContactConfirmationNotification parse(RegexGrouper rg) {
    RadarContactConfirmationNotification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
