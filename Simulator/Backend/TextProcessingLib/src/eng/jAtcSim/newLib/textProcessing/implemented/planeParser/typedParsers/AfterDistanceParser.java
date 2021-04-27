package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.RegexGrouper;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterDistanceCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterDistanceParser extends TextSpeechParser<AfterDistanceCommand> {
  private static final IReadOnlyList<String> patterns = EList.of(
          "AD (\\S+)/(\\d+(\\.\\d+)?)"
  );

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After distance from fix", "AD {fixName}/{distance",
            "After (= at) distance (in nm) from specified fix. Distance can be fractional.",
            "AD KENOK/10.8");
    return ret;
  }

  @Override
  public AfterDistanceCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    EAssert.Argument.isTrue(patternIndex == 0);

    String ns = groups.getString(1);
    double d = groups.tryGetDouble(2).orElse(0d);
    AfterDistanceCommand ret = AfterDistanceCommand.create(ns, d, AboveBelowExactly.exactly);
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }
}
