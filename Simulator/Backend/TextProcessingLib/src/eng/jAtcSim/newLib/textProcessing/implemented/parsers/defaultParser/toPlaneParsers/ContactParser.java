package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.LocalInstanceProvider;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class ContactParser extends SpeechParser<ContactCommand> {

  private static final String[][] patterns = {
      {"((CT)|(CC))|(CNT ([A-Z_]+))"}};

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
        "Contact other ATC",
        "CNT {atc_name} - contact atc with the specified name\nCC - contact CTR\nCT - contact TWR",
        "Orders airplane to contact other ATC",
        "CNT LKPR_TWR\nCT\nCC");
    return ret;
  }

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public ContactCommand parse(IList<String> blocks) {
    IReadOnlyList<AtcId> atcs = LocalInstanceProvider.getAtcIds();
    AtcId atcId;
    if (blocks.get(1) != null){
      switch (blocks.get(1)) {
      case "CT":
        atcId = atcs.getFirst(q->q.getType() == AtcType.ctr);
        break;
      case "CC":
        atcId = atcs.getFirst(q->q.getType() == AtcType.twr);
        break;
      default:
        throw new EEnumValueUnsupportedException(blocks.get(1));
    }
    } else if (blocks.get(5) != null){
      // CNT variant
      String atcName = blocks.get(6);
      atcId = atcs.getFirst(q-> q.getName().equals(atcName));
    } else
      throw new EApplicationException("Some error when analysing contact parse command.");

    ContactCommand ret = new ContactCommand(atcId);
    return ret;
  }
}
