package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.Acc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.specific.HoldCommand;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.PublishedHold;

class HoldParser extends SpeechParser {

  private static final String[] prefixes = new String[]{"H"};
  private static final String pattern = "H (\\S{1,5})( (\\d{3}))?( (R|L))?";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
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
