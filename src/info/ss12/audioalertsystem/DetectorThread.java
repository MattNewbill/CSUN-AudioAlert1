/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * musicg api in Google Code: http://code.google.com/p/musicg/
 * Android Application in Google Play: https://play.google.com/store/apps/details?id=com.whistleapp
 * 
 */

package info.ss12.audioalertsystem;

import java.util.LinkedList;

import info.ss12.audioalertsystem.AlarmAPI;
import com.musicg.wave.WaveHeader;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Vibrator;
import android.content.Context;

public class DetectorThread extends Thread{

	private RecorderThread recorder;
	private WaveHeader waveHeader;
	private AlarmAPI alarmAPI;
	private Context context;
	private Vibrator vibrator = (Vibrator)context.getSystemService("VIBRATOR_SERVICE");
	private volatile Thread _thread;

	private LinkedList<Boolean> alarmResultList = new LinkedList<Boolean>();
	private int numAlarms;
	private int alarmCheckLength = 3;
	private int alarmPassScore = 3;
	
	private OnSignalsDetectedListener onSignalsDetectedListener;
	
	public DetectorThread(RecorderThread recorder){
		this.recorder = recorder;
		AudioRecord audioRecord = recorder.getAudioRecord();
		
		int bitsPerSample = 0;
		if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT){
			bitsPerSample = 16;
		}
		else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT){
			bitsPerSample = 8;
		}
		
		int channel = 0;
		// whistle detection only supports mono channel
		if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_CONFIGURATION_MONO){
			channel = 1;
		}

		waveHeader = new WaveHeader();
		waveHeader.setChannels(channel);
		waveHeader.setBitsPerSample(bitsPerSample);
		waveHeader.setSampleRate(audioRecord.getSampleRate());
		alarmAPI = new AlarmAPI(waveHeader);
	}

	private void initBuffer() {
		numAlarms = 0;
		alarmResultList.clear();
		
		// init the first frames
		for (int i = 0; i < alarmCheckLength; i++) {
			alarmResultList.add(false);
		}
		// end init the first frames
	}

	public void start() {
		_thread = new Thread(this);
        _thread.start();
    }
	
	public void stopDetection(){
		_thread = null;
	}
	
	public void run() {
		try {
			byte[] buffer;
			initBuffer();
			
			Thread thisThread = Thread.currentThread();
			while (_thread == thisThread) {
				// detect sound
				buffer = recorder.getFrameBytes();
				
				// audio analyst
				if (buffer != null) {
					// sound detected	
					// whistle detection
					//System.out.println("*Whistle:");
					boolean isAlarm = alarmAPI.isFireAlarm(buffer);
					if (alarmResultList.getFirst()) {
						numAlarms--;
					}
		
					alarmResultList.removeFirst();
					alarmResultList.add(isAlarm);
		
					if (isAlarm) {
						numAlarms++;
						vibrator.vibrate(5000);
					}
					//System.out.println("num:" + numWhistles);
		
					if (numAlarms >= alarmPassScore) {
						// clear buffer
						initBuffer();
						onFireAlarmDetected();
					}
				// end whistle detection
				}
				else{
					// no sound detected
					if (alarmResultList.getFirst()) {
						numAlarms--;
					}
					alarmResultList.removeFirst();
					alarmResultList.add(false);
				}
				// end audio analyst
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void onFireAlarmDetected(){
		if (onSignalsDetectedListener != null){
			onSignalsDetectedListener.onFireAlarmDetected();
		}
	}
	
	public void setOnSignalsDetectedListener(OnSignalsDetectedListener listener){
		onSignalsDetectedListener = listener;
	}
}