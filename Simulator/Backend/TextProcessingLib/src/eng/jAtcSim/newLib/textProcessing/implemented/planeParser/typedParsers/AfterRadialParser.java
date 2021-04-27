package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands.AfterRadialCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class AfterRadialParser extends TextSpeechParser<AfterRadialCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "AR (\\S+)/(\\d{1,3})");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "After radial",
            "AR {fixName}/{radial}",
            "When passing radial to fix",
            "AR KENOK/030\nAR KENOK/30");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public AfterRadialCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String ns = groups.getString(1);
    int rad = groups.getInt(2);
    AfterRadialCommand ret = AfterRadialCommand.create(ns, rad);
    return ret;
  }
}
