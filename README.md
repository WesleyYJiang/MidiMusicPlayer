# MidiMusicPlayer

README:

This program is designed to allow users to create, modify, and play music via the usage of the
Controller that manipulated the Model via the methods found in the IMusicOperations which then
updates the different Views.

MODEL:

The design of the music model is to have an interface IMusicOperations that contains
all the methods to have all the operations a music model would be able to do.
A music model that implements this interface should be able to add a note, delete a note, edit a
note, get the last beat, get the tones of the music, and also get all the notes that start
at a specific beat.

Model: The model Music, represents a piece of music, implements the IMusicOperations interface.

Pitch: The Pitch enum class represents the pitch in music. A pitch is a enum, and they also has have
corresponding String values. There is also a static array list of all the tones, which are all
the possible pitches plus octaves, from C1 ro B10. A tone is represented in Strings. The index of
this array list is used to scale the console rendering of the music model, because it gives the
order of the pitches and also allow the string render to scale when notes are added and removed.

ToneRange: The ToneRange class represents the range of tones in music. A tone is represented by
String. This class has a deque of strings to keep track of all the tones in the music model.
This deque can be updated as the notes are added or deleted.

Note: The Note class represents the notes in music. A note is made of a pitch, an octave and a
duration. Octave an duration are integers.

Set: The Set Class represents a group of notes. This group of notes all have the same starting beat.

ConsoleVisualizer: The ConsoleVisualizer helps the model to draw the console render.
This class has a public method render, given the notes, the tones, and the last beat, it can render
the console view of this music.



VIEW:

View: In the view interface, called View, we define all of the methods to be implemented by our four
views. Console View, MidiView, GuiViewFrame, and PlayMidiFile.

GuiView: In the GuiView interface (it extends view) we add additional methods to be used by our
GuiViewFrame
*Modified* We added methods to give the GUI related classes that implements this interface
more functionality

Console View: In the console view we print out the model's contents to the console for the user to
see.The console view, implements the View interface.

MidiView: In the MidiView we play the notes found in the model. This view also implements the View
interface.

GuiView: In the GuiView we created a basic graphics user interface that allows the user to scroll
through the notes in the song and move the song marker via the left and right arrow keys. This view
implements the GuiView interface.

*Modified*
CompositeView: This view class extends the existing Midi class and has a GuiView class as a
delegate. We have the GUi view and midi view synced via the sequencer, and we can send in the
sequencer meta message per beat and pass in a metaMessageListener that refreshes the Gui view
every beat according to the current beat of the sequencer.


CONTROLLER:

Controller: We allow the user to input the initial settings so that they can choose what song to
play and what view to use. This is a basic controller and will need to be changed in the upcoming
homework. Implements the IController.

*Modified*
The controller establishes the mouse listener and keyboard listener to connect to the view.

UTIL:

MusicReader: Converts midi-text and adds it to our Model via the usage of a Builder class
that conforms to the MidiModel.

BUILDER:

CompositionBuilder: Interface for the Builder class found in our Music class (the model).
This allows the MusicReader to convert Midi-Text to our model.

MusicEditor: Main class that starts the program. Parses the configuration and sets up the view
and file to open.
