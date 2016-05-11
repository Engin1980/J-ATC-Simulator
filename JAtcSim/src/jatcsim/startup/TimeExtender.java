/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextField;

/**
 *
 * @author Marek Vajgl
 */
public class TimeExtender {

  private JTextField txt;
  private Color okColor;

  public TimeExtender(JTextField txt) {
    if (txt == null) {
      throw new IllegalArgumentException("Argument \"txt\" cannot be null.");
    }
    this.txt = txt;
    this.okColor = txt.getForeground();
    this.txt.addKeyListener(new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        checkSanity();
      }
    });
    checkSanity();
  }

  private boolean checkSanity() {
    Pattern p = Pattern.compile("^([01]?\\d):(\\d{2})$");
    String input = txt.getText();
    Matcher m = p.matcher(input);

    if (m.find() == false) {
      txt.setForeground(Color.red);
      return false;
    } else {
      txt.setForeground(okColor);
      return true;
    }
  }

  public boolean isValid() {
    return checkSanity();
  }
}
