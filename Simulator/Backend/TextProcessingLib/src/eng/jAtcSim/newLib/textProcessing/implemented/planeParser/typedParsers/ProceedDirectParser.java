package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ProceedDirectParser extends TextSpeechParser<ProceedDirectCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "PD (\\S+)");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Proceed direct to",
            "PD {fixName}",
            "Orders to proceed direct to specified fix.\nFix can be also specified using fix/radial/distance format.",
            "PD ERASU\nPD SIGMA/030/20.5");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ProceedDirectCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String ns = groups.getString(1);
    ProceedDirectCommand ret = ProceedDirectCommand.create(ns);
    return ret;
  }
}
