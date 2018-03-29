package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class SetAltitudeRestrictionParser extends SpeechParser<SetAltitudeRestriction> {

  private static final String[] prefixes = new String[]{"AM", "AL", "AE", "AC"};
  private static final String pattern = "((AM)|(AL)|(AE)|(AC))( (\\d{1,3}))?";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public SetAltitudeRestriction parse(RegexGrouper rg) {
    Restriction res;
    SetAltitudeRestriction ret;
    String dirS = rg.getString(1);
    if (dirS.equals("AR")) {
      res = null;
    } else {
      int val = rg.getInt(7) * 100;
      switch (rg.getString(1)) {
        case "AM":
          res = new Restriction(Restriction.eDirection.atLeast, val);
          break;
        case "AL":
          res = new Restriction(Restriction.eDirection.atMost, val);
          break;
        case "AE":
          res = new Restriction(Restriction.eDirection.exactly, val);
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    ret = new SetAltitudeRestriction(res);
    return ret;
  }
}