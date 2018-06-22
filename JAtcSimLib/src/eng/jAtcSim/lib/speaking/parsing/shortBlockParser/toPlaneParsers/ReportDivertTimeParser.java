package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

public class ReportDivertTimeParser extends SpeechParser<ReportDivertTime> {

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
  public ReportDivertTime parse(IList<String> blocks) {
    return new ReportDivertTime();
  }
}
