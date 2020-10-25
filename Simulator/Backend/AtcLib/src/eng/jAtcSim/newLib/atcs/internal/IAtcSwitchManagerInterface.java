package eng.jAtcSim.newLib.atcs.internal;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.atcs.internal.computer.RequestResult;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.planeSwitching.PlaneSwitchRequestRouting;

public interface IAtcSwitchManagerInterface {
  AtcId getAtcId();

  IReadOnlyList<IAirplane> getPlanesUnderControl();

  boolean isPlaneReadyToSwitchToAnotherAtc(IAirplane airplane);

  AtcId getAtcIdWhereIAmSwitchingPlanes();

  boolean acceptsNewRouting(IAirplane plane, PlaneSwitchRequestRouting routing);

  void sendMessage(Message msg);

  RequestResult canIAcceptPlaneIncomingFromAnotherAtc(IAirplane plane);

  void onOutgoingPlaneSwitchCompleted(Squawk sqwk);

  void onAfterIncomingPlaneGoodDayNotificationConfirmed(Squawk sqwk);
}
