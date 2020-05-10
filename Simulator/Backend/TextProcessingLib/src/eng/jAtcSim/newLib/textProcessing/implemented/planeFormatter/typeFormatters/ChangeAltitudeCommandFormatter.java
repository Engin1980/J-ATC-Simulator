package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Format;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ChangeAltitudeCommandFormatter extends TextSpeechFormatter<ChangeAltitudeCommand> {
  @Override
  public Class<? extends ChangeAltitudeCommand> getSourceType() {
    return ChangeAltitudeCommand.class;
  }

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
