package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.*;

public class Format {
  //TODO fix this to be get from getActiveAirport().getTransitionAltitude()
  private static final int TRANSITION_ALTITUDE = 5000;

  public static class Altitude{

    /**
     * Converts altitude to "# ft" or "FL###" according to the altitude and transition level.
     * @param altitude
     * @return
     */
    public static String toStandardAltitudeOrFL(double altitude){
      if (altitude < TRANSITION_ALTITUDE)
        return String.format("%.0f ft", altitude);
      else
        return String.format("FL%03d", toFL(altitude));
    }

    private static int toFL(double altitude){
      return (int) (altitude / 100);
    }
  }

  public static class Heading{
    public static String to(int heading){
      return String.format("%03d", heading);
    }
  }

  public static class Distance{
    public static String to(double distance){
      return String.format("%.2f nm", distance);
    }
  }

  public static class Speed{
    public static String to(int speed){
      return String.format("%d kt", speed);
    }
  }

  public static class Frequency{
    public static String to(double frequency){
      return String.format("%5.2f", frequency);
    }
  }

  public static String formatAltitudeOrFlightLevel(int altInFt, boolean appendFt){
    if (altInFt > TRANSITION_ALTITUDE) {
      return String.format("FL%03d", ((int) altInFt) / 100);
    } else {
      if (appendFt) {
        return String.format("%d ft", (int) altInFt);
      } else {
        return String.format("%d", (int) altInFt);
      }
    }
  }

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
