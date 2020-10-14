package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.airplane.IForPlaneSpeech;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.system.ISystemSpeech;

public interface IUserAtcInterface {
  void sendAtcCommand(AtcId toAtcId, IAtcSpeech atcSpeech);

  void sendPlaneCommand(Callsign toCallsign, SpeechList<IForPlaneSpeech> cmds);

  void sendSystemCommand(ISystemSpeech systemSpeech);
}
