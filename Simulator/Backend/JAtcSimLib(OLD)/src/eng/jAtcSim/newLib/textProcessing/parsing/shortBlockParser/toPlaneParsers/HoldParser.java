package eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.exceptions.EInvalidCommandException;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.newLib.area.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.Navaid;
import eng.jAtcSim.newLib.world.PublishedHold;

public class HoldParser extends SpeechParser<HoldCommand> {

  private static final String[][] patterns = {
      {"H", "\\S{1,5}", "\\d{3}", "R|L"},
      {"H", "\\S{1,5}"},
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  public String getHelp() {
    String ret = super.buildHelpString(
        "Hold",
        "H {fixName} - for published hold\n" +
            "H {fixName} {inboundRadial} {L/R} - for custom hold. L=left turns, R=right turns",
        "Hold over specified fix. When short version used, hold must be published.\nOtherwise hold parameters must be specified.\nFix can be also specified using fix/radial/distance format.",
        "H ERASU\nH SIGMA 040 R\nH SIGMA/030/20.5 250 R");
    return ret;
  }

  @Override
  public HoldCommand parse(IList<String> rg) {
    HoldCommand ret;

    String ns = rg.get(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.get(1));
    }

    if (rg.size() == 2) {
      // published
      PublishedHold h = Acc.airport().getHolds().tryGetFirst(q -> q.getNavaid().equals(n));

      if (h == null) {
        throw new EInvalidCommandException(
            "Hold over " + ns + " is not published. You must specify exact hold procedure.",
            rg.get(1));
      }

      ret = new HoldCommand(h);
    } else {
      Integer heading = getInt(rg, 2);
      char leftOrRight = rg.get(3).charAt(0);

      boolean left = leftOrRight == 'L';
      ret = new HoldCommand(n, heading, left);
    }

    return ret;
  }
}
