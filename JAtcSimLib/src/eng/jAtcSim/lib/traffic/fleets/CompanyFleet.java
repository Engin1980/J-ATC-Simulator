package eng.jAtcSim.lib.traffic.fleets;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.global.ECollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CompanyFleet extends ArrayList<FleetType>{
  public String icao;

  private double fleetWeightSum = -1;
  private Map<Character, Double> categoryFleetWeightSum = null;

  public FleetType tryGetRandom() {
    FleetType ret = null;

    if (fleetWeightSum < 0) updateFleetWeightSum();

    double tmp = Acc.rnd().nextDouble(this.fleetWeightSum);
    for (int i = 0; i < this.size(); i++) {
      FleetType ft = this.get(i);
      if (tmp < ft.weight){
        ret = ft;
        break;
      } else {
        tmp -= ft.weight;
      }
    }

    assert ret != null;
    return ret;
  }

  private void updateFleetWeightSum() {
    this.fleetWeightSum = ECollections.sum(this, o->o.weight);
  }

  public FleetType tryGetRandomByCategory(char category) {
    FleetType ret = null;
    if (categoryFleetWeightSum == null) updateCategoryWeightSum();

    double catWeight = this.categoryFleetWeightSum.get(category);
    if (catWeight == 0)
      return null; // no types for category

    double tmp = Acc.rnd().nextDouble(catWeight);
    for (int i = 0; i < this.size(); i++) {
      FleetType ft = this.get(i);
      if (ft.getAirplaneType().category != category) continue;
      if (tmp < ft.weight){
        ret = ft;
        break;
      } else {
        tmp -= ft.weight;
      }
    }

    assert ret != null;
    return ret;
  }

  private void updateCategoryWeightSum() {
    this.categoryFleetWeightSum = new HashMap<>();
    this.categoryFleetWeightSum.put('A', 0d);
    this.categoryFleetWeightSum.put('B', 0d);
    this.categoryFleetWeightSum.put('C', 0d);
    this.categoryFleetWeightSum.put('D', 0d);

    for (FleetType fleetType : this) {
      double tmp =
          this.categoryFleetWeightSum.get(fleetType.getAirplaneType().category) +
              fleetType.weight;
      this.categoryFleetWeightSum.put(fleetType.getAirplaneType().category, tmp);
    }
  }
}
