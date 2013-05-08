import processing.core.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.util.*;

public class Main extends PApplet {

	// May 2010
	// http://www.abandonedart.org
	// http://www.zenbullets.com
	//
	// This work is licensed under a Creative Commons 3.0 License.
	// (Attribution - NonCommerical - ShareAlike)
	// http://creativecommons.org/licenses/by-nc-sa/3.0/
	// 
	// This basically means, you are free to use it as long as you:
	// 1. give http://www.zenbullets.com a credit
	// 2. don't use it for commercial gain
	// 3. share anything you create with it in the same way I have
	//
	// These conditions can be waived if you want to do something groovy with it 
	// though, so feel free to email me via http://www.zenbullets.com


	/**
	 * Constants.
	 */
	final int FFT_BAND_PER_OCT_NBR = 3;
	final int FFT_BASE_FREQ = 22;
	final int BPM_CNT = 140;
	final int MAX_LATENCY_MS_NBR = 13;


	/**
	 * Global variables.
	 */
	Minim _minim;
	AudioPlayer _player;
	FFT _fft;
	JSONObject _songJSON;
	JSONArray _beatsJSONArray, _sectionsJSONArray;
	JSONObject[] _beats, _sections;
	int _currBeatIdx = 0;
	int _currSectionIdx = 0;

	int a = 0;
	ArrayList<Circle> circles = new ArrayList<Circle>();


	/**
	 * Creates a Processing PApplet.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		PApplet.main(new String[] { "Main" });
	}


	/**
	 * Setup.
	 */
	public void setup() {
		size(1200, 800);
		smooth(); 
		frameRate(60);
		background(0);

		try {
			loadData();
		} catch (Exception excp) {
			excp.printStackTrace();
		}

		_minim = new Minim(this);
		_player = _minim.loadFile("Song of Los.wav", 512);

		_fft = new FFT(_player.bufferSize(), _player.sampleRate());
		_fft.window(FFT.HAMMING);
		// With values of 22 and 3, we are dividing the spectrum into 10 octages, each of 3 bands.
		_fft.logAverages(FFT_BASE_FREQ, FFT_BAND_PER_OCT_NBR);

		circles.add(new Circle(this, _fft, 4, 30));

		_player.play();
	}


	/**
	 * Draw loop.
	 */
	public void draw(){
		
		drawBeat();
		//drawSection();
	}


	/**
	 * Handles keyboard button presses.
	 */
	public void keyPressed() {
		if (key==ESC) {
			key=0;
			cleanupResources();
		}
	}


	/**
	 * Performs cleanup when the sketch is terminated.
	 */
	public void cleanupResources() {
		_player.close();
		_minim.stop();

		try {
			// Add in a short sleep time to give minim threads a chance to end; otherwise, you will hear a heinous sound.
			Thread.sleep(50);
		} catch (InterruptedException excp) {
			excp.printStackTrace();
		}
		exit();
	}


	/**
	 * Loads song analysis data.
	 * @throws Exception 
	 */
	private void loadData() throws Exception {
		String jsonTxt = join(loadStrings("Song of Los.txt"), "");

		JSONParser parser = new JSONParser();

		_songJSON = (JSONObject)parser.parse(jsonTxt);
		_beatsJSONArray = (JSONArray)_songJSON.get("beats");

		_beats = new JSONObject[_beatsJSONArray.size()];

		for (int idx = 0; idx < _beatsJSONArray.size(); idx++) {
			_beats[idx] = (JSONObject)_beatsJSONArray.get(idx);
		}
		
		_sectionsJSONArray = (JSONArray)_songJSON.get("sections");
		
		_sections = new JSONObject[_sectionsJSONArray.size()];
		
		for (int idx = 0; idx < _sectionsJSONArray.size(); idx++) {
			_sections[idx] = (JSONObject)_sectionsJSONArray.get(idx);
		}
	}


	private void drawBeat() {
		
		if (_currBeatIdx < _beats.length) {
			int startTmMsNbr = (int)(Float.parseFloat(_beats[_currBeatIdx].get("start").toString()) * 1000);

			if (startTmMsNbr >= _player.position() - MAX_LATENCY_MS_NBR) {
				int diffMsNbr = abs(startTmMsNbr - _player.position());

				if (diffMsNbr <= MAX_LATENCY_MS_NBR) {
					circles.get(0).update();
					//println(_currBeatIdx + " drawn");
					_currBeatIdx++;
				}
			} else {
				_currBeatIdx++;
			}
		}
	}
	
	
	
	private void drawSection() {
		
		if (_currSectionIdx < _sections.length) {
			int startTmMsNbr = (int)(Float.parseFloat(_sections[_currSectionIdx].get("start").toString()) * 1000);

			if (startTmMsNbr >= _player.position() - MAX_LATENCY_MS_NBR) {
				int diffMsNbr = abs(startTmMsNbr - _player.position());

				if (diffMsNbr <= MAX_LATENCY_MS_NBR) {
					rectMode(CENTER);
					noStroke();
					fill(0);
					rect(width/2, height/2, 20, 20);
					fill(random(255));
					rect(width/2, height/2, 20, 20);
					_currSectionIdx++;
				}
			} else {
				_currSectionIdx++;
			}
		}
	}
}
