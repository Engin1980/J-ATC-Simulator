package eng.jAtcSim.newLib.textProcessing.formatting;

import eng.jAtcSim.newLib.area.atcs.Atc;
import eng.jAtcSim.newLib.area.speaking.ISpeech;
import eng.jAtcSim.newLib.area.speaking.fromAtc.atc2atc.PlaneSwitchMessage;

public interface IFormatter {
  String format(ISpeech speech);
  String format(Atc sender, PlaneSwitchMessage msg);
}
