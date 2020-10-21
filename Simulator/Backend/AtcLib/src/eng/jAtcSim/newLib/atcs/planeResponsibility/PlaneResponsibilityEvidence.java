//package eng.jAtcSim.newLib.atcs.planeResponsibility;
//
//import eng.eSystem.collections.EMap;
//import eng.eSystem.collections.IMap;
//import eng.eSystem.validation.EAssert;
//import eng.jAtcSim.newLib.shared.AtcId;
//import eng.jAtcSim.newLib.shared.Squawk;
//
//public class PlaneResponsibilityEvidence {
//
//  private final IMap<Squawk, AtcId> current = new EMap<>();
//  private final IMap<Squawk, AtcId> previous = new EMap<>();
//
//  public AtcId getResponsibleAtcId(Squawk squawk, boolean includePrevious) {
//    AtcId ret = current.tryGet(squawk);
//    if (ret == null && includePrevious)
//      ret = previous.tryGet(squawk);
//    return ret;
//  }
//
//  public void register(AtcId sender, Squawk squawk) {
//    EAssert.Argument.isNotNull(sender, "sender");
//    EAssert.Argument.isNotNull(squawk, "squawk");
//    previous.tryRemove(squawk);
//    current.set(squawk, sender);
//  }
//
//  public void unregister(AtcId sender, Squawk squawk) {
//    EAssert.isTrue(sender.equals(current.get(squawk)));
//    previous.set(squawk, sender);
//    current.remove(squawk);
//  }
//
//}
