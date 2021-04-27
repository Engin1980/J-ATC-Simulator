package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ReportDivertTimeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ReportDivertTimeParser extends TextSpeechParser<ReportDivertTimeCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "RDVT");

  public String getHelp() {
    String ret = super.buildHelpString(
            "Report divert time",
            "RDVT",
            "Asks an airplane about its divert time.",
            "RDVT");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ReportDivertTimeCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    return new ReportDivertTimeCommand();
  }
}
