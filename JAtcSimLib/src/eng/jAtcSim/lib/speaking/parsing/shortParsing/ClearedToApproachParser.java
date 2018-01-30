package eng.jAtcSim.lib.speaking.parsing.shortParsing;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.exceptions.EInvalidCommandException;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.EStringBuilder;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.world.Approach;
import eng.jAtcSim.lib.world.RunwayThreshold;
import jatcsimlib.Acc;
import jatcsimlib.exceptions.EInvalidCommandException;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.ClearedToApproachCommand;
import jatcsimlib.world.Approach;
import jatcsimlib.world.RunwayThreshold;

class ClearedToApproachParser extends SpeechParser<ClearedToApproachCommand> {

  private static final String[] prefixes = new String[]{"C "};
  private static final String pattern = "C (I|II|III|G|V|R|N) (\\S+)";

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
  ClearedToApproachCommand parse(RegexGrouper rg) {
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

    //TODO this should be checked by the pilot, not by the
    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(runwayName);
    if (rt == null) {
      throw new EInvalidCommandException(
          "Cannot be cleared to approach. There is no runway designated as \"" + runwayName + "\".", rg.getMatch());
    }

    Approach app = rt.tryGetApproachByTypeWithILSDerived(type);
    if (app == null) {
      throw new EInvalidCommandException(
          "Cannot be cleared to approach. There is no approach type "
              + type + " for runway " + rt.getName(),
          rg.getMatch());
    }

    ClearedToApproachCommand ret = new ClearedToApproachCommand(app);
    return ret;
  }
}
