/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.FlightMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

/**
 * @author Marek Vajgl
 */
public class TestTrafficModel extends DayGeneratedTrafficModel {
  private static String AIRPLANE_TYPE = "A320";
  private static String COMPANY_NAME = "CSA";
  private int count;
  private FlightMovementTemplate.eKind kind;

  public TestTrafficModel(int countPerHour, FlightMovementTemplate.eKind kind) {
    super(delayProbability, perStepDelay, useExctendedCallsigns);
    this.count = countPerHour;
    this.kind = kind;
  }

  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      for (int c = 0; c < count; c++) {
        MovementTemplate tmp = new FlightMovementTemplate(
            new Callsign(
                TestTrafficModel.COMPANY_NAME,
                i + "0" + count),
            TestTrafficModel.AIRPLANE_TYPE,
            this.kind,
            new ETimeStamp(i, 0, 0),
            0,
            new EntryExitInfo(290)
        );
        ret.add(tmp);
      }
    }

    return ret;
  }
}
