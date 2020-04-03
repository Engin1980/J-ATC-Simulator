//package eng.jAtcSim.newLib.xml.traffic.internal;
//
//import eng.eSystem.collections.*;
//import eng.eSystem.validation.EAssert;
//
//import static eng.eSystem.utilites.FunctionShortcuts.*;
//
//public class ValueAndWeight {
//  private final String value;
//  private final int weight;
//
//  public static  ValueAndWeight create(String value, int weight) {
//    return new ValueAndWeight(value,weight);
//  }
//
//  private ValueAndWeight(String value, int weight) {
//    EAssert.Argument.isNonemptyString(value, "value");
//    EAssert.Argument.isTrue(weight >= 0);
//    this.value = value;
//    this.weight = weight;
//  }
//
//  public String getValue() {
//    return value;
//  }
//
//  public int getWeight() {
//    return weight;
//  }
//}
