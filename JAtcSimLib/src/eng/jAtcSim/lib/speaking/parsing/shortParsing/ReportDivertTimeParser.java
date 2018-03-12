package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;

public class ReportDivertTimeParser extends SpeechParser<ReportDivertTime> {

  private static final String[] prefixes = new String[]{"RDVT"};
  private static final String pattern = "(RDVT)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  ReportDivertTime parse(RegexGrouper line) {
    return new ReportDivertTime();
  }
}
