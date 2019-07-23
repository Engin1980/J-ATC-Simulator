//package eng.jAtcSim.lib.world.approachesOld;
//
//
//import eng.eSystem.xmlSerialization.annotations.XmlOptional;
//
//public class GnssApproach extends Approach {
//
//  private int daA;
//  private int daB;
//  private int daC;
//  private int daD;
//  @XmlOptional
//  private double glidePathPercentage = 3;
//
//  public double getGlidePathPercentage() {
//    return glidePathPercentage;
//  }
//
//  public int getDA(char category) {
//    switch (category) {
//      case 'A':
//        return daA;
//      case 'B':
//        return daB;
//      case 'C':
//        return daC;
//      case 'D':
//        return daD;
//      default:
//        throw new UnsupportedOperationException();
//    }
//  }
//
//  @Override
//  public String getTypeString() {
//    return "GNSS";
//  }
//
//  @Override
//  protected void _bind() {
//  }
//}
