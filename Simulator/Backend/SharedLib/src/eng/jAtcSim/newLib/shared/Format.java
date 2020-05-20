package eng.jAtcSim.newLib.shared;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;

public class Format {
  public static class Altitude {
    public static String toAlfOrFLLong(int value) {
      if (value < TRANSITION_ALTITUDE)
        return String.format("%.0d ft", value);
      else
        return String.format("FL%03d", (value / 100));
    }

    public static String toFLShort(int value) {
      return String.format("%d", value / 100);
    }

    public static String toFLLong(int value) {
      return String.format("FL%d", value / 100);
    }

    public static String toFixedFL(int value) {
      return String.format("%03d", value / 100);
    }
  }

  public static class Heading {
    public static String to(int heading) {
      return String.format("%03d", heading);
    }

    public static String toShort(int heading) {
      return Integer.toString(heading);
    }
  }

  public static class Distance {
    public static String to(double distance) {
      return String.format("%.2f nm", distance);
    }
  }

  public static class Speed {
    public static String to(int speed) {
      return toLong(speed);
    }

    public static String toLong(int speed) {
      return to(speed, "%d kt");
    }

    public static String toShort(int speed) {
      return to(speed, "%d");
    }

    private static String to(int speed, String pattern) {
      return String.format(pattern, speed);
    }

    public static String toRightAligned(int value) {
      return String.format("%1# 3d", value);
    }
  }

  public static class VerticalSpeed {
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
  }

  public static class Frequency {
    public static String to(double frequency) {
      return String.format("%5.2f", frequency);
    }
  }

  public static class PlaneInfo{
    public static String toDepartureArrivalChar(boolean isDeparture) {
      if (isDeparture) {
        return "▲";
      } else {
        return "▼";
      }
    }


  }

  public static class Flight{
    public static char getDepartureArrivalChar(DepartureArrival dir){
      switch (dir) {
        case departure:
          return '↗';
        case arrival:
          return '↘';
        default:throw new EEnumValueUnsupportedException(dir);
      }
    }
  }

  //TODO fix this to be get from getActiveAirport().getTransitionAltitude()
  private static final int TRANSITION_ALTITUDE = 5000;


//  public static String callsignS(Callsign value) {
//    return value.toString();
//  }

  public static String formatSqwk(Squawk value) {
    return value.toString();
  }

//  public static String formatTypeCategory(AirplaneType value) {
//    return Character.toString(value.category);
//  }


}
