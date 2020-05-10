package eng.jAtcSim.newLib.speeches.system.user2system;

import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

public class TickSpeedRequest implements ISystemUserRequest {
  public static TickSpeedRequest createGet() {
    return new TickSpeedRequest(null);
  }

  public static TickSpeedRequest createSet(int interval) {
    return new TickSpeedRequest(interval);
  }
  private final Integer value;


  private TickSpeedRequest(Integer value) {
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }
}
