package eng.jAtcSim.sharedLib;

public class UnitProvider {
  public static double ftToNm(double value) {
    return value / 6077.1;
  }

  public static double kmToNM(double value) {
    return value * 0.539968;
  }

  public static double mToNM(double value) {
    return UnitProvider.kmToNM(value / 1000d);
  }

  public static double nmToFt(double value) {
    return value * 6076.1;
  }

  public static double nmToKm(double value) {
    return value * 1.85196;
  }
}
