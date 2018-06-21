package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ChangeSpeedParser extends SpeechParser<ChangeSpeedCommand> {

  private static final String[][] patterns = {
      {"SC"},
      {"S[MLE]", "\\d{3}"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ChangeSpeedCommand parse(IList<String> blocks) {

    ChangeSpeedCommand ret;

    // 1. rg je SC
    // 2. rg je SL/SM/SE
    // 3. rg je kts
    if (blocks.get(0).equals("SC")) {
      ret = new ChangeSpeedCommand();
    } else {
      int speed = super.getInt(blocks,1);
      char c = blocks.get(0).charAt(1);
      switch (c) {
        case 'L':
          ret = new ChangeSpeedCommand(Restriction.eDirection.atMost, speed);
          break;
        case 'M':
          ret = new ChangeSpeedCommand(Restriction.eDirection.atLeast, speed);
          break;
        case 'E':
          ret = new ChangeSpeedCommand(Restriction.eDirection.exactly, speed);
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    return ret;
  }

}
