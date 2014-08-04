/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes;

import java.util.Objects;

/**
 *
 * @author Marek
 */
public class Callsign {
  private final String company;
  private final String number;

  public Callsign(String company, String number) {
    this.company = company;
    this.number = number;
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
  
}
