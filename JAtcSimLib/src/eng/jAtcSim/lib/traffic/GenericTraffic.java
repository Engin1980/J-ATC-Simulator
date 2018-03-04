/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.traffic.fleets.CompanyFleet;
import eng.jAtcSim.lib.traffic.fleets.Fleets;

/**
 * @author Marek Vajgl
 */
public class GenericTraffic extends Traffic {

  private static final String[] COMPANIES = new String[]{"CSA", "EZY", "BAW", "RYR"};
  private static final String[] PLANE_COUNTRY_CODES = new String[]{"OK", "OM"};

  private final double probabilityOfNonCommercialFlight = 0;

  /**
   * Probability that generated plane is departure (arrival otherwise), range 0.0-1.0 .
   */
  private final double probabilityOfDeparture;

  /**
   * Specifies number of movements for each hour. This is 24 item array, where for each hour number movements is
   * defined.
   */
  private final int[] movementsPerHour = new int[24];

  /**
   * Specifies probability thresholds of each category. By indices 0, 1, 2, 3 for categories A, B, C, D.
   */
  private final double[] probabilityOfCategory = new double[4];



  private int nextHourToGenerateTraffic = -1;


  public GenericTraffic(int movementsPerHour, double probabilityOfDeparture,
                        int trafficCustomWeightTypeA, int trafficCustomWeightTypeB, int trafficCustomWeightTypeC, int trafficCustomWeightTypeD,
                        boolean useExtendedCallsigns) {

    if (movementsPerHour < 0) {
      throw new IllegalArgumentException("Argument \"movementsPerHour\" must be equal or greater than 0.");
    }

    if (eng.eSystem.Number.isBetweenOrEqual(0, probabilityOfDeparture, 1) == false) {
      throw new IllegalArgumentException("\"probabilityOfDeparture\" must be between 0 and 1.");
    }

    for (int i = 0; i < this.movementsPerHour.length; i++) {
      this.movementsPerHour[i] = movementsPerHour;
    }
    this.probabilityOfDeparture = probabilityOfDeparture;

    // category probabilities init
    {
      double sum = trafficCustomWeightTypeA + trafficCustomWeightTypeB + trafficCustomWeightTypeC + trafficCustomWeightTypeD;
      probabilityOfCategory[0] = trafficCustomWeightTypeA / sum;
      probabilityOfCategory[1] = trafficCustomWeightTypeB / sum;
      probabilityOfCategory[2] = trafficCustomWeightTypeC / sum;
      probabilityOfCategory[3] = trafficCustomWeightTypeD / sum;
    }

    super.setUseExtendedCallsigns(useExtendedCallsigns);
  }



  @Override
  public void generateNewMovementsIfRequired() {
    if (nextHourToGenerateTraffic != -1 && Acc.now().getHours() != nextHourToGenerateTraffic) {
      return;
    }

    int currentHour = Acc.now().getHours();
    int expMovs = movementsPerHour[currentHour];
    for (int i = 0; i < expMovs; i++) {
      Movement m = generateMovement(currentHour);
      super.addScheduledMovement(m);
    }
    nextHourToGenerateTraffic = currentHour + 1;
    if (nextHourToGenerateTraffic > 23) {
      nextHourToGenerateTraffic = 0;
    }
  }

  private char getRandomCategory() {
    char ret = 'A';
    double sum = 0;
    for (double v : probabilityOfCategory) {
      sum += v;
    }
    double tmp = Acc.rnd().nextDouble(sum);
    int index = -1;
    while (index < probabilityOfCategory.length){
      index++;
      if (tmp < probabilityOfCategory[index]){
        ret = (char) ((int)ret + index);
      }else {
        tmp -= probabilityOfCategory[index];
      }
    }
    return ret;
  }

  private Movement generateMovement(int hour) {

    ETime initTime = new ETime(hour, Acc.rnd().nextInt(0, 60), Acc.rnd().nextInt(0, 60));
    boolean isDeparture = (Acc.rnd().nextDouble() <= this.probabilityOfDeparture);
    boolean isNonCommercial = Acc.rnd().nextDouble() > this.probabilityOfNonCommercialFlight;
    String prefix;
    if (isNonCommercial)
      prefix = this.PLANE_COUNTRY_CODES[Acc.rnd().nextInt(this.PLANE_COUNTRY_CODES.length)];
    else
      prefix = this.COMPANIES[Acc.rnd().nextInt(this.COMPANIES.length)];
    Callsign cls = generateCallsign(prefix, isNonCommercial);

    int delayInMinutes = generateDelayMinutes();

    AirplaneType type;
    char category = getRandomCategory();
    if (isNonCommercial) {
      type = Acc.sim().getPlaneTypes().getRandomFromCategory(category);
    } else{
      type = getTypeByCategoryAndCompany(prefix, category);
    }

    Movement ret = new Movement(cls, type, initTime, delayInMinutes, isDeparture);
    return ret;

  }

  private AirplaneType getTypeByCategoryAndCompany(String companyCode, char category) {
    CompanyFleet cf = super.getFleets().tryGetByIcao(companyCode);
    if (cf == null) cf = Fleets.getDefaultCompanyFleet();

    AirplaneType ret = cf.tryGetRandomByCategory(category).getAirplaneType();
    if (ret == null)
      ret = cf.tryGetRandom().getAirplaneType();

    return ret;
  }

}

