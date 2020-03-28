package eng.jAtcSim.newLib.textProcessing.implemented.parsers.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.speeches.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.shortBlockParser.SpeechParser;

public class ContactParser extends SpeechParser<ContactCommand> {

  private static final String[][] patterns = {
      {"CNT"}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Contact other ATC",
        "CNT {atc_name} - contact atc with the specified name",
        "Orders airplane to contact other ATC",
        "CNT LKPR_TWR");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ContactCommand parse(IList<String> blocks) {
//    AtcType tuddla;
//    switch (blocks.get(0)) {
//      case "CT":
//        t = Atc.eType.twr;
//        break;
//      case "CC":
//        t = Atc.eType.ctr;
//        break;
//      default:
//        throw new EEnumValueUnsupportedException(blocks.get(0));
//    }
//    ContactCommand ret = new ContactCommand(t);
//    return ret;

    ContactCommand ret;
    String atcName = blocks.get(1);
    ret = new ContactCommand(atcName, 0);
    return ret;
  }
}
