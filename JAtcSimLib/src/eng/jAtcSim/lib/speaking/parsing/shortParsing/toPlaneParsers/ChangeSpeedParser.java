package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class ChangeSpeedParser extends SpeechParser<ChangeSpeedCommand> {

  private static final String[] prefixes = new String[]{"SM", "SL", "SE", "SC"};
  private static final String pattern = "(SC)|(?:(S[MLE]) ?(\\d{3}))";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public ChangeSpeedCommand parse(RegexGrouper rg) {

    ChangeSpeedCommand ret;

    // 1. rg je SC
    // 2. rg je SL/SM/SE
    // 3. rg je kts
    if (rg.getString(1) != null) {
      ret = new ChangeSpeedCommand();
    } else {
      int speed = rg.getInt(3);
      char c = rg.getString(2).charAt(1);
      switch (c) {
        case 'L':
          ret = new ChangeSpeedCommand(Restriction.eDirection.atMost, speed);
          break;
        case 'M':
          ret = new ChangeSpeedCommand(Restriction.eDirection.atLeast, speed);
          break;
        case 'E':
          ret = new ChangeSpeedCommand(Restriction.eDirection.exactly, speed);
          break;
        default:
          throw new ENotSupportedException();
      }
    }
    return ret;
  }

}
