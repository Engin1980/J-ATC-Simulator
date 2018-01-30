package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;

class ContactParser extends SpeechParser<ContactCommand> {

  private static final String[] prefixes = new String[]{"CT", "CA", "CC"};
  private static final String pattern = "(CT|CA|CC)";

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  ContactCommand parse(RegexGrouper rg) {
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
