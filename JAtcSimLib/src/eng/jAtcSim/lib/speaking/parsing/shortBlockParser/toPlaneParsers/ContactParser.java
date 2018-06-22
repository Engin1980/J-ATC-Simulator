package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;

import java.util.Arrays;

public class ContactParser extends SpeechParser<ContactCommand> {

  private static final String[][] patterns = {
      {"CT"},
      {"CC"}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Contact other ATC",
        "CT - contact tower\nCC - contac center",
        "Orders airplane to contact other ATC",
        "CT\nCC");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ContactCommand parse(IList<String> blocks) {
    Atc.eType t;
    switch (blocks.get(0)) {
      case "CT":
        t = Atc.eType.twr;
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
