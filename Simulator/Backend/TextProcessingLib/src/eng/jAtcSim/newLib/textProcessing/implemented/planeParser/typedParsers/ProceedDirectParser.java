package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class ProceedDirectParser extends SpeechParser<ProceedDirectCommand> {

  private static final String[][] patterns = {{"PD", "\\S+"}};

  public String getHelp() {
    String ret = super.buildHelpString(
        "Proceed direct to",
        "PD {fixName}",
        "Orders to proceed direct to specified fix.\nFix can be also specified using fix/radial/distance format.",
        "PD ERASU\nPD SIGMA/030/20.5");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ProceedDirectCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);
    ProceedDirectCommand ret = ProceedDirectCommand.create(ns);
    return ret;
  }
}
