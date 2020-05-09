package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeSpeedCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.textParsing.SpeechParser;

public class ChangeSpeedParser extends SpeechParser<ChangeSpeedCommand> {

  private static final String[][] patterns = {
      {"SC"},
      {"SM", "\\d{1,3}"},
      {"SL", "\\d{1,3}"},
      {"SE", "\\d{1,3}"}
  };

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Change speed",
        "SC - Cancel current speed restriction\n"+
            "SM {speed} - Speed more or equal to value\n"+
            "SL {speed} - Speed less or equal to value\n"+
            "SE {speed} - Speed exactly",
        "Changes airplane speed",
        "SC\n"+
            "SM 200\n"+
            "SL 200\n"+
            "SE 200");
    return ret;
  }
  
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
      ret = ChangeSpeedCommand.createResumeOwnSpeed();
    } else {
      int speed = super.getInt(blocks,1);
      char c = blocks.get(0).charAt(1);
      switch (c) {
        case 'L':
          ret = ChangeSpeedCommand.create(AboveBelowExactly.below, speed);
          break;
        case 'M':
          ret = ChangeSpeedCommand.create(AboveBelowExactly.above, speed);
          break;
        case 'E':
          ret = ChangeSpeedCommand.create(AboveBelowExactly.exactly, speed);
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    return ret;
  }

}
