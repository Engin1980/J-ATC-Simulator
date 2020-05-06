package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.EList;

import java.util.Arrays;

/**
 * @author Marek
 */
public class Squawk {

  public static Squawk create(String code) {
    char[] c = code.toCharArray();
    Squawk ret = Squawk.create(c);
    return ret;
  }

  public static Squawk create(char[] code) {
    Squawk ret = new Squawk(code);
    return ret;
  }

  public static Squawk generate() {
    int len = 4;
    char[] tmp = new char[len];

    // 1st cannot be 7
    tmp[0] = EList.of('0', '1', '2', '3', '4', '5', '6').getRandom();
    for (int i = 1; i < len; i++) {
      tmp[i] = EList.of('0', '1', '2', '3', '4', '5', '6', '7').getRandom();
    }

    Squawk ret = Squawk.create(tmp);
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
  private final char[] code;

  private Squawk() {
    this(new char[]{'7', '7', '7', '7'});
  }

  private Squawk(char[] code) {
    this.code = code;
    checkSanity();
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

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Arrays.hashCode(this.code);
    return hash;
  }

  @Override
  public String toString() {
    return new String(code);
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

}
