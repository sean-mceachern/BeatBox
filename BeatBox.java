import java.awt.*;
import javax.swing.*; 
import javax.sound.midi.*; 
import java.util.*; 
import java.awt.event.*;

// Beat Box music maker from Head First Java ed. 2, ch. 13
// This class will build the beatbox display, setup the MIDI and then build the tracks and start playing
// The beatbox will display 16 sounds to choose from over 16 measures

public class BeatBox {
	JPanel mainPanel;
	ArrayList<JCheckBox> checkboxList; /* an array of checkboxes */
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	JFrame theFrame;

	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", 
								"Hand Clap","High Tom", "High Bongo", "Maracas", "Whistle", "Low Conga"," Cowbell",
								"Vibraslap", "Low-mid Tom", "High Agogo", "Open High Conga"}; /* name of instruments for GUI labels */

	int[] instruments = {35,41,46,38,49,39,50,60,70,72,64,56,58,47,67,63}; /* each number is a different drum "key" */ 


	public static void main(String[] args) {
		new BeatBox().BuildGUI();
	}

	// this method lays out what the GUI will look like
	public void BuildGUI() {
		theFrame = new JFrame("Cyber BeatBox");
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); /* purely aesthetic border */

		checkboxList = new ArrayList<JCheckBox>();
		Box buttonBox = new Box(BoxLayout.Y_AXIS);

		// adding the 4 music buttons: Start, Stop, Tempo Up, and Tempo Down
		JButton start = new JButton("Start");
		start.addActionListener(new MyStopListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyStopListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyStopListener());
		buttonBox.add(downTempo);

		// name boxes for the 16 instruments
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i < 16; i++) {
			nameBox.add(new Label(instrumentNames[i]));
		}
		// add the music buttons and instument names boxes
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);

		theFrame.getContentPane().add(background);

		// UI layout specs
		GridLayout grid = new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER, mainPanel);

		// make the checkboxes, set to false so they are unchecked, add them to ArrayList and GUI planel
		for(int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}

		setUpMidi();
		theFrame.setBounds(50,50,300,300);
		theFrame.pack();
		theFrame.setVisible(true);
	}
	// usual MIDI setup
	public void setUpMidi() {
		try{
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
			
		}catch(Exception e) {e.printStackTrace();}
	}

	public void buildTrackAndStart() {
		int[] trackList = null;

		sequence.deleteTrack(track); /* get rid of the old track */
		track = sequence.createTrack(); /* create a new track */

		for(int i = 0; i < 16; i++) { /* for loop for each row (drum, hi-hat, bass, congo, etc..) */
			trackList = new int[16];
			int key = instruments[i];

			for(int j = 0; j < 16; j++) { /* for each beat column */
				JCheckBox jc = checkboxList.get(j + 16 * i);
				// puts the instrument on the track if the checkBox is selected at each beat
				if(jc.isSelected()){
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			makeTracks(trackList);
			track.add(makeEvent(176,1,127,0,16));
		}
		track.add(makeEvent(192,9,1,0,15)); /* this makes sure the beats go to 16 */
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception e) {e.printStackTrace();}
	}

// This inner class is a listener for the buttons. it calls on the method which builds and plays the track when the button is clicked.
	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			buildTrackAndStart();
		}
	}
// inner class listener for the stop button
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			sequencer.stop();
		}
	}
// inner class listener for the increase tempo button
	public class myUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor ((float) (tempoFactor * 1.03)); /*increases the tempo by 3% per click */
		}
	}
// inner class listener for the decrease tempo button
	public class myDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempoFactor * .97));
		}
	}
// iterates through each measure and adds a selected track
	public void makeTracks(int[] list) {
		for(int i = 0; i < 16; i++) {
			int key = list[i];

			if(key != 0) {
				track.add(makeEvent(144,9,key,100,i)); /*note on event */
				track.add(makeEvent(128,9,key,100,i + 1)); /*note off event */
			}
		}
	}
//
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
		MidiEvent event = null;
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(comd,chan,one,two);
			event = new MidiEvent(a,tick);
		} catch(Exception e) {e.printStackTrace();}
		return event;
	} 
}



































