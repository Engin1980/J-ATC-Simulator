/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.frmPacks.simple;

import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import java.awt.event.KeyListener;
import javax.swing.JTextField;

/**
 *
 * @author Marek
 */
public class CommandJTextField extends JTextField {

  public CommandJTextField() {
    super.addKeyListener(new EMyKeyListener(this));
  }

}

class EMyKeyListener implements KeyListener {

  private final JTextField parent;

  public EMyKeyListener(JTextField parent) {
    this.parent = parent;
  }
  
  @Override
  public void keyTyped(KeyEvent e) {
    if (e.getKeyCode() == VK_ESCAPE){
      parent.setText("");
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }
  
}
