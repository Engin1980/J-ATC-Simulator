package eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.newLib.textProcessing.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.newLib.world.approaches.Approach;

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

    Approach.ApproachType type;
    switch (typeS) {
      case "G":
      case "GNSS":
        type = Approach.ApproachType.gnss;
        break;
      case "I":
        type = Approach.ApproachType.ils_I;
        break;
      case "II":
        type = Approach.ApproachType.ils_II;
        break;
      case "III":
        type = Approach.ApproachType.ils_III;
        break;
      case "N":
      case "NDB":
        type = Approach.ApproachType.ndb;
        break;
      case "R":
      case "VOR":
        type = Approach.ApproachType.vor;
        break;
      case "V":
      case "VISUAL":
        type = Approach.ApproachType.visual;
        break;
      default:
        throw new EEnumValueUnsupportedException(typeS);
    }

    ClearedToApproachCommand ret = new ClearedToApproachCommand(runwayName, type);
    return ret;
  }
}
