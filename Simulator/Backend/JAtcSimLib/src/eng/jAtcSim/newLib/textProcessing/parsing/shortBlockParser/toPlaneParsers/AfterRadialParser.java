package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters.AfterRadialCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.RegexGrouper;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.Navaid;

public class AfterRadialParser extends SpeechParser<AfterRadialCommand> {
  private static final String BLOCK_PATTERN = "(\\S+)/(\\d{1,3})";
  private static final String[][] patterns = {{"AR", BLOCK_PATTERN}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "After radial",
        "AR {fixName}/{radial}",
        "When passing radial to fix",
        "AR KENOK/030\nAR KENOK/30");
    return ret;
  }


  @Override
  public String [][] getPatterns() {
    return patterns;
  }

  @Override
  public AfterRadialCommand parse(IList<String> blocks) {

    RegexGrouper rg = RegexGrouper.apply(blocks.get(1), BLOCK_PATTERN);
    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGetFirst(q->q.getName().equals(ns));
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }
    int rad = rg.getInt(2);
    AfterRadialCommand ret = new AfterRadialCommand(n, rad);
    return ret;
  }
}
