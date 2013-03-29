package info.ss12.audioalertsystem;

import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

/**
 * Date: 3/28/13
 * Time: 9:28 PM
 */
public class AlarmApi extends WhistleApi
{

    public AlarmApi(WaveHeader waveHeader)
    {
        super(waveHeader);
    }

    @Override
    protected void init()
    {
        minFrequency = 600.0f;
        maxFrequency = Double.MAX_VALUE;

        minIntensity = 100.0f;
        maxIntensity = 100000.0f;

        minStandardDeviation = 0.1f;
        maxStandardDeviation = 1.0f;

        highPass = 100;
        lowPass = 10000;

        minNumZeroCross = 50;
        maxNumZeroCross = 200;

        numRobust = 10;
    }

    public boolean isAlarm(byte[] audioBytes)
    {
        return isSpecificSound(audioBytes);
    }

    @Override
    public boolean isWhistle(byte[] audioBytes)
    {
        return isAlarm(audioBytes);
    }
}
