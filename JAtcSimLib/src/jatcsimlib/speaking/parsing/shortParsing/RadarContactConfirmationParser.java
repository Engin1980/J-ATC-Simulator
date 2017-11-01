package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.notifications.Notification;
import jatcsimlib.speaking.notifications.specific.RadarContactConfirmationNotification;
import jatcsimlib.speaking.parsing.CmdParser;

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
  Command parse(RegexGrouper rg) {
    Notification ret = new RadarContactConfirmationNotification();
    return ret;
  }
}
