package com.zerocode.tuningnew;

import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zerocode.fft.RealDoubleFFT;

import java.text.DecimalFormat;

public class MainActivity extends ActionBarActivity {
    private boolean isRecording = false;    //is Recording started or not
    private Button btnStartRecording;   //button for start recording
    private Button btnStopRecording;    //button for stop recording
    /**
     * Button for choosing range of frequencies (432 or 440)
     * Author: Milorad
     */
    private RadioButton rb432;
    /**
     * Button for choosing range of frequencies (432 or 440)
     * Author: Milorad
     */
    private RadioButton rb440;

    private int frequency = 8000;   //sample rate for max 3600Hz frequencies (more info here: http://wiki.audacityteam.org/wiki/Sample_Rates)
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;


    /**
     * Buffer in which we store measurements
     * Author: Milorad
     */
    private CircularBuffer freqCircularBuffer;
    private int bufferLength;   //length of buffer
    /**
     * Counter for measurements, one refresh of display per few measurements, specified by counter
     * Author: Milorad
     */
    private int cntDisplayReading;

    AudioRecord audioRecord;
    RecordAudio recordTask;

    private RealDoubleFFT fft;

    private TextView mFrequence;    //text view which show note
    // private Typeface tf = Typeface.createFromAsset(getAssets(), "Malin.ttf");

    // Called when the activity is first created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartRecording = (Button) findViewById(R.id.btn_start);
        btnStopRecording = (Button) findViewById(R.id.btn_stop);

        btnStopRecording.setVisibility(View.INVISIBLE);
        btnStartRecording.setVisibility(View.VISIBLE);

        mFrequence = (TextView) findViewById(R.id.frequency);

        bufferLength = 5;
        freqCircularBuffer = new CircularBuffer(bufferLength);
        cntDisplayReading = 0;

        rb432 = (RadioButton) findViewById(R.id.rb432);
        rb440 = (RadioButton) findViewById(R.id.rb440);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //keep screen on when app open

    }

    //function to start recording
    public void startRecording(View v) {
        btnStartRecording.setVisibility(View.INVISIBLE);
        btnStopRecording.setVisibility(View.VISIBLE);
        isRecording = true;
        recordTask = new RecordAudio();
        recordTask.execute();   //start new thread
        rb432.setEnabled(false);
        rb440.setEnabled(false);
    }

    //function to stop recording
    public void stopRecording(View v) {
        if (isRecording) {
            audioRecord.stop();
            isRecording = false;
            btnStopRecording.setVisibility(View.INVISIBLE);
            btnStartRecording.setVisibility(View.VISIBLE);
            recordTask.cancel(false);
            rb432.setEnabled(true);
            rb440.setEnabled(true);
        }
    }

    //new thread which starting recording, doing fft and show notes
    private class RecordAudio extends AsyncTask<Void, Double, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            int bufferReadResult;
            short[] buffer = new short[bufferSize];
            double[] toTransform = new double[bufferSize * 2];
            audioRecord.startRecording();

            fft = new RealDoubleFFT(bufferSize * 2);

            while (isRecording) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Maximal magnitude for 8000Hz sample rate is 3600Hz (More info on: http://wiki.audacityteam.org/wiki/Sample_Rates)
                //We use frequencies in range 216Hz - 880Hz
                double max_magnitude = -5000; //Max magnitude from specter (buffer) we recorded. This is initial value;
                double max_index = -1;
                double re;
                double im;
                double[] magnitude = new double[bufferSize / 2];

                bufferReadResult = audioRecord.read(buffer, 0, bufferSize);

                // copy real input data to complex toTransform buffer
                for (int i = 0; i < bufferSize - 1; i++) {
                    toTransform[2 * i] = (double) buffer[i];
                    toTransform[2 * i + 1] = 0;
                }

                //perform in-place complex-to-complex FFT on toTransform[] buffer
                fft.ft(toTransform);

                // calculate power spectrum (magnitude) values from fft[]
                for (int i = 0; i < bufferSize / 2 - 1; i++) {
                    re = toTransform[2 * i];
                    im = toTransform[2 * i + 1];
                    magnitude[i] = Math.sqrt(re * re + im * im);
                }

                // find largest peak in power spectrum
                for (int i = 0; i < bufferSize / 2 - 1; i++) {
                    if (magnitude[i] > max_magnitude) {
                        max_magnitude = magnitude[i];
                        max_index = i;
                    }
                }

                // convert index of largest peak to frequency
                double frequencyBla = max_index * frequency / bufferSize;

                publishProgress(frequencyBla);

            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Double... progress) {
            //write notes in text view


            freqCircularBuffer.store(progress[0]);
            if(++cntDisplayReading == 5) {
                mFrequence.setText(checkNoteFor440(freqCircularBuffer.getAverage()));   //displays info on display every thread_sleep*cntDisplayReadingMAX (currently 0.5*5 seconds)
                cntDisplayReading=0;
            }

            /*
            if(((RadioButton) findViewById(R.id.rb432)).isChecked())
                mFrequence.setText(checkNoteFor432(progress[0]));
            else
                mFrequence.setText(checkNoteFor440(progress[0]));
            */
        }

        @Override
        protected void onPostExecute(Boolean result) {
            audioRecord.stop();
        }
    }


    //function which set note
    public String checkNoteFor440(double freq) {
        DecimalFormat twoDecimal = new DecimalFormat("#.00");
        //check if frequency less than first note
        if (Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotesFor440.values()[0].getFrequency() - 6) {
            mFrequence.setTextColor(Color.RED);
            //return Double.valueOf(twoDecimal.format(freq)).toString();
            return ">>> " + FrequencyOfNotesFor440.values()[0].getName();
        }
        //check if frequency bigger than last note
        if (Double.valueOf(twoDecimal.format(freq)) > FrequencyOfNotesFor440.values()[FrequencyOfNotesFor440.values().length - 1].getFrequency() + 6) {
            mFrequence.setTextColor(Color.RED);
            //return Double.valueOf(twoDecimal.format(freq)).toString();
            return FrequencyOfNotesFor440.values()[FrequencyOfNotesFor440.values().length - 1].getName() + " <<<";
        }
        //check which note is current frequency
        for (int i = 0; i < FrequencyOfNotesFor440.values().length - 1; i++) {
            //if frequency same like some note
            if ((Double.valueOf(twoDecimal.format(freq)) >= FrequencyOfNotesFor440.values()[i].getFrequency() - 6)
                    && (Double.valueOf(twoDecimal.format(freq)) <= FrequencyOfNotesFor440.values()[i].getFrequency() + 6)) {
                if(
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A0) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A0B0) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B0) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C1D1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D1E1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F1G1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G1A1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A1B1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B1) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C2D2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D2E2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F2G2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G2A2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A2B2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B2) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C3D3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D3E3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F3G3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A3B3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B3) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C4D4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D4E4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F4G4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G4A4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A4B4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B4) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C5D5)||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D5E5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F5G5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G5A5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A5B5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B5) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C6D6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D6E6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F6G6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G6A6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A6B6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B6) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C7D7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D7E7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F7G7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G7A7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A7B7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.B7) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.C8D8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.D8E8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.E8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.F8G8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.G8A8) ||
                        (FrequencyOfNotesFor440.values()[i] == FrequencyOfNotesFor440.A8)) {
                    mFrequence.setTextColor(Color.GREEN);
                }else{
                    mFrequence.setTextColor(Color.RED);
                }
                //return Double.valueOf(twoDecimal.format(freq)).toString();
                return FrequencyOfNotesFor440.values()[i].getName();
            } else {
                //if frequency between two notes
                if (FrequencyOfNotesFor440.values()[i].getFrequency() + 6 < Double.valueOf(twoDecimal.format(freq))
                        && Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotesFor440.values()[i + 1].getFrequency() - 6) {
                    if (Double.valueOf(twoDecimal.format(freq)) - FrequencyOfNotesFor440.values()[i].getFrequency() <
                            FrequencyOfNotesFor440.values()[i + 1].getFrequency() - Double.valueOf(twoDecimal.format(freq))) {
                        mFrequence.setTextColor(Color.RED);
                        //return Double.valueOf(twoDecimal.format(freq)).toString();
                        return FrequencyOfNotesFor440.values()[i].getName();
                    } else {
                        mFrequence.setTextColor(Color.RED);
                        //return Double.valueOf(twoDecimal.format(freq)).toString();
                        return FrequencyOfNotesFor440.values()[i + 1].getName();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Function find which note is closest to measured frequency
     * @param freq Measured frequency
     * @return  Note from range of A3-A5
     */
    public String checkNoteFor432(double freq) {
        DecimalFormat twoDecimal = new DecimalFormat("#.00");
        //check if frequency less than first note
        if (Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotesFor432.values()[0].getFrequency() - 6) {
            mFrequence.setTextColor(Color.RED);
            //return Double.valueOf(twoDecimal.format(freq)).toString();
            return ">>> " + FrequencyOfNotesFor432.values()[0].getName();
        }
        //check if frequency bigger than last note
        if (Double.valueOf(twoDecimal.format(freq)) > FrequencyOfNotesFor432.values()[FrequencyOfNotesFor432.values().length - 1].getFrequency() + 6) {
            mFrequence.setTextColor(Color.RED);
            //return Double.valueOf(twoDecimal.format(freq)).toString();
            return FrequencyOfNotesFor432.values()[FrequencyOfNotesFor432.values().length - 1].getName() + " <<<";
        }
        //check which note is current frequency
        for (int i = 0; i < FrequencyOfNotesFor432.values().length - 1; i++) {
            //if frequency same like some note
            if ((Double.valueOf(twoDecimal.format(freq)) >= FrequencyOfNotesFor432.values()[i].getFrequency() - 6)
                    && (Double.valueOf(twoDecimal.format(freq)) <= FrequencyOfNotesFor432.values()[i].getFrequency() + 6)) {
                if(
                        (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A0) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A0B0) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B0) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C1D1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D1E1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F1G1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G1A1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A1B1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B1) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C2D2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D2E2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F2G2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G2A2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A2B2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B2) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C3D3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D3E3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F3G3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A3B3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B3) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C4D4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D4E4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F4G4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G4A4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A4B4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B4) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C5D5)||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D5E5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F5G5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G5A5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A5B5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B5) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C6D6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D6E6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F6G6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G6A6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A6B6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B6) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C7D7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D7E7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F7G7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G7A7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A7B7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.B7) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.C8D8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.D8E8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.E8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.F8G8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.G8A8) ||
                                (FrequencyOfNotesFor432.values()[i] == FrequencyOfNotesFor432.A8)) {

                    mFrequence.setTextColor(Color.GREEN);
                }else{
                    mFrequence.setTextColor(Color.RED);
                }
                return Double.valueOf(twoDecimal.format(freq)).toString();
                //return FrequencyOfNotes.values()[i].getName();
            } else {
                //if frequency between two notes
                if (FrequencyOfNotesFor432.values()[i].getFrequency() + 6 < Double.valueOf(twoDecimal.format(freq))
                        && Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotesFor432.values()[i + 1].getFrequency() - 6) {
                    if (Double.valueOf(twoDecimal.format(freq)) - FrequencyOfNotesFor432.values()[i].getFrequency() <
                            FrequencyOfNotesFor432.values()[i + 1].getFrequency() - Double.valueOf(twoDecimal.format(freq))) {
                        mFrequence.setTextColor(Color.RED);
                        return Double.valueOf(twoDecimal.format(freq)).toString();
                        //return FrequencyOfNotesFor432.values()[i].getName() + " <<<";
                    } else {
                        mFrequence.setTextColor(Color.RED);
                        return Double.valueOf(twoDecimal.format(freq)).toString();
                        //return ">>> " + FrequencyOfNotes.values()[i + 1].getName();
                    }
                }
            }
        }

        return null;
    }

    /**
     * This class represents circular buffer. Starts from zero, and after last element cursor starts again from zero.
     * There is no method for deleting elements. New elements override old ones on current position.
     * Author: Milorad
     */
    class CircularBuffer {
        private double data[];
        private int head;

        public CircularBuffer(Integer number) {
            data = new double[number];
            head = 0;
        }
        /**
         * Adding new element on next position.
         */
        public void store(double value) {
            data[head++] = value;
            if (head == bufferLength) {
                head = 0;
            }
        }
        /**
         * Counts by algorithm average value among buffer elements.
         */
        public double getAverage() {
            double average = 0; //average value
            double retVal = 0;  //return value
            int cntRetVal = 0;  //counter for values that stays in deviation range
            for (int i = 0; i<bufferLength; i++) {
                average += data[i];

            }
            average = average/bufferLength;



            /*
            //Remove elements with high deviation from counting
            for (int i = 0; i<bufferLength; i++)
                if(data[i] < average*1.1 && data[i] > average*0.9) {
                    retVal += data[i];
                    cntRetVal++;
                }
            return retVal/cntRetVal;
            */
            return average;
        }
    }
}
