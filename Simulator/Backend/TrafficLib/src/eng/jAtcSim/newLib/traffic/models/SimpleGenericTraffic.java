package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.SharedInstanceProvider;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralAviationMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.GeneralCommercialMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.util.Arrays;

public class SimpleGenericTraffic extends DayGeneratedTrafficModel {

  private final double probabilityOfNonCommercialFlight;
  private final double probabilityOfDeparture; // 0-1
  private final int[] movementsPerHour; // int[24]
  private ERandom rnd = SharedInstanceProvider.getRnd();

  public SimpleGenericTraffic(int[] movementsPerHour,
                              double probabilityOfDeparture, double probabilityOfNonCommercialFlight) {
    EAssert.isNotNull(movementsPerHour);
    EAssert.isTrue(movementsPerHour.length == 24);
    for (int i : movementsPerHour) {
      EAssert.isTrue(i >= 0, new IllegalArgumentException("Argument \"movementsPerHour\" must have all elements equal or greater than 0."));
    }
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1));

    this.movementsPerHour = Arrays.copyOf(movementsPerHour, 24);
    this.probabilityOfDeparture = probabilityOfDeparture;
    this.probabilityOfNonCommercialFlight = probabilityOfNonCommercialFlight;
  }

  @Override
  public IReadOnlyList<MovementTemplate> generateMovementsForOneDay() {
    IList<MovementTemplate> ret = new EList<>();

    for (int i = 0; i < 24; i++) {
      int cnt = movementsPerHour[i];
      for (int c = 0; c < cnt; c++) {
        MovementTemplate tmp = generateMovement(i);
        ret.add(tmp);
      }
    }

    return ret;
  }

  private MovementTemplate generateMovement(int hour) {
    ETimeStamp initTime = new ETimeStamp(hour, rnd.nextInt(0, 60), rnd.nextInt(0, 60));
    MovementTemplate.eKind kind = (rnd.nextDouble() <= this.probabilityOfDeparture) ?
        MovementTemplate.eKind.departure : MovementTemplate.eKind.arrival;
    boolean isNonCommercial = rnd.nextDouble() < this.probabilityOfNonCommercialFlight;
    int radial = rnd.nextInt(360);

    MovementTemplate ret;
    if (isNonCommercial)
      ret = new GeneralAviationMovementTemplate(kind, initTime,new EntryExitInfo(radial));
    else
      ret = new GeneralCommercialMovementTemplate(null, null,
          kind, initTime, new EntryExitInfo(radial));
    return ret;
  }
}
