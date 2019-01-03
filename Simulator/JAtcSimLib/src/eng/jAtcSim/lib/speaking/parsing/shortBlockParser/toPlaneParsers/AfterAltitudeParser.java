package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.afters.AfterAltitudeCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class AfterAltitudeParser extends SpeechParser<AfterAltitudeCommand> {

  private static final String[][] patterns = {
      {"AA", "\\d{1,3}[+-]?"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After altitude", "AA {altitude}[+/-]",
        "When passing specified altitude in FL; +/- means \"or above/below\"",
        "AA 130+\nAA 050\nAA 40-");
    return ret;
  }

  @Override
  public AfterAltitudeCommand parse(IList<String> blocks) {
    String s = blocks.get(1);
    AfterAltitudeCommand.ERestriction res;
    char c = s.charAt(s.length() - 1);
    switch (c) {
      case '-':
        res = AfterAltitudeCommand.ERestriction.andBelow;
        s = s.substring(0, s.length() - 1);
        break;
      case '+':
        res = AfterAltitudeCommand.ERestriction.andAbove;
        s = s.substring(0, s.length() - 1);
        break;
      default:
        res = AfterAltitudeCommand.ERestriction.exact;
        break;
    }
    int alt = Integer.parseInt(s) * 100;
    AfterAltitudeCommand ret = new AfterAltitudeCommand(alt, res);
    return ret;
  }
}
