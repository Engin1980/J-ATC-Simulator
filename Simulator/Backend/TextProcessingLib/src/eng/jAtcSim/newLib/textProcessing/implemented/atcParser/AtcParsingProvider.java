package eng.jAtcSim.newLib.textProcessing.implemented.atcParser;

import eng.eSystem.collections.ISet;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.textProcessing.IWithHelp;
import eng.jAtcSim.newLib.textProcessing.implemented.ParsingProviderUtils;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.PlaneSwitchRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayInUseRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.atcParser.typedParsers.RunwayMaintenanceRequestParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParserList;
import eng.jAtcSim.newLib.textProcessing.parsing.EInvalidCommandException;
import eng.jAtcSim.newLib.textProcessing.parsing.IAtcParsingProvider;

public class AtcParsingProvider implements IAtcParsingProvider, IWithHelp {

  private static final TextSpeechParserList<IAtcSpeech> atcParsers;

  static {
    atcParsers = new TextSpeechParserList<>();
    atcParsers.add(new RunwayMaintenanceRequestParser());
    atcParsers.add(new RunwayInUseRequestParser());
//    atcParsers.add(new PlaneSwitchRequestCancelationParser());
    atcParsers.add(new PlaneSwitchRequestParser());
  }

  @Override
  public boolean acceptsType(Class<?> type) {
    return String.class.equals(type);
  }

  @Override
  public boolean canAccept(Object input) {
    if (input == null) return true;
    String tmp = (String) input;
    tmp = tmp.trim();
    return (tmp.length() == 0 || tmp.charAt(0) == '+' || tmp.charAt(0) == '-');
  }

  @Override
  public String getHelp() {
    return atcParsers.getHelp();
  }

  @Override
  public String getHelp(Object cmd) {
    String tag = (String) cmd;
    return atcParsers.getHelp(tag);
  }

  @Override
  public IAtcSpeech parse(Object input) {
    StringBuilder todo = new StringBuilder();
    StringBuilder done = new StringBuilder();
    ParsingProviderUtils.prepareStringBuilders(input, todo, done);

    ISet<TextSpeechParser<? extends IAtcSpeech>> parsers = atcParsers.getAllByPatterns(todo.toString());
    if (parsers.size() == 0)
      throw new EInvalidCommandException("Failed to parse atc command.", done.toString(), todo.toString());
    else if (parsers.size() > 1)
      throw new EInvalidCommandException("There are multiple ways to parse atc command (probably internal error?).",
              done.toString(), todo.toString());

    IAtcSpeech ret = ParsingProviderUtils.parseWithParser(parsers.getFirst(), todo, done);
    return ret;
  }

}
