package eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.Navaid;

public class ProceedDirectParser extends SpeechParser<ProceedDirectCommand> {

  private static final String [][]patterns = {{"PD","\\S+"}};
  public String getHelp() {
    String ret = super.buildHelpString(
        "Proceed direct to",
        "PD {fixName}",
        "Orders to proceed direct to specified fix.\nFix can be also specified using fix/radial/distance format.",
        "PD ERASU\nPD SIGMA/030/20.5");
    return ret;
  }
  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ProceedDirectCommand parse(IList<String> blocks) {
    String ns = blocks.get(1);

    Navaid n = Acc.area().getNavaids().getOrGenerate(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", blocks.get(1));
    }
    ProceedDirectCommand ret = new ProceedDirectCommand(n);
    return ret;
  }
}
