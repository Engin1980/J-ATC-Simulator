package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.enums.AtcType;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;
import eng.jAtcSim.newLib.textProcessing.contextLocal.Context;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ContactParser extends TextSpeechParser<ContactCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "CT", "CC", "CNT (A-Z_)");

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
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ContactCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    IReadOnlyList<AtcId> atcs = Context.getShared().getAtcs();
    AtcId atcId;
    switch (patternIndex) {
      case 0:
        atcId = atcs.getFirst(q -> q.getType() == AtcType.twr);
        break;
      case 1:
        atcId = atcs.getFirst(q -> q.getType() == AtcType.ctr);
        break;
      case 2:
        String atcName = groups.getString(1);
        atcId = atcs.tryGetFirst(q -> q.getName().equals(atcName))
                .orElseThrow(() -> new ApplicationException(sf("Unable to recognize ATC name '%s'.", atcName)));
        break;
      default:
        throw new UnexpectedValueException(patternIndex);
    }

    ContactCommand ret = new ContactCommand(atcId);
    return ret;
  }
}
