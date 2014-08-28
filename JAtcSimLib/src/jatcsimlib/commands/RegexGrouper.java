/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Marek
 */
public class RegexGrouper {

  private Matcher m;
  
  public static RegexGrouper apply(String data, String pattern) {
    RegexGrouper ret = new RegexGrouper();
    Pattern p = Pattern.compile("^" + pattern);
    ret.m = p.matcher(data);
    if (ret.m.find() == false){
      ret = null;
    }

    return ret;
  }
  
  public int getIndexOfCharacterAfterMatch(){
    return m.end();
  }

  public int getInt(int groupIndex) {
    return Integer.parseInt(m.group(groupIndex));
  }
  
  public double getDouble (int groupIndex){
    return Double.parseDouble(m.group(groupIndex));
  }

  public String getString(int groupIndex) {
    return (String) m.group(groupIndex);
  }
}
