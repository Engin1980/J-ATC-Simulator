/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;

/**
 * @author Marek Vajgl
 */
public class TestTraffic extends Traffic {
  public static String AIRPLANE_TYPE = "A320";
  private int count = 1;
  private FlightMovementTemplate.eKind kind;

  public TestTraffic(double delayProbability, int maxDelayInMinutesPerStep, int countPerHour, FlightMovementTemplate.eKind kind) {
    super(delayProbability, maxDelayInMinutesPerStep);
    this.count = countPerHour;
    this.kind = kind;
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    return new EList<>();
  }

  @Override
  public IReadOnlyList<FlightMovementTemplate> getMovements(ETimeStamp fromTimeInclusive, ETimeStamp toTimeExclusive) {
    IList<FlightMovementTemplate> ret = new EList<>();
    ETimeStamp time;
    if (fromTimeInclusive.getMinutes() == 0 && fromTimeInclusive.getSeconds() == 0)
      time = fromTimeInclusive;
    else
      time = new ETimeStamp(fromTimeInclusive.getDays(), fromTimeInclusive.getHours(), fromTimeInclusive.getMinutes(), fromTimeInclusive.getSeconds());
    while (time.isBefore(toTimeExclusive)) {
      for (int i = 0; i < count; i++) {
        FlightMovementTemplate tmp = new FlightMovementTemplate(
            new Callsign("CSA", time.getHours() + "0" + count),
            new AirplaneTypeDefinition(TestTraffic.AIRPLANE_TYPE), this.kind,
            new EntryExitInfo(290),
            time,
            0
        );
        ret.add(tmp);
      }
      time = time.addHours(1);
    }
    return ret;
  }
}
