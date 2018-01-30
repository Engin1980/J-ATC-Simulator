package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;

class ChangeAltitudeParser extends SpeechParser<ChangeAltitudeCommand> {

  private static final String[] prefixes = new String[]{"MA", "CM", "DM"};
  private static final String pattern = "((MA)|(CM)|(DM)) ?(\\d{1,3})";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  ChangeAltitudeCommand parse(RegexGrouper rg) {
    ChangeAltitudeCommand ret;
    ChangeAltitudeCommand.eDirection d;
    int a;

    switch (rg.getString(1)) {
      case "MA":
        d = ChangeAltitudeCommand.eDirection.any;
        break;
      case "CM":
        d = ChangeAltitudeCommand.eDirection.climb;
        break;
      case "DM":
        d = ChangeAltitudeCommand.eDirection.descend;
        break;
      default:
        throw new ERuntimeException("Invalid prefix for Maintain-altitude command.");
    }

    a = rg.getInt(5) * 100;

    ret = new ChangeAltitudeCommand(d, a);
    return ret;
  }
}
