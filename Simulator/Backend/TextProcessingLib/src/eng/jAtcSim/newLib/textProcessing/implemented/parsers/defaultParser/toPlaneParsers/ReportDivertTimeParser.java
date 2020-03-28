package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc2airplane.ReportDivertTimeNotification;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class ReportDivertTimeParser extends SpeechParser<ReportDivertTimeNotification> {

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
  public ReportDivertTimeNotification parse(IList<String> blocks) {
    return new ReportDivertTimeNotification();
  }
}
