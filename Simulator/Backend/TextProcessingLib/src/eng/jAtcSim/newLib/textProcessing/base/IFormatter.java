package eng.jAtcSim.newLib.textProcessing.base;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.messaging.IMessageParticipant;
import eng.jAtcSim.newLib.speeches.ISpeech;

public interface IFormatter {
  String format(ISpeech speech);

  default String formatMessageParticipant(IMessageParticipant participant){
    EAssert.Argument.isNotNull(participant);
    return participant.getName();
  }

  default String format(IMessageParticipant source, IMessageParticipant target, ISpeech speech) {
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
