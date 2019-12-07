package eng.jAtcSim.newLib.textProcessing.formatting;

import eng.jAtcSim.newLib.atcs.Atc;
import eng.jAtcSim.newLib.speaking.ISpeech;
import eng.jAtcSim.newLib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;

public interface IFormatter {
  String format(ISpeech speech);
  String format(Atc sender, PlaneSwitchMessage msg);
}
