package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ReportDivertTimeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ReportDivertTimeParser extends TextSpeechParser<ReportDivertTimeCommand> {

  private static final String [][]patterns = {{"RDVT"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Report divert time",
        "RDVT",
        "Asks an airplane about its divert time.",
        "RDVT");
    return ret;
  }
  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ReportDivertTimeCommand parse(IList<String> blocks) {
    return new ReportDivertTimeCommand();
  }
}
