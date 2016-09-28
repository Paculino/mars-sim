/**
 * Mars Simulation Project
 * AudioPlayer.java
 * @version 3.08 2016-03-31
 * @author Lars Naesbye Christensen (complete rewrite for OGG)
 */

package org.mars_sim.msp.ui.swing.sound;

import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;

import javafx.application.Platform;

/**
 * A class to dispatch playback of OGG files to OGGSoundClip.
 */
public class AudioPlayer {

	private static Logger logger = Logger.getLogger(AudioPlayer.class.getName());

	/** The current clip sound. */
	private OGGSoundClip currentOGGSoundClip;
	private OGGSoundClip backgroundSoundTrack;
	
	private MainDesktopPane desktop;
	
	/** The volume of the audio player (0.0 to 1.0) */
	private float volume = .5F;

	public AudioPlayer(MainDesktopPane desktop) {
		logger.info("constructor is on " + Thread.currentThread().getName());
		this.desktop = desktop;
		
		currentOGGSoundClip = null;
		backgroundSoundTrack = null;
		
	
		if (UIConfig.INSTANCE.useUIDefault()) {
			setMute(false);
			setVolume(.5F);
		} else {
			setMute(UIConfig.INSTANCE.isMute());
			setVolume(UIConfig.INSTANCE.getVolume());
		}
	}

	/**
	 * Play a clip once.
	 * 
	 * @param filepath
	 *            the file path to the sound file.
	 */
	@SuppressWarnings("restriction")
	public void play(String filepath) {
		logger.info("play() is on " + Thread.currentThread().getName());
		// 2016-09-27 Adde checking if it's set to mute.
		
		//if (!isMute()) {
			if (desktop.getMainScene() != null) {
					Platform.runLater(() -> {
						logger.info("play() is on " + Thread.currentThread().getName());
						try {
							currentOGGSoundClip = new OGGSoundClip(filepath);
							if (!isMute(false)) {
								// Use the state of the background sound track to determine if the sound effect should be played. 
								currentOGGSoundClip.play();
								logger.info("Just currentOGGSoundClip.play()");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
			}
			
			else {
				SwingUtilities.invokeLater(() -> {
						logger.info("play() is on " + Thread.currentThread().getName());
						try {
							currentOGGSoundClip = new OGGSoundClip(filepath);
							if (!isMute(false)) {
								// Use the state of the background sound track to determine if the sound effect should be played. 
								currentOGGSoundClip.play();
								logger.info("Just currentOGGSoundClip.play()");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}	
				});
			}
		//}
	}

	/**
	 * Play a clip once.
	 * @param filepath  the file path to the sound file.
	 */
	@SuppressWarnings("restriction")
	public void playInBackground(String filepath) {
		logger.info("play() is on " + Thread.currentThread().getName());
		// 2016-09-28 Added checking if it's set to mute.
			if (desktop.getMainScene() != null) {
					Platform.runLater(() -> {
						logger.info("playInBackground() is on " + Thread.currentThread().getName());
						try {
							backgroundSoundTrack = new OGGSoundClip(filepath);
							if (!isMute(false))
								backgroundSoundTrack.play();
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
			}
			
			else {
				SwingUtilities.invokeLater(() -> {
						logger.info("playInBackground() is on " + Thread.currentThread().getName());
						try {
							backgroundSoundTrack = new OGGSoundClip(filepath);
							if (!isMute(false)) 
								backgroundSoundTrack.play();
						} catch (IOException e) {
							e.printStackTrace();
						}	
				});
			}
		//}
	}
	
	/**
	 * Play the clip in a loop.
	 * 
	 * @param filepath
	 *            the filepath to the sound file.
	 */
	public void loop(String filepath) {
		try {
			// 2016-09-28 Replaced currentOGGSoundClip with backgroundSoundTrack for looping
			backgroundSoundTrack = new OGGSoundClip(filepath);
			backgroundSoundTrack.loop();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Stops the playing clip.
	 */

	public void stop() {
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.stop();
			currentOGGSoundClip = null;
		}
		// 2016-09-28 Added backgroundSoundTrack
		if (backgroundSoundTrack != null) {
			backgroundSoundTrack.stop();
			backgroundSoundTrack = null;
		}
	}

	/**
	 * Gets the volume of the audio player.
	 * 
	 * @return volume (0.0 to 1.0)
	 */
	public float getVolume() {
		return volume;
	}

	/**
	 * Sets the volume of the audio player.
	 * 
	 * @param volume
	 *            (0.0 quiet, .5 medium, 1.0 loud) (0.0 to 1.0 valid range)
	 */
	public void setVolume(float volume) {
		if (volume < 0F)
			volume = 0F;
		if (volume > 1F)
			volume = 1F;

		this.volume = volume;
		if (currentOGGSoundClip != null) {
			currentOGGSoundClip.setGain(volume);
		}
		// 2016-09-28 Added backgroundSoundTrack
		if (backgroundSoundTrack != null) {
			backgroundSoundTrack.setGain(volume);
		}
	}

	/**
	 * Checks if the audio player is muted.
	 * @return true if muted.
	 */
	public boolean isMute(boolean isSoundEffect) {
		boolean result = false;
		if (isSoundEffect) {
			if (currentOGGSoundClip != null) {
				result = currentOGGSoundClip.isMute();
			}
		}
		else { 
			// 2016-09-28 Added backgroundSoundTrack
			if (backgroundSoundTrack != null) {
				result = backgroundSoundTrack.isMute();
			}
		}
		return result;
	}

	/**
	 * Sets if the audio player is mute or not.
	 * @param mute is audio player mute?
	 */
	public void setMute(boolean mute) {
		//if (currentOGGSoundClip != null) {
		//	currentOGGSoundClip.setMute(mute);
		//}
		// 2016-09-28 Added backgroundSoundTrack
		if (backgroundSoundTrack != null) {
			backgroundSoundTrack.setMute(mute);
		}
		
	}

	public void cleanAudioPlayer() {
		stop();
	}

}