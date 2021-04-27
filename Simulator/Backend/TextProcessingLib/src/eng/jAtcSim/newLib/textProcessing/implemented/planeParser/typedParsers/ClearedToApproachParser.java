package eng.jAtcSim.newLib.textProcessing.implemented.planeParser.typedParsers;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToApproachCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parserHelpers.TextSpeechParser;

public class ClearedToApproachParser extends TextSpeechParser<ClearedToApproachCommand> {

  private static final IReadOnlyList<String> patterns = EList.of(
          "C (I|II|III|G|V|R|N|GNSS|VOR|NDB|VISUAL) (\\S+)");

  @Override
  public String getHelp() {
    String ret = super.buildHelpString(
            "Cleared to approach",
            "C I {rwy} - ILS cat I\n" +
                    "C II {rwy} -  ILS cat II\n" +
                    "C III {rwy} - ILS cat III\n" +
                    "C R {rwy} - VOR/DME\n" +
                    "C N {rwy} - NDB\n" +
                    "C G {rwy} - GPS/GNSS\n" +
                    "C V {rwy} - visual",
            "Gives approach clearance",
            "C I 24 \t - cleared ILS category I 24\n" +
                    "C R 24 \t - cleared VOR/DME 24\n" +
                    "C V 24 \t - cleared visual 24");
    return ret;
  }

  @Override
  public IReadOnlyList<String> getPatterns() {
    return patterns;
  }

  @Override
  public ClearedToApproachCommand parse(int patternIndex, RegexUtils.RegexGroups groups) {
    String typeS = groups.getString(1);
    String runwayName = groups.getString(2);

    ApproachType type;
    switch (typeS) {
      case "G":
      case "GNSS":
        type = ApproachType.gnss;
        break;
      case "I":
        type = ApproachType.ils_I;
        break;
      case "II":
        type = ApproachType.ils_II;
        break;
      case "III":
        type = ApproachType.ils_III;
        break;
      case "N":
      case "NDB":
        type = ApproachType.ndb;
        break;
      case "R":
      case "VOR":
        type = ApproachType.vor;
        break;
      case "V":
      case "VISUAL":
        type = ApproachType.visual;
        break;
      default:
        throw new UnexpectedValueException(typeS);
    }

    ClearedToApproachCommand ret = new ClearedToApproachCommand(runwayName, type);
    return ret;
  }
}
