/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.app.extenders;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.JTextField;

/**
 *
 * @author Marek Vajgl
 */
public class TimeExtender {

  private final JTextField txt;
  private Color okColor;
  private Color failColor = Color.red;
  private static final String pattern = "H:mm";

  public TimeExtender(java.time.LocalTime time){
    this(new JTextField(), time);
  }

  public TimeExtender(JTextField txt, java.time.LocalTime time) {
    this.txt = txt;
    this.txt.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        checkSanity();
      }
    });
    if (time != null){
      setTime(time);
    }
    okColor = this.txt.getForeground();
    checkSanity();
  }

  private boolean checkSanity() {
    boolean ret = getTime() != null;
    if (ret){
      txt.setForeground(okColor);
    } else
    {
      if (txt.getForeground().equals(failColor) == false)
        okColor = txt.getForeground();
      txt.setForeground(failColor);
    }
    return ret;
  }

  public JTextField getControl(){
    return txt;
  }

  public void setTime(java.time.LocalTime time){
    if (time == null)
      time = LocalTime.now();
    String h = time.format(DateTimeFormatter.ofPattern(pattern));
    txt.setText(h);
  }

  public java.time.LocalTime getTime(){
    java.time.LocalTime ret;
    try{
      ret = java.time.LocalTime.parse(txt.getText(), DateTimeFormatter.ofPattern(pattern));
    } catch (Exception ex){
      ret = null;
    }
    return ret;
  }

  public boolean isValid() {
    return checkSanity();
  }
}
