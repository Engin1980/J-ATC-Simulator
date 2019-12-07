//package eng.jAtcSim.lib.eng.jAtcSim.lib.world.approaches;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.EMap;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IMap;
//import eng.jAtcSim.lib.eng.jAtcSim.lib.world.approaches.entryLocations.IApproachEntryLocation;
//import eng.jAtcSim.lib.eng.jAtcSim.lib.world.approaches.stages.IApproachStage;
//
//public class ApproachStageSet {
//  private IMap<Character, IList<IApproachStage>> inner = new EMap<>();
//
//  public ApproachStageSet() {
//    this.inner.set('A', new EList<>());
//    this.inner.set('B', new EList<>());
//    this.inner.set('C', new EList<>());
//    this.inner.set('D', new EList<>());
//  }
//
//  public void addToAll(IApproachStage approachStage){
//    inner.get('A').add(approachStage);
//    inner.get('B').add(approachStage);
//    inner.get('C').add(approachStage);
//    inner.get('D').add(approachStage);
//  }
//
//  public void addToAll(IApproachStage aCategoryStage, IApproachStage bCategoryStage, IApproachStage cCategoryStage, IApproachStage dCategoryStage){
//    inner.get('A').add(aCategoryStage);
//    inner.get('B').add(bCategoryStage);
//    inner.get('C').add(cCategoryStage);
//    inner.get('D').add(dCategoryStage);
//  }
//}
