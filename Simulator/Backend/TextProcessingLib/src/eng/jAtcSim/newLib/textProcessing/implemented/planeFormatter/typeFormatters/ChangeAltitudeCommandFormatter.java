package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters;

import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;

public class ChangeAltitudeCommandFormatter extends SmartTextSpeechFormatter<ChangeAltitudeCommand> {

  @Override
  public String format(ChangeAltitudeCommand cmd) {
    StringBuilder sb = new StringBuilder();
    switch (cmd.getDirection()) {
      case any:
        break;
      case climb:
        sb.append("climb and maintain ");
        break;
      case descend:
        sb.append("descend and maintain ");
        break;
      default:
        throw new UnsupportedOperationException();
    }
    sb.append(Format.Altitude.toAlfOrFLLong(cmd.getAltitudeInFt()));
    return sb.toString();
  }
}
