package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.Acc;

public class AirplaneDataFormatter {

  public static String callsignS(Callsign value) {
    return value.toString();
  }

  public static String formatSqwk(Squawk value) {
    return value.toString();
  }

  public static String formatTypeCategory(AirplaneType value) {
    return Character.toString(value.category);
  }

  public static String formatHeadingLong(double value) {
    return String.format("%1$03d", (int) value);
  }

  public static String formatHeadingShort(double value) {
    return Integer.toString((int) value);
  }

  public static String formatSpeedLong(double value) {
    return ((int) value) + " kt";
  }

  public static String formatSpeedShort(double value) {
    return Integer.toString((int) value);
  }

  public static String formatSpeedAligned(double value) {
    return String.format("%1# 3d", (int) value);
  }

  public static String formatAltitudeLong(int value) {
    return Acc.toAltS(value, true);
  }

  public static String formatAltitudeLong(int value, boolean appendFt) {
    return Acc.toAltS(value, appendFt);
  }

  public static String formatAltitudeInFt(int value, boolean addFt) {
    String ret = Integer.toString(value);
    if (addFt)
      ret += "ft";
    return ret;
  }

  public static String formatAltitudeShort(int value, boolean fixedLength) {
    String ret;
    if (fixedLength)
      ret = String.format("%03d", value / 100);
    else
      ret = Integer.toString(value / 100);
    return ret;
  }

  public static String altitudeSFixed(int value) {
    return String.format("%1$03.0f", value / 100);
  }

  public static String formatVerticalSpeedLong(double value) {
    return ((int) value) + " ft/m";
  }

  public static String formatVerticalSpeedShort(double value) {
    return Integer.toString((int) value);
  }

  public static String getClimbDescendChar(double verticalSpeed) {
    if (verticalSpeed > 100) {
      return "↑"; //"▲";
    } else if (verticalSpeed < -100) {
      return "↓"; // "▼";
    } else {
      return "=";
    }
  }

  public static String getDepartureArrivalChar(boolean isDeparture) {
    if (isDeparture) {
      return "▲";
    } else {
      return "▼";
    }
  }
}
