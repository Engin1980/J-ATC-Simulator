/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.shared;

import eng.eSystem.validation.EAssert;

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

    int indexOfDigitOrSpace = -1;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == ' ' || (c >= '0' && c <= '9')) {
        indexOfDigitOrSpace = i;
        break;
      }
    }

    this.company = value.substring(0, indexOfDigitOrSpace).trim().toUpperCase();
    this.number = value.substring(indexOfDigitOrSpace).trim().toUpperCase();

    EAssert.matchPattern(this.company, "[A-Z]+");
    EAssert.matchPattern(this.number, "[A-Z0-9]+");
  }

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
