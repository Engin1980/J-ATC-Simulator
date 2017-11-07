package jatcsim;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class JFrameThread extends Thread {

  private JFrame frame;
  private final Object LOCK = new Object();

  public JFrameThread(JFrame frame) {
    this.frame = frame;
  }

  @Override
  public void run() {

    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent arg) {
        synchronized (LOCK) {
          System.out.println("Unlocking");
          LOCK.notify();
        }
      }
    }
    );

    frame.setVisible(true);
    synchronized (LOCK) {
      try {
        System.out.println("Locking");
        LOCK.wait();
      } catch (InterruptedException ex) {
      }
    }
  }

}
