package eng.jAtcSim.newLib.textProcessing.implemented.systemParser.typedParser;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.system.user2system.GetHelpRequest;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class GetHelpRequestParser extends TextSpeechParser<GetHelpRequest> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "HELP",
          "HELP ([A-Z]+)");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public GetHelpRequest parse(int patternIndex, RegexUtils.RegexGroups groups) {
    GetHelpRequest ret;
    switch (patternIndex) {
      case 0:
        ret = new GetHelpRequest();
        break;
      case 1:
        ret = new GetHelpRequest(groups.getString(1));
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }

    return new GetHelpRequest();
  }
}
