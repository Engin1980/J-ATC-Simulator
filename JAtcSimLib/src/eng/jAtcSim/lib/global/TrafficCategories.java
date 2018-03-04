///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package eng.jAtcSim.lib.global;
//
//import eng.jAtcSim.lib.Acc;
//
///**
// *
// * @author Marek
// */
//public class TrafficCategories {
//  private double categoryA;
//  private double categoryB;
//  private double categoryC;
//  private double categoryD;
//
//  public double getCategoryA() {
//    return categoryA;
//  }
//
//  public double getCategoryB() {
//    return categoryB;
//  }
//
//  public double getCategoryC() {
//    return categoryC;
//  }
//
//  public double getCategoryD() {
//    return categoryD;
//  }
//
//  public char getRandomCategory() {
//    double value = Acc.rnd().nextDouble(0, categoryA + categoryB + categoryC + categoryD);
//
//    if (value < categoryA)
//      return 'A';
//    else if (value < categoryA + categoryB)
//      return 'B';
//    else if (value <categoryA  + categoryB + categoryC)
//      return 'C';
//    else
//      return 'D';
//  }
//}
