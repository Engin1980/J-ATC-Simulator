package eng.jAtcSim.newLib.traffic.models;

import eng.eSystem.ERandom;
import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.SharedFactory;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.models.base.DayGeneratedTrafficModel;
import eng.jAtcSim.newLib.traffic.movementTemplating.CategoryMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import java.util.Arrays;

public class SimpleGenericTraffic extends DayGeneratedTrafficModel {

  private final double probabilityOfNonCommercialFlight;
  private final double probabilityOfDeparture; // 0-1
  private final int[] movementsPerHour; // int[24]
  private final double[] probabilityOfCategory = new double[4]; //A,B,C,D
  private char[] orderedCategoriesByProbabilityDesc = null;
  private ERandom rnd = SharedFactory.getRnd();

  public SimpleGenericTraffic(int[] movementsPerHour,
                              double probabilityOfDeparture, double probabilityOfNonCommercialFlight,
                              double trafficCustomWeightTypeA, double trafficCustomWeightTypeB, double trafficCustomWeightTypeC, double trafficCustomWeightTypeD,
                              double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
    super(delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);

    EAssert.isNotNull(movementsPerHour);
    EAssert.isTrue(movementsPerHour.length == 24);
    for (int i : movementsPerHour) {
      EAssert.isTrue(i >= 0, new IllegalArgumentException("Argument \"movementsPerHour\" must have all elements equal or greater than 0."));
    }
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1));

    this.movementsPerHour = Arrays.copyOf(movementsPerHour, 24);
    this.probabilityOfDeparture = probabilityOfDeparture;
    this.probabilityOfNonCommercialFlight = probabilityOfNonCommercialFlight;

    // category probabilities init
    {
      double sum = trafficCustomWeightTypeA + trafficCustomWeightTypeB + trafficCustomWeightTypeC + trafficCustomWeightTypeD;
      probabilityOfCategory[0] = trafficCustomWeightTypeA / sum;
      probabilityOfCategory[1] = trafficCustomWeightTypeB / sum;
      probabilityOfCategory[2] = trafficCustomWeightTypeC / sum;
      probabilityOfCategory[3] = trafficCustomWeightTypeD / sum;
    }

    this.orderedCategoriesByProbabilityDesc = new char[4];
    this.fillOrderedCategories();
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

  private void fillOrderedCategories() {
    IList<Tuple<Character, Double>> tmp = new EList<>();
    tmp.add(new Tuple<>('A', probabilityOfCategory[0]));
    tmp.add(new Tuple<>('B', probabilityOfCategory[1]));
    tmp.add(new Tuple<>('C', probabilityOfCategory[2]));
    tmp.add(new Tuple<>('D', probabilityOfCategory[3]));
    tmp.sort(q -> -q.getB());

    for (int i = 0; i < tmp.size(); i++) {
      this.orderedCategoriesByProbabilityDesc[i] = tmp.get(i).getA();
    }
  }

  private CategoryMovementTemplate generateMovement(int hour) {
    ETimeStamp initTime = new ETimeStamp(hour, rnd.nextInt(0, 60), rnd.nextInt(0, 60));
    MovementTemplate.eKind kind = (rnd.nextDouble() <= this.probabilityOfDeparture) ? MovementTemplate.eKind.departure : MovementTemplate.eKind.arrival;
    boolean isNonCommercial = rnd.nextDouble() < this.probabilityOfNonCommercialFlight;
    char category = getRandomCategory();
    int delayInMinutes = generateDelayMinutes();
    int entryRadial = rnd.nextInt(360);

    CategoryMovementTemplate ret = new CategoryMovementTemplate(
        category, !isNonCommercial, kind, initTime, delayInMinutes, new EntryExitInfo(entryRadial)
    );
    return ret;
  }

  private char getRandomCategory() {
    char ret = 'A';
    double sum = 0;
    for (double v : probabilityOfCategory) {
      sum += v;
    }
    double tmp = rnd.nextDouble(sum);
    int index = -1;
    while (index < probabilityOfCategory.length) {
      index++;
      if (tmp < probabilityOfCategory[index]) {
        ret = (char) ((int) ret + index);
        break;
      } else {
        tmp -= probabilityOfCategory[index];
      }
    }
    return ret;
  }
}
