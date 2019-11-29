package eng.jAtcSim.lib.textProcessing.formatting;

import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.fromAtc.atc2atc.PlaneSwitchMessage;

public interface IFormatter {
  String format(ISpeech speech);
  String format(Atc sender, PlaneSwitchMessage msg);
}
