/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.shared;

import java.util.Objects;

public class Callsign {

  private final String company;
  private final String number;

  public Callsign(String company, String number) {
    this.company = company;
    this.number = number;
  }

  public Callsign(String value) {
    if (value == null)
      throw new IllegalArgumentException("Callsign string cannot be null");
    if (value.length() < 4)
      throw new IllegalArgumentException("Callsign string must be at least 4 chars long.");

    this.company = value.substring(0, 3).toUpperCase();
    this.number = value.substring(3).trim().toUpperCase();
  }

//  public static String getRandomNumber(boolean isIfr, boolean useExtendedCallsigns) {
//    StringBuilder sb = new StringBuilder();
//    if (isIfr) {
//      if (useExtendedCallsigns && Acc.rnd().nextDouble() < EXTENDED_CALLSIGN_PROBABILITY) {
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('A', 'Z'));
//        sb.append(getRandomChar('A', 'Z'));
//      } else {
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//        sb.append(getRandomChar('0', '9'));
//      }
//    } else {
//      sb.append(getRandomChar('A', 'Z'));
//      sb.append(getRandomChar('A', 'Z'));
//      sb.append(getRandomChar('A', 'Z'));
//    }
//    return sb.toString();
//  }
//
//  private static char getRandomChar(char from, char to) {
//    int val = Acc.rnd().nextInt(from, to + 1);
//    char ret = (char) val;
//    return ret;
//  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Callsign other = (Callsign) obj;
    if (!Objects.equals(this.company, other.company)) {
      return false;
    }
    if (!Objects.equals(this.number, other.number)) {
      return false;
    }
    return true;
  }

  public String getCompany() {
    return company;
  }

  public String getNumber() {
    return number;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.company);
    hash = 97 * hash + Objects.hashCode(this.number);
    return hash;
  }

  /**
   * Returns company and number together, without separating space, e.g. EZY5405.
   *
   * @return Company and number together
   */
  @Override
  public String toString() {
    return company + " " + number;
  }

  public String toString(boolean separateCompanyAndNumber) {
    if (separateCompanyAndNumber)
      return company + " " + number;
    else
      return company + number;
  }
}
