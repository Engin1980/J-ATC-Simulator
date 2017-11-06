package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.Speech;
import jatcsimlib.speaking.notifications.Notification;
import jatcsimlib.speaking.notifications.specific.RadarContactConfirmationNotification;

class RadarContactConfirmationParser extends SpeechParser {

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
  Speech parse(RegexGrouper rg) {
    Notification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
