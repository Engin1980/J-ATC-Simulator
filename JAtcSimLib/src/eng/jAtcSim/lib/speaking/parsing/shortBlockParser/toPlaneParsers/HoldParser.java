package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.PublishedHold;

public class HoldParser extends SpeechParser<HoldCommand> {

  private static final String [][]patterns = {
      {"H","\\S{1,5}"},
      {"H","\\S{1,5}", "\\d{3}", "R|L"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public HoldCommand parse(IList<String> rg) {
    HoldCommand ret;

    String ns = rg.get(1);
    Navaid n = Acc.area().getNavaids().tryGet(ns);
    if (n == null) {
      throw new EInvalidCommandException("Unable to find navaid named \"" + ns + "\".", rg.get(1));
    }

    if (rg.size() == 2){
      // published
      PublishedHold h = Acc.airport().getHolds().tryGetFirst(q->q.getNavaid().equals(n));

      if (h == null) {
        throw new EInvalidCommandException(
            "Hold over " + ns + " is not published. You must specify exact hold procedure.",
            rg.get(1));
      }

      ret = new HoldCommand(h);
    }
    else {
      Integer heading = getInt(rg, 2);
      char leftOrRight = rg.get(3).charAt(0);

      boolean left = leftOrRight == 'L';
      ret = new HoldCommand(n, heading, left);
    }

    return ret;
  }
}
