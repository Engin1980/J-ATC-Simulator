//package eng.jAtcSim.newLib.atcs.planeResponsibility;
//
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.jAtcSim.newLib.shared.AtcId;
//import eng.jAtcSim.newLib.shared.Callsign;
//
////TODO rename to something like PlaneResponsibilityList
////TODO Add some caching here?
//class PlaneResponsibilityDAO {
//  private final IList<AirplaneResponsibilityInfo> all = new EList<>();
//
//  public AirplaneResponsibilityInfo get(Callsign plane) {
//    AirplaneResponsibilityInfo ret = all.getFirst(q -> q.getPlane().equals(plane));
//    return ret;
//  }
//
//  public void add(AirplaneResponsibilityInfo ari) {
//    this.all.add(ari);
//  }
//
//  public IList<AirplaneResponsibilityInfo> getAll() {
//    return all;
//  }
//
//  public void remove(AirplaneResponsibilityInfo ari) {
//    this.all.remove(ari);
//  }
//
//  public IReadOnlyList<AirplaneResponsibilityInfo> getByAtc(AtcId atcId) {
//    IReadOnlyList<AirplaneResponsibilityInfo> ret = all.where(q -> q.getAtc().equals(atcId));
//    return ret;
//  }
//}
