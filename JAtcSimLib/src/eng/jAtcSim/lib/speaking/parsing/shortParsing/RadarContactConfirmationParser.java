package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.fromAtc.notifications.RadarContactConfirmationNotification;

class RadarContactConfirmationParser extends SpeechParser<RadarContactConfirmationNotification> {

  private static final String[] prefixes = new String[]{"RC"};
  private static final String pattern = "RC";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  RadarContactConfirmationNotification parse(RegexGrouper rg) {
    RadarContactConfirmationNotification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
