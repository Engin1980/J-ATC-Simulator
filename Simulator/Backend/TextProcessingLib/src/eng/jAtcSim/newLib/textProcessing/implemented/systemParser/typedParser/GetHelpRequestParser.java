package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.system.user2system.GetHelpRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.SmartTextSpeechFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class GetHelpRequestParser extends TextSpeechParser<GetHelpRequest> {
  @Override
  public String[][] getPatterns() {
    return new String[][]{
        {"help"},
        {"help", "[A-Z]+"}};
  }

  @Override
  public String getHelp() {
    return super.buildHelpString(
        "get-help",
        "help\nhelp {cmd}",
        "Returns help overview or help for specific command.",
        "-help\n-help CM"
    );
  }

  @Override
  public GetHelpRequest parse(IList<String> blocks) {
    return new GetHelpRequest();
  }
}
