//package eng.jAtcSim.lib.world.xml;
//
//import eng.eSystem.xmlSerialization.supports.IValueParser;
//
//public class AltitudeValueParser implements IValueParser<Integer> {
//  @Override
//  public Integer parse(String s) {
//    int ret;
//    if (s.startsWith("FL")){
//      s = s.substring(2);
//      ret = Integer.parseInt(s) * 100;
//    } else {
//      ret = Integer.parseInt(s);
//    }
//    return ret;
//  }
//
//  @Override
//  public String format(Integer value) {
//    String ret;
//    if (value <= 10000){
//      ret = Integer.toString(value);
//    } else {
//      ret = "FL" + (int) (Math.ceil(value / 100d));
//    }
//    return ret;
//  }
//}
