//TODEL
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eng.jAtcSim.newLib.shared;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Deprecated
///**
// *
// * @author Marek
// * @see eng.eSystem.utilites.RegexUtils.RegexGroups
// */
//public class RegexGrouper {
//
//  private final Matcher m;
//
//  public static RegexGrouper apply(String data, String pattern) {
//    Pattern p = Pattern.compile("^" + pattern);
//    RegexGrouper ret = new RegexGrouper(p.matcher(data));
//    if (ret.m.find() == false) {
//      ret = null;
//    }
//
//    return ret;
//  }
//
//  public RegexGrouper(Matcher m) {
//    this.m = m;
//  }
//
//  public int getIndexOfCharacterAfterMatch() {
//    return m.end();
//  }
//
//  public String getMatch() {
//    return m.group(0);
//  }
//
//  public int getInt(int groupIndex) {
//    return Integer.parseInt(m.group(groupIndex));
//  }
//
//  public double getDouble(int groupIndex) {
//    return Double.parseDouble(m.group(groupIndex));
//  }
//
//  public String getString(int groupIndex) {
//    return (String) m.group(groupIndex);
//  }
//
//  public String tryGetString(int groupIndex) {
//    if (groupIndex <= m.groupCount() && m.group(groupIndex) != null) {
//      return getString(groupIndex);
//    } else {
//      return null;
//    }
//  }
//
//  public Integer tryGetInt(int groupIndex) {
//    if (groupIndex < m.groupCount() && m.group(groupIndex) != null) {
//      return getInt(groupIndex);
//    } else {
//      return null;
//    }
//  }
//}
