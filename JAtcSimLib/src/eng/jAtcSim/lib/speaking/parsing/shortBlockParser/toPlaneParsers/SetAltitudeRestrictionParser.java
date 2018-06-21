package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class SetAltitudeRestrictionParser extends SpeechParser<SetAltitudeRestriction> {

  private static final String[][] patterns = {
      {"AC"},
      {"AM|AL|AE", "\\d{1,3}"}
  };

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public SetAltitudeRestriction parse(IList<String> blocks) {
    Restriction res;
    SetAltitudeRestriction ret;
    if (blocks.size() == 1)
      res = null;
    else {
      int val = super.getInt(blocks,1) * 100;
      switch (blocks.get(0).charAt(1)) {
        case 'M':
          res = new Restriction(Restriction.eDirection.atLeast, val);
          break;
        case 'L':
          res = new Restriction(Restriction.eDirection.atMost, val);
          break;
        case 'E':
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