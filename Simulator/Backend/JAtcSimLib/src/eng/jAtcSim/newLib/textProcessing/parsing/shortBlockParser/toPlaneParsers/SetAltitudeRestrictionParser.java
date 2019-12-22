package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.SetAltitudeRestriction;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.global.Restriction;

public class SetAltitudeRestrictionParser extends SpeechParser<SetAltitudeRestriction> {

  private static final String[][] patterns = {
      {"AC"},
      {"AM", "\\d{1,3}"},
      {"AL", "\\d{1,3}"},
      {"AE", "\\d{1,3}"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  public String getHelp() {
    String ret = super.buildHelpString(
        "Altitude restrictions",
        "AC - Clears current altitude restriction\n"+
            "AM {altitude} - Sets upper altitude restriction (at most)\n"+
            "AL {altitude} - Sets lower altitude restriction (at least)\n"+
            "AE {altitude} - Sets exact altitude restriction (exactly)",
        "Sets/clears altitude restrictions. Supposed for SID/STARS definitions only.",
        "AC\n"+
            "AM 050\n"+
            "AL 40\n"+
            "AE 120");
    return ret;
  }

  @Override
  public SetAltitudeRestriction parse(IList<String> blocks) {
    Restriction res;
    SetAltitudeRestriction ret;
    if (blocks.size() == 1)
      res = null;
    else {
      int val = super.getInt(blocks, 1) * 100;
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
