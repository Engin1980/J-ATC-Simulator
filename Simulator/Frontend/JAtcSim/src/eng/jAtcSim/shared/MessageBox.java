/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.shared;

import javax.swing.*;

/**
 * @author Marek Vajgl
 */
public class MessageBox {

  public static void show(String infoMessage, String titleBar) {
    JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
  }

}
