package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.system.user2system.DeletePlaneRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class DeletePlaneRequestParser extends TextSpeechParser<DeletePlaneRequest> {
  @Override
  public String[][] getPatterns() {
    return new String[][]{
      {"remove", "\\d{4}"}
    };
  }

  @Override
  public String getHelp() {
    return super.buildHelpString(
        "remove plane",
        "remove {squawk}",
        "Removes plane from the simulation. This is a tool for user to deal with some error plane and " +
            "should be used for the debug purposes only.",
        "-remove 2313"
    );
  }

  @Override
  public DeletePlaneRequest parse(IList<String> blocks) {
    Squawk sqwk = Squawk.create(blocks.get(1));
    return new DeletePlaneRequest(sqwk);
  }
}
