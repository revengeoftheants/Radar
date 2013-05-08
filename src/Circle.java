import java.util.concurrent.*;
import processing.core.*;
import ddf.minim.analysis.*;

public class Circle {

	/**
	 * Constants.
	 */
	final PApplet PAR_APP;
	final int ID;
	final FFT LOG_FFT;
	final PVector CENTER_COORD;
	final int RADIAL_BUFFER_LEN_NBR = 20;
	final int MAX_SEGMENTS_CNT = 5;
	final float ANGLE_RADIAN_INCRMNT_NBR = PApplet.radians(6);
	final float START_ANGLE_RADIAN_NBR = -(PApplet.HALF_PI); // We start drawing the circle at the top of the screen.
	

	/**
	 * Global variables.
	 */
	float _prevAvgNbr, _currAvgNbr;
	int _thresholdNbr;
	float _currAngleRadianNbr = START_ANGLE_RADIAN_NBR;
	float _radiusLenNbr = 100;
	float _strokeClrNbr = 254;
	float _greenClrNbr, _blueClrNbr;
	PVector _lastCircumLnCoord, _lastRadialLnOuterCoord;
	ArrayBlockingQueue<Float> _radiansToDraw = new ArrayBlockingQueue<Float>(MAX_SEGMENTS_CNT);


	/**
	 * Constructor.
	 * 
	 * @param inpParApp
	 * @param inpCircleId
	 */
	Circle (PApplet inpParApp, FFT inpLogFFT, int inpCircleId, int inpThresholdNbr) {
		PAR_APP = inpParApp;
		LOG_FFT = inpLogFFT;
		ID = inpCircleId;
		CENTER_COORD = new PVector(PAR_APP.width/2, PAR_APP.height/2, 0);
		_thresholdNbr = inpThresholdNbr;
		_lastCircumLnCoord = new PVector();
		_lastRadialLnOuterCoord = new PVector();
		
		init();
	}


	/**
	 * Initializes this circle's global values.
	 */
	public void init() {
		_radiusLenNbr = (PAR_APP.height/2 - RADIAL_BUFFER_LEN_NBR);
		_lastCircumLnCoord.x = CENTER_COORD.x;
		_lastCircumLnCoord.y = CENTER_COORD.y - _radiusLenNbr;
		PAR_APP.noiseSeed((int)PAR_APP.random(1000));
		_strokeClrNbr = PAR_APP.random(1) + 0.5f;
		_greenClrNbr = PAR_APP.random(255); 
		_blueClrNbr = PAR_APP.random(255);

	}


	boolean detected() {
		Boolean rtnInd = false;
		_currAvgNbr = LOG_FFT.getAvg(ID);

		PAR_APP.println("freqBand: " + ID + " avg: " + _currAvgNbr);

		if (_currAvgNbr - _prevAvgNbr > _thresholdNbr) {
			rtnInd = true;
		}

		_prevAvgNbr = _currAvgNbr;

		return rtnInd;
	}


	/**
	 * Updates the circle.
	 */
	public void update() {
		PAR_APP.background(0);
		PAR_APP.noFill();
		
		float lastAngleRadianNbr = 0;
		
		if (_currAngleRadianNbr >= (START_ANGLE_RADIAN_NBR + PApplet.TWO_PI)) { 
			_currAngleRadianNbr -= PApplet.TWO_PI;
			//lastAngleRadianNbr -= PApplet.TWO_PI;
			_strokeClrNbr++; 
			//clearBackground();
		}

		if (_strokeClrNbr > 10) {
			if (PAR_APP.random(2) > 1) {
				_blueClrNbr += 40;
				if (_blueClrNbr > 250) { _blueClrNbr = 0; }
			} else {
				_greenClrNbr += 40;
				if (_greenClrNbr > 250) { _greenClrNbr = 0; }
			}
		}
		
		if (_radiansToDraw.remainingCapacity() == 0) {
			_radiansToDraw.poll();
		}
		
		_radiansToDraw.add(_currAngleRadianNbr);
		
		Float[] radiansToDrawArr = new Float[_radiansToDraw.size()];
		_radiansToDraw.toArray(radiansToDrawArr);
		
		int _opacityNbr = 255;
		
		for (int idx = radiansToDrawArr.length - 1; idx > -1; idx--) {
			
			float _thisAngleRadianNbr = radiansToDrawArr[idx];
			
			if (radiansToDrawArr.length == 5 || (radiansToDrawArr.length > 1 && idx > 0)) {
				lastAngleRadianNbr = _thisAngleRadianNbr - ANGLE_RADIAN_INCRMNT_NBR;
			} else {
				lastAngleRadianNbr = _thisAngleRadianNbr;
			}
			
			// Get the end points of our radial lines.
			PVector _currRadialLnOuterCoord = new PVector(CENTER_COORD.x + (_radiusLenNbr * PApplet.cos(_thisAngleRadianNbr)), 
														  CENTER_COORD.y + (_radiusLenNbr * PApplet.sin(_thisAngleRadianNbr)));
			
			PAR_APP.strokeWeight(0.7f);
			PAR_APP.stroke(_strokeClrNbr * 40, _greenClrNbr, _blueClrNbr, _opacityNbr * 0.95f);
			PAR_APP.arc(CENTER_COORD.x, CENTER_COORD.y, 2*_radiusLenNbr, 2*_radiusLenNbr, lastAngleRadianNbr, _thisAngleRadianNbr);
			PAR_APP.strokeWeight(0.5f);
			PAR_APP.stroke(_strokeClrNbr * 40, _greenClrNbr, _blueClrNbr, _opacityNbr * 0.75f);
			PAR_APP.line(CENTER_COORD.x, CENTER_COORD.y, _currRadialLnOuterCoord.x, _currRadialLnOuterCoord.y);
			
			_opacityNbr -= (255/MAX_SEGMENTS_CNT);
		}
		

		//_lastCircumLnCoord.x = _currRadialLnOuterCoord.x;
		//_lastCircumLnCoord.y = _currRadialLnOuterCoord.y;
		//_lastAngleRadianNbr = _currAngleRadianNbr;
		_currAngleRadianNbr += ANGLE_RADIAN_INCRMNT_NBR;
	}


	/**
	 * Clears the background.
	 */
	private void clearBackground() {
		// Draws a semi-transparent black rectangle across the entire viewport in order to partially obscure the last drawing.
		PAR_APP.fill(0);
		PAR_APP.noStroke();
		PAR_APP.rectMode(PApplet.CORNER);
		PAR_APP.rect(0, 0, PAR_APP.width, PAR_APP.height);
		PAR_APP.noFill();
	}

}