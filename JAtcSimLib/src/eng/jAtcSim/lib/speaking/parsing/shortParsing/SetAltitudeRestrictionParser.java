package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;

public class SetAltitudeRestrictionParser extends SpeechParser<SetAltitudeRestriction> {

  private static final String[] prefixes = new String[]{"AM", "AL", "AE", "AR"};
  private static final String pattern = "((AM)|(AL)|(AE)|(AR))( (\\d{1,3}))?";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  SetAltitudeRestriction parse(RegexGrouper rg) {
    Restriction res;
    SetAltitudeRestriction ret;
    String dirS = rg.getString(1);
    if (dirS.equals("AR")){
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