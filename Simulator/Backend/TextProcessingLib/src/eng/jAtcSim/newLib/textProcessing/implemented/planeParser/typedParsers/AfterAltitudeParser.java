package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import java.util.Optional;

public class AfterAltitudeParser extends TextSpeechParser<AfterAltitudeCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AA (\\d{1,3})([+-])?");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After altitude", "AA {altitude}[+/-]",
            "When passing specified altitude in FL; +/- means \"or above/below\"",
            "AA 130+\nAA 050\nAA 40-");
    return ret;
  }

  @Override
  public AfterAltitudeCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    EAssert.Argument.isTrue(patternIndex == 0);
    int alt = groups.getInt(1) * 100;
    Optional<Character> c = groups.tryGetChar(2);
    AboveBelowExactly res =
            c.isEmpty() ? AboveBelowExactly.exactly : c.get()=='-' ? AboveBelowExactly.below : AboveBelowExactly.above;
    AfterAltitudeCommand ret = AfterAltitudeCommand.create(alt, res);
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }
}
