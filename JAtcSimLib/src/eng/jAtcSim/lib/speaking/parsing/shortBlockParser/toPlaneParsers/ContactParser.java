package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

public class ContactParser extends SpeechParser<ContactCommand> {

  private static final String [][]patterns = {{"CT|CA|CC"}};

  @Override
  public String [][]getPatterns() {
    return patterns;
  }

  @Override
  public ContactCommand parse(IList<String> blocks) {
    Atc.eType t;
    switch (blocks.get(0)) {
      case "CT":
        t = Atc.eType.twr;
        break;
      case "CA":
        t = Atc.eType.app;
        break;
      case "CC":
        t = Atc.eType.ctr;
        break;
      default:
        throw new EEnumValueUnsupportedException(blocks.get(0));
    }
    ContactCommand ret = new ContactCommand(t);
    return ret;
  }
}
