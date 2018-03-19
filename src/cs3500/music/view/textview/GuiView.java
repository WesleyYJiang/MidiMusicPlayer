package cs3500.music.view.textview;

import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import cs3500.music.view.IView;

/**
 * Class represents the interface (GuiView) to be used by
 * both the Visual and the Composite views. These methods
 * are used to add additional functionality to these two views.
 */
public interface GuiView extends IView {
  /**
   * Sets the current beat to the end of the song.
   */
  void toEnd();

  /**
   * Sets the current beat to the beginning of the song.
   */
  void toBeginning();


  /**
   * Toggles play and pause. If true, play the music and sync the current beat with the song,
   * if false don't play anything.
   */
  void togglePlay();

  /**
   * Reset the focus on the appropriate part of the view that has the keyboard listener attached
   * to it, so that keyboard events will still flow through.
   */
  void resetFocus();

  /**
   * Signal the view to draw itself.
   */
  void refresh();


  /**
   * this is to force the view to have a method to set up the keyboard.
   * The name has been chosen deliberately. This is the same method signature to
   * add a key listener in Java Swing. <p>
   *
   * </p>
   * Thus our Swing-based implementation of this interface will already have such a
   * method.
   */
  void addKeyListener(KeyListener listener);


  /**
   * this is to force the view to have a method to set up the Mouse.
   * The name has been chosen deliberately. This is the same method signature to
   * add a key listener in Java Swing.<p>
   *
   * </p>
   * Thus our Swing-based implementation of this interface will already have such a
   * method.
   */
  void addMouseListener(MouseListener listener);


  /**
   * Adds the note to the view determined by the given MouseEvent location.
   * @param e Represents the MouseEvent and its characteristics.
   * @param duration
   */
  void addNote(MouseEvent e, int duration);

  /**
   * Moves the panel to the location of the current beat. This is used mainly
   * when pressing space to pause or play the editor.
   */
  void movePanel();



  //TODO:
  void increaseTempo();

  //TODO:
  void decreaseTempo();

  //TODO:
  void togglePractice();

  void startCreate();

  void addBeginRepeat();

  void addEndRepeat();

  void addDoubleRepeat();

  void addTimebar();
}
