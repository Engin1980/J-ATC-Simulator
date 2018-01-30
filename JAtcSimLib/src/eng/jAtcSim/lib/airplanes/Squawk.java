/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import java.util.Arrays;

/**
 *
 * @author Marek
 */
public class Squawk {

  private final char[] code;

  public static Squawk create(String code) {
    char[] c = code.toCharArray();
    Squawk ret = Squawk.create(c);
    return ret;
  }

  public static Squawk create(char[] code) {
    Squawk ret = new Squawk(code);
    return ret;
  }

  public static Squawk tryCreate(String code) {
    char[] c = code.toCharArray();
    Squawk ret = tryCreate(c);
    return ret;
  }

  public static Squawk tryCreate(char[] code) {
    Squawk ret;
    try {
      ret = Squawk.create(code);
    } catch (Exception ex) {
      ret = null;
    }
    return ret;
  }

  private Squawk(char[] code) {
    this.code = code;
    checkSanity();
  }

  private void checkSanity() {
    if (code.length != 4) {
      throw new IllegalArgumentException("Sqwk length must be 4");
    }
    for (int i = 0; i < code.length; i++) {
      char c = code[i];
      if (c < '0' || c > '7') {
        throw new IllegalArgumentException("Sqwk length must character 0-7.");
      }
    }
  }

  @Override
  public String toString() {
    return new String(code);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Arrays.hashCode(this.code);
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
    final Squawk other = (Squawk) obj;
    if (!Arrays.equals(this.code, other.code)) {
      return false;
    }
    return true;
  }

}
