package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.system.user2system.DeletePlaneRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class DeletePlaneRequestParser extends TextSpeechParser<DeletePlaneRequest> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "REMOVE (\\D{4})");

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
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
  public DeletePlaneRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String tmp = groups.getString(1);
    Squawk sqwk = Squawk.create(tmp);
    return new DeletePlaneRequest(sqwk);
  }
}
