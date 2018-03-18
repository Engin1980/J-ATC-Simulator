package eng.jAtcSim.lib.speaking.parsing.shortParsing.fromPlaneParsers;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class ReportDivertTimeParser extends SpeechParser<ReportDivertTime> {

  private static final String[] prefixes = new String[]{"RDVT"};
  private static final String pattern = "(RDVT)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public ReportDivertTime parse(RegexGrouper line) {
    return new ReportDivertTime();
  }
}
