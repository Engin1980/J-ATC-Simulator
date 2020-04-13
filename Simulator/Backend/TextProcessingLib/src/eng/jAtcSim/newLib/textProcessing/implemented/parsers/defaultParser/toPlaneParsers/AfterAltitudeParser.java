package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

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
    AboveBelowExactly res;
    char c = s.charAt(s.length() - 1);
    switch (c) {
      case '-':
        res = AboveBelowExactly.below;
        s = s.substring(0, s.length() - 1);
        break;
      case '+':
        res = AboveBelowExactly.above;
        s = s.substring(0, s.length() - 1);
        break;
      default:
        res = AboveBelowExactly.exactly;
        break;
    }
    int alt = Integer.parseInt(s) * 100;
    AfterAltitudeCommand ret = AfterAltitudeCommand.create(alt, res);
    return ret;
  }
}
