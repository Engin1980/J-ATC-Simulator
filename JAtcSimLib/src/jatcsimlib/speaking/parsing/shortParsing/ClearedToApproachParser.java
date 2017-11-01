package jatcsimlib.speaking.parsing.shortParsing;

import jatcsimlib.Acc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.specific.ClearedToApproachCommand;
import jatcsimlib.world.Approach;
import jatcsimlib.world.RunwayThreshold;

class ClearedToApproachParser extends SpeechParser {

  private static final String[] prefixes = new String[]{"C "};
  private static final String pattern = "C (I|II|III|G|V|R) (\\S+)";

  @Override
  public String getHelp() {
    EStringBuilder sb = new EStringBuilder();

    sb.appendLine("Cleared to approach");
    sb.appendLine("\t " + pattern);
    sb.appendLine("\tI\t.. ILS cat I");
    sb.appendLine("\tII\t.. ILS cat II");
    sb.appendLine("\tIII\t.. ILS cat III");
    sb.appendLine("\tR\t.. VOR/DME");
    sb.appendLine("\tG\t.. GPS");
    sb.appendLine("\tV\t.. visual");
    sb.appendLine("Example:");
    sb.appendLine("\t C I 24 \t - cleared ILS category I 24");
    sb.appendLine("\t C R 24 \t - cleared VOR/DME 24");
    sb.appendLine("\t C V 24 \t - cleared visual 24");

    return sb.toString();
  }

  @Override
  String[] getPrefixes() {
    return prefixes;
  }

  @Override
  String getPattern() {
    return pattern;
  }

  @Override
  Command parse(RegexGrouper rg) {
    String typeS = rg.getString(1);
    String runwayName = rg.getString(2);

    Approach.eType type;
    switch (typeS) {
      case "G":
        type = Approach.eType.GNSS;
        break;
      case "I":
        type = Approach.eType.ILS_I;
        break;
      case "II":
        type = Approach.eType.ILS_II;
        break;
      case "III":
        type = Approach.eType.ILS_III;
        break;
      case "N":
        type = Approach.eType.NDB;
        break;
      case "R":
        type = Approach.eType.VORDME;
        break;
      case "V":
        type = Approach.eType.Visual;
        break;
      default:
        throw new ENotSupportedException();
    }

    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(runwayName);
    if (rt == null) {
      throw new EInvalidCommandException(
          "Cannot be cleared to approach. There is no runway name \"" + runwayName + "\".", rg.getMatch());
    }

    Approach app = rt.tryGetApproachByTypeWithILSDerived(type);
    if (app == null) {
      throw new EInvalidCommandException(
          "Cannot be cleared to approach. There is no approach type "
              + type + " for runway " + rt.getName(),
          rg.getMatch());
    }

    Command ret = new ClearedToApproachCommand(app);
    return ret;
  }
}
