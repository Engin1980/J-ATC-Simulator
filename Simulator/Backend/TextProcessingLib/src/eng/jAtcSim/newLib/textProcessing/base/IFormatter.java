package eng.jAtcSim.newLib.textProcessing.base;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public interface IFormatter {
  String format(ISpeech speech);

  default String formatMessageParticipant(Participant participant){
    EAssert.Argument.isNotNull(participant);
    return participant.getId();
  }

  default String format(Participant source, Participant target, ISpeech speech) {
    EAssert.Argument.isNotNull(source);
    EAssert.Argument.isNotNull(target);
    String sb = this.formatMessageParticipant(source) +
        " => " +
        this.formatMessageParticipant(target) +
        ": " +
        this.format(speech);
    return sb;
  }
  //String format(Atc sender, PlaneSwitchMessage msg);
}
