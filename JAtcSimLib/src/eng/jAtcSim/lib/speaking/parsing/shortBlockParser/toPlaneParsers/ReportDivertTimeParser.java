package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ReportDivertTimeParser extends SpeechParser<ReportDivertTime> {

  private static final String [][]patterns = {{"RDVT"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ReportDivertTime parse(IList<String> blocks) {
    return new ReportDivertTime();
  }
}
