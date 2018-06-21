package eng.jAtcSim.lib.speaking.parsing.shortBlockParser.toPlaneParsers;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.speaking.parsing.shortBlockParser.SpeechParser;
import eng.jAtcSim.lib.world.approaches.Approach;

public class ClearedToApproachParser extends SpeechParser<ClearedToApproachCommand> {

  private static final String pattern = "C (I|II|III|G|V|R|N) (\\S+)";
  private static final String[][] patterns = {
      {"C", "I|II|III|G|V|R|N", "\\S+"}
  };

  @Override
  public String[][] getPatterns() {
    return patterns;
  }

  @Override
  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("Cleared to approach");
    sb.appendLine("\t " + pattern);
    sb.appendLine("\tI\t.. ILS cat I");
    sb.appendLine("\tII\t.. ILS cat II");
    sb.appendLine("\tIII\t.. ILS cat III");
    sb.appendLine("\tR\t.. VOR/DME");
    sb.appendLine("\tN\t.. NDB");
    sb.appendLine("\tG\t.. GPS/GNSS");
    sb.appendLine("\tV\t.. visual");
    sb.appendLine("Example:");
    sb.appendLine("\t C I 24 \t - cleared ILS category I 24");
    sb.appendLine("\t C R 24 \t - cleared VOR/DME 24");
    sb.appendLine("\t C V 24 \t - cleared visual 24");

    return sb.toString();
  }

  @Override
  public ClearedToApproachCommand parse(IList<String> blocks) {
    String typeS = blocks.get(1);
    String runwayName = blocks.get(2);

    Approach.ApproachType type;
    switch (typeS) {
      case "G":
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
        type = Approach.ApproachType.ndb;
        break;
      case "R":
        type = Approach.ApproachType.vor;
        break;
      case "V":
        type = Approach.ApproachType.visual;
        break;
      default:
        throw new EEnumValueUnsupportedException(typeS);
    }

    ClearedToApproachCommand ret = new ClearedToApproachCommand(runwayName, type);
    return ret;
  }
}
