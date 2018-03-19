package cs3500.music.view.graphicsview;


import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import cs3500.music.mechanics.Note;
import cs3500.music.mechanics.Pitch;
import cs3500.music.mechanics.repeats.Repeat;
import cs3500.music.mechanics.repeats.RepeatType;
import cs3500.music.mechanics.repeats.TimeBar;
import cs3500.music.model.IMusicOperations;
import cs3500.music.view.midiview.MidiViewImpl;
import cs3500.music.view.textview.GuiView;


/**
 * A Class that represents both the MidiView and the GuiView.
 * Via the implementation of the Midi Receiver and Synthesizer and JFrame's methods
 * it allows the user to see the editor and the notes while also hearing them play.
 */
public class CompView extends MidiViewImpl implements GuiView {
  private final ArrayList<Integer> beats;
  private boolean play = false;
  private final GuiViewFrame guiDelegate;
  private int lastBeat;
  private boolean practicing;
  private ArrayList<String> tonesPlayed;
  protected Sequence sequenceForPratice;
  protected Sequencer sequencerForPratice;
  protected ArrayList<Integer> repeatBeats;
  protected HashMap<Integer, Repeat> repeats;
  private int currentEnding;


  /**
   * Builds a MidiViewImpl.
   * The MidiView has an IMusicOperation, the last beat in the model, a synthesizer,
   * a receiver, and the starting beats.
   *
   * @param op Represents the model to read from.
   * @throws MidiUnavailableException throws an exception if the midi fails.
   */
  public CompView(IMusicOperations op) throws MidiUnavailableException {
    super(op, MidiSystem.getSequencer(), false);
    this.tonesPlayed = new ArrayList<>();
    this.guiDelegate = new GuiViewFrame(op);
    this.beats = op.getStartingBeats();
    this.currentEnding = 1;
    Track t = this.sequence.createTrack();
    MetaMessage tick = new MetaMessage();
    this.lastBeat = this.op.lastBeat();
    for (int i = 0; i <= lastBeat; i++) {
      MidiEvent tic = new MidiEvent(tick, i);
      t.add(tic);
    }
    sequencer.addMetaEventListener(new Refresh());
    try {
      this.sequencerForPratice = MidiSystem.getSequencer();
      sequenceForPratice = new Sequence(Sequence.PPQ, 1);
      sequencerForPratice.setTempoInMPQ(this.tempo);
    } catch (InvalidMidiDataException e) {
      // failed to use mid
    }
    try {
      sequencerForPratice.open();
    } catch (MidiUnavailableException e) {
      // failed to connect to mid
    }
    this.repeatBeats = op.repeatBeats();
    this.repeats = op.getRepeats();
  }

  public void update() {
    Track t = this.sequence.createTrack();
    MetaMessage tick = new MetaMessage();
    this.lastBeat = this.op.lastBeat();
    for (int i = 0; i <= lastBeat; i++) {
      MidiEvent tic = new MidiEvent(tick, i);
      t.add(tic);
    }
//
//    Track r = this.sequence.createTrack();
//    byte[] dum = {};
//    MetaMessage rMessage = new MetaMessage();
//    try {
//      rMessage.setMessage(1,dum, dum.length);
//    } catch (InvalidMidiDataException e) {
//      e.printStackTrace();
//    }
//    for (int b : this.repeatBeats) {
//      MidiEvent tic = new MidiEvent(rMessage, b);
//      r.add(tic);
//    }
  }

  @Override
  public void initialize() {
    this.guiDelegate.initialize();

    for (int i : beats) {
      for (Note n : op.getNotes(i).values()) {
        try {
          super.playNote(n.getTone(), n.getDuration(), i, n.getVolume(), n.getInstrument() - 1);
        } catch (InvalidMidiDataException e) {
          //failed to init Midi
        }
      }
    }

    try {
      sequencer.setSequence(sequence);
    } catch (InvalidMidiDataException e) {
      // failed to get midi data
    }
    if (play) {
      sequencer.start();
    }
  }

  @Override
  public void togglePlay() {
    if (!practicing) {
      this.guiDelegate.movePanel();
      if (this.play) {
        sequencer.stop();
        this.play = false;
      } else {
        sequencer.start();
        sequencer.setTempoInMPQ(super.tempo);
        this.play = true;
      }
    }
  }

  @Override
  public void prevBeat() {
    if (!play && !practicing) {
      this.guiDelegate.prevBeat();
      super.prevBeat();
    }
  }

  @Override
  public void nextBeat() {
    if (!practicing) {
      if (!play) {
        this.guiDelegate.nextBeat();
        super.nextBeat();
      }
    } else {
      if (playedAllNotes()) {
        for (Note n : this.op.activeNotes(GuiViewFrame.BEAT).values()) {
          try {
            this.playNext(n.getTone(), GuiViewFrame.BEAT, n.getVolume(), n.getInstrument() - 1);
          } catch (InvalidMidiDataException e) {
          }
        }

        try {
          sequencerForPratice.setSequence(sequenceForPratice);
        } catch (InvalidMidiDataException e) {
          // failed to get midi data
        }
        this.sequencerForPratice.setTickPosition(GuiViewFrame.BEAT);
        this.sequencerForPratice.start();
        try {
          Thread.sleep((int) this.sequencerForPratice.getTempoInBPM());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        this.sequencerForPratice.stop();
        this.guiDelegate.nextBeat();
        super.nextBeat();
        tonesPlayed = new ArrayList<>();
      }
    }
  }

  private boolean playedAllNotes() {
    for (String key : op.activeNotes(GuiViewFrame.BEAT).keySet()) {
      if (!tonesPlayed.contains(key)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void toEnd() {
    this.guiDelegate.toEnd();
    this.sequencer.setTickPosition(op.lastBeat());
  }

  @Override
  public void toBeginning() {
    this.guiDelegate.toBeginning();
    this.sequencer.setTickPosition(0);
    this.sequencer.setTempoInMPQ(this.tempo);
    if (play) {
      this.sequencer.start();
    }
  }

  @Override
  public void resetFocus() {
    this.guiDelegate.resetFocus();
  }

  @Override
  public void refresh() {
    super.refresh();
    this.update();
    this.guiDelegate.refresh();
  }

  @Override
  public void addKeyListener(KeyListener e) {
    this.guiDelegate.addKeyListener(e);
  }

  @Override
  public void addMouseListener(MouseListener e) {
    this.guiDelegate.addMouseListener(e);
  }

  @Override
  public void addNote(MouseEvent e, int duration) {
    if (!practicing) {
      if (!play) {
        this.guiDelegate.addNote(e, duration);
        refresh();
      }
    } else {
      for (int i = 0; i < PianoPanel.KEYS.size(); i++) {
        PianoPanel.Key k = PianoPanel.KEYS.get(i);
        if (k.onKey(e.getX(), e.getY() - 500)) {
          if (k.getPitch().isSharp()) {
            tonesPlayed.add(k.getPitch().toString() + Integer.toString(k.getOctave()));
            break;
          } else {
            for (int j = i; j < PianoPanel.KEYS.size(); j++) {
              if (k.onKey(e.getX(), e.getY() - 500)) {
                tonesPlayed.add(k.getPitch().toString() + Integer.toString(k.getOctave()));
                break;
              }
            }
          }
        }
      }
      this.nextBeat();
    }
  }

  @Override
  public void movePanel() {
    //do nothing
  }

  @Override
  public void increaseTempo() {
    if (!play) {
      op.setTempo(op.getTempo() - 5000);
      super.refresh();
    }
  }

  @Override
  public void decreaseTempo() {
    if (!play) {
      op.setTempo(op.getTempo() + 5000);
      super.refresh();
    }
  }

  @Override
  public void togglePractice() {
    if (!play) {
      if (practicing) {
        practicing = false;
      } else {
        practicing = true;
      }
    }
//    JLabel practice = new JLabel("Practice Mode");
//    practice.setText("Test");
//    this.guiDelegate.add(practice);
//    this.guiDelegate.refresh();
  }

  @Override
  public void startCreate() {
    this.guiDelegate.startCreate();
  }

  @Override
  public void addBeginRepeat() {
    if (!play && !repeatBeats.contains(GuiViewFrame.BEAT)) {

      int beat = GuiViewFrame.BEAT;
      if (beat % 4 == 0) {
        op.addRepeat(beat, RepeatType.BEGIN);
      }
      repeatBeats = op.repeatBeats();
      repeats = op.getRepeats();
    }
  }

  @Override
  public void addEndRepeat() {
    if (!play && !repeatBeats.contains(GuiViewFrame.BEAT)) {
      int beat = GuiViewFrame.BEAT;
      if (beat % 4 == 0) {
        op.addRepeat(beat, RepeatType.END);
      }
      repeatBeats = op.repeatBeats();
      repeats = op.getRepeats();
    }
  }

  @Override
  public void addDoubleRepeat() {
    if (!play && !repeatBeats.contains(GuiViewFrame.BEAT)) {
      int beat = GuiViewFrame.BEAT;
      if (beat % 4 == 0) {
        op.addRepeat(beat, RepeatType.BOTH);
      }
      repeatBeats = op.repeatBeats();
      repeats = op.getRepeats();
    }
  }

  @Override
  public void addTimebar() {
    if (op.getRepeats().containsKey(GuiViewFrame.BEAT)
            && !op.getRepeats().get(GuiViewFrame.BEAT).hasTimebar()
            && GuiViewFrame.BEAT > lastEnding()
            && (op.getRepeats().get(GuiViewFrame.BEAT).getType() == RepeatType.BEGIN
            || op.getRepeats().get(GuiViewFrame.BEAT).getType() == RepeatType.BOTH)) {
      TimeBar t = new TimeBar(GuiViewFrame.BEAT - 4, op.getTimebars().size() + 1);
      this.op.addTimebar(t);
      op.getRepeats().get(GuiViewFrame.BEAT).setTimebar(t);
    }
  }

  private int lastEnding() {
    int max = 0;
    for (TimeBar r : op.getTimebars()) {
      if (r.getStartingBeat() > max) {
        max = r.getStartingBeat();
      }
    }
    return max;
  }

  /**
   * Plays the given note as defined by the following parameters.
   *
   * @param tone       Represents the tone of the note to play.
   * @param startBeat  Represents the startBeat of the note to play.
   * @param volume     Represents the volume of the note to play.
   * @param instrument Represents the instrument of the note to play.
   * @throws InvalidMidiDataException if the midi fails to play the note.
   */
  protected void playNext(String tone, int startBeat, int volume, int instrument)
          throws InvalidMidiDataException {
    MidiMessage start = new ShortMessage(ShortMessage.NOTE_ON, instrument,
            Pitch.toneIndex.indexOf(tone), volume);
    MidiMessage stop = new ShortMessage(ShortMessage.NOTE_OFF, instrument,
            Pitch.toneIndex.indexOf(tone), volume);

    MidiEvent startNote = new MidiEvent(start, startBeat);
    MidiEvent endNote = new MidiEvent(stop, startBeat + 1);
    Track notes = sequenceForPratice.createTrack();
    notes.add(startNote);
    notes.add(endNote);
  }

  private class Refresh implements MetaEventListener {
    @Override
    public void meta(MetaMessage meta) {
      int currentBeat = (int) sequencer.getTickPosition();
      Repeat r = repeats.get(currentBeat);

      handleEndings();

      handleRepeats(currentBeat, r);

      GuiViewFrame.BEAT = (int) sequencer.getTickPosition();
      guiDelegate.movePanel();
      guiDelegate.refresh();
      resetRepeats();
    }

    private void handleRepeats(int currentBeat, Repeat r) {
      if (repeatBeats.contains(currentBeat)) {
        if (r.getType() == RepeatType.BEGIN && !r.isPlayed()
                || r.getType() == RepeatType.BOTH && !r.isEndPlayed()) {
          for (int i = currentBeat - 1; i >= 0; i--) {
            if (repeats.containsKey(i) && repeats.get(i).getType() == RepeatType.END
                    && !repeats.get(i).isPlayed()) {
              sequencer.setTickPosition(i);
              break;
            } else if (repeats.containsKey(i) && repeats.get(i).getType() == RepeatType.BOTH) {
              sequencer.setTickPosition(i);
              break;
            } else if (i == 0) {
              sequencer.setTickPosition(0);
              break;
            }
          }
          r.setPlayed();
          sequencer.setTempoInMPQ(tempo);
        }
      }
    }

    private void handleEndings() {
      if (repeatBeats.contains(GuiViewFrame.BEAT + 4) &&
              repeats.get(GuiViewFrame.BEAT + 4).hasTimebar()) {
        if (repeats.get(GuiViewFrame.BEAT + 4).getTimebar().getEndingNumber() != currentEnding) {
          sequencer.setTickPosition(GuiViewFrame.BEAT + 4);
          sequencer.setTempoInMPQ(tempo);
        } else {
          currentEnding++;
          sequencer.setTickPosition(GuiViewFrame.BEAT + 1);
          sequencer.setTempoInMPQ(tempo);
        }
      }
    }

    private void resetRepeats() {
      if (GuiViewFrame.BEAT == op.lastBeat()) {
        for (Repeat r : op.getRepeats().values()) {
          r.setHasntPlayed();
          currentEnding = 1;

        }
      }
    }
  }

}