package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.specific.ChangeSpeedCommand;

class ChangeSpeedParser extends SpeechParser {

  private static final String[] prefixes = new String[]{"SM", "SL", "SE", "SR"};
  private static final String pattern = "(SR)|(?:(S[MLE]) ?(\\d{3}))";

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

    ChangeSpeedCommand ret;

    // 1. rg je SR
    // 2. rg je SL/SM/SE
    // 3. rg je kts
    if (rg.getString(1) != null) {
      ret = new ChangeSpeedCommand();
    } else {
      int speed = rg.getInt(3);
      char c = rg.getString(2).charAt(1);
      switch (c) {
        case 'L':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.atMost, speed);
          break;
        case 'M':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.atLeast, speed);
          break;
        case 'E':
          ret = new ChangeSpeedCommand(SpeedRestriction.eDirection.exactly, speed);
          break;
        default:
          throw new ENotSupportedException();
      }
    }
    return ret;
  }

}
