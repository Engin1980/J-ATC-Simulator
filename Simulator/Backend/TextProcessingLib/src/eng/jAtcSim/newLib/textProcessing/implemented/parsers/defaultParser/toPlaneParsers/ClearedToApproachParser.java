package eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToApproachCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.parsers.defaultParser.common.SpeechParser;

public class ClearedToApproachParser extends SpeechParser<ClearedToApproachCommand> {

  private static final String pattern = "C (I|II|III|G|V|R|N|GNSS|VOR|NDB|VISUAL) (\\S+)";
  private static final String[][] patterns = {
      {"C", "I|II|III|G|V|R|N|GNSS|VOR|NDB|VISUAL", "\\S+"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

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
  public ClearedToApproachCommand parse(IList<String> blocks) {
    String typeS = blocks.get(1);
    String runwayName = blocks.get(2);

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
        throw new EEnumValueUnsupportedException(typeS);
    }

    ClearedToApproachCommand ret = new ClearedToApproachCommand(runwayName, type);
    return ret;
  }
}
