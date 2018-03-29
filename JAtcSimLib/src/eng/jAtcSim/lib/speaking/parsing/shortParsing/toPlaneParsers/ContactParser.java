package eng.jAtcSim.lib.speaking.parsing.shortParsing.toPlaneParsers;

import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.RegexGrouper;
import eng.jAtcSim.lib.speaking.parsing.shortParsing.SpeechParser;

public class ContactParser extends SpeechParser<ContactCommand> {

  private static final String[] prefixes = new String[]{"CT", "CA", "CC"};
  private static final String pattern = "(CT|CA|CC)";

  @Override
  public String[] getPrefixes() {
    return prefixes;
  }

  @Override
  public String getPattern() {
    return pattern;
  }

  @Override
  public ContactCommand parse(RegexGrouper rg) {
    Atc.eType t;
    switch (rg.getString(1)) {
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
        throw new ENotSupportedException();
    }
    ContactCommand ret = new ContactCommand(t);
    return ret;
  }
}
