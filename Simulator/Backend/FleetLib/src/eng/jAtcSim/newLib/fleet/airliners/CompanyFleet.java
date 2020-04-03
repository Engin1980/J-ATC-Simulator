package eng.jAtcSim.newLib.fleet.airliners;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.fleet.TypeAndWeight;

public class CompanyFleet {

  private final String icao;
  private final String name;
  private final IReadOnlyList<TypeAndWeight> types;
//  private final double fleetWeightSum;
//  private final IMap<Character, Double> categoryFleetWeightSum;

  public CompanyFleet(String icao, String name, IReadOnlyList<TypeAndWeight> types) {
    EAssert.Argument.isNonemptyString(icao, "icao");
    EAssert.Argument.isNonemptyString(name, "name");
    EAssert.Argument.isNotNull(types, "types");
    this.icao = icao;
    this.name = name;
    this.types = types;
//    this.fleetWeightSum = this.types.sumInt(q -> q.getWeight());
//    this.categoryFleetWeightSum = new EMap<>();
//    this.updateCategoryWeightSum();
  }

  public String getIcao() {
    return icao;
  }

  public String getName() {
    return name;
  }

//  public FleetType getRandom() {
//    FleetType ret = null;
//
//    double tmp = Acc.rnd().nextDouble(this.fleetWeightSum);
//    for (int i = 0; i < this.types.size(); i++) {
//      FleetType ft = this.types.get(i);
//      if (tmp < ft.getWeight()) {
//        ret = ft;
//        break;
//      } else {
//        tmp -= ft.getWeight();
//      }
//    }
//
//    assert ret != null;
//    return ret;
//  }

  public IReadOnlyList<TypeAndWeight> getTypes() {
    return types;
  }

  @Override
  public String toString() {
    return String.format("%s (%d types)", this.icao, this.types.size());
  }

//  public FleetType tryGetRandomByCategory(char category) {
//    FleetType ret = null;
//
//    double catWeight = this.categoryFleetWeightSum.get(category);
//    if (catWeight == 0)
//      return null; // no types for category
//
//    double tmp = Acc.rnd().nextDouble(catWeight);
//    for (int i = 0; i < this.types.size(); i++) {
//      FleetType ft = this.types.get(i);
//      if (ft.getAirplaneType().category != category) continue;
//      if (tmp < ft.getWeight()) {
//        ret = ft;
//        break;
//      } else {
//        tmp -= ft.getWeight();
//      }
//    }
//
//    assert ret != null;
//    return ret;
//  }

//  private void updateCategoryWeightSum() {
//    this.categoryFleetWeightSum.set('A', 0d);
//    this.categoryFleetWeightSum.set('B', 0d);
//    this.categoryFleetWeightSum.set('C', 0d);
//    this.categoryFleetWeightSum.set('D', 0d);
//
//    for (FleetType fleetType : this.types) {
//      double tmp =
//          this.categoryFleetWeightSum.get(fleetType.getAirplaneType().category) +
//              fleetType.getWeight();
//      this.categoryFleetWeightSum.set(fleetType.getAirplaneType().category, tmp);
//    }
//  }
}
