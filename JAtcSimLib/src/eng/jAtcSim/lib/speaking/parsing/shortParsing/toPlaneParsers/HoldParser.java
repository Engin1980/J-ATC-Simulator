package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.PublishedHold;

public class HoldParser extends SpeechParser<HoldCommand> {

  private static final String[] prefixes = new String[]{"H"};
  private static final String pattern = "H (\\S{1,5})( (\\d{3}))?( (R|L))?";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public HoldCommand parse(RegexGrouper rg) {
    HoldCommand ret;

    String ns = rg.getString(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.getMatch());
    }

    Integer heading = rg.tryGetInt(3);
    String leftOrRight = rg.tryGetString(5);

    if (heading == null) {
      PublishedHold h = Acc.airport().getHolds().get(n);

      if (h == null) {
        throw new EInvalidCommandException(
            "Hold over fix " + ns + " is not published. You must specify exact hold procedure.",
            rg.getMatch());
      }

      ret = new HoldCommand(h);
    } else {
      boolean left = leftOrRight.equals("L");
      ret = new HoldCommand(n, heading, left);
    }
    return ret;
  }
}
