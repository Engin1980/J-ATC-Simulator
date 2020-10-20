package eng.jAtcSim.newLib.atcs.internal.computer;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.messaging.Participant;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcConfirmation;
import eng.jAtcSim.newLib.speeches.atc.atc2user.AtcRejection;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequest;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;

class PlaneSwitchWrapper {

  public static boolean isPlaneSwitchBased(Message message) {
    PlaneSwitchRequest ps = tryGetBaseIfBasedOnPlaneSwitch(message);
    return ps != null;
  }

  public static PlaneSwitchRequest tryGetBaseIfBasedOnPlaneSwitch(Message message) {
    if (message.getContent() instanceof PlaneSwitchRequest)
      return message.getContent();
    else if (message.getContent() instanceof AtcConfirmation)
      return (PlaneSwitchRequest) ((AtcConfirmation) message.getContent()).getOrigin();
    else if (message.getContent() instanceof AtcRejection)
      return (PlaneSwitchRequest) ((AtcRejection) message.getContent()).getOrigin();
    else
      return null;
  }

  private final Message message;

  public PlaneSwitchWrapper(Message message) {
    EAssert.isTrue(isPlaneSwitchBased(message));
    this.message = message;
  }

  public PlaneSwitchRequest getPlaneSwitch() {
    return tryGetBaseIfBasedOnPlaneSwitch(this.message);
  }

  public PlaneSwitchRequestRouting getRouting() {
    return getPlaneSwitch().getRouting();
  }

  public AtcId getSource() {
    Participant p = message.getSource();
    EAssert.isTrue(p.getType() == Participant.eType.atc);
    AtcId ret = Context.Internal.getAtc(p.getId()).getAtcId();
    return ret;
  }

  public Squawk getSquawk() {
    return getPlaneSwitch().getSquawk();
  }

  public AtcId getTarget() {
    Participant p = message.getTarget();
    EAssert.isTrue(p.getType() == Participant.eType.atc);
    AtcId ret = Context.Internal.getAtc(p.getId()).getAtcId();
    return ret;
  }
}
