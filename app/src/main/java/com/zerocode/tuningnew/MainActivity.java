package com.zerocode.tuningnew;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zerocode.fft.RealDoubleFFT;

import java.text.DecimalFormat;

public class MainActivity extends ActionBarActivity {
    private boolean isRecording = false;    //is Recording started or not
    private Button btnStartRecording;   //button for start recording
    private Button btnStopRecording;    //button for stop recording

    private int frequency = 8000;   //sample rate
    private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //keep screen on when app open

    }

    //function to start recording
    public void startRecording(View v) {
        btnStartRecording.setVisibility(View.INVISIBLE);
        btnStopRecording.setVisibility(View.VISIBLE);
        isRecording = true;
        recordTask = new RecordAudio();
        recordTask.execute();   //start new thread
    }

    //function to start recording
    public void stopRecording(View v) {
        if (isRecording) {
            audioRecord.stop();
            isRecording = false;
            btnStopRecording.setVisibility(View.INVISIBLE);
            btnStartRecording.setVisibility(View.VISIBLE);
            recordTask.cancel(false);
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
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                double max_magnitude = -5000;
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
            mFrequence.setText(checkNote(progress[0]));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            audioRecord.stop();
        }
    }


    //function which set note
    public String checkNote(double freq) {
        DecimalFormat twoDecimal = new DecimalFormat("#.00");
        //check if frequency less than first note
        if (Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotes.values()[0].getFrequency() - 6) {
            mFrequence.setTextColor(Color.RED);
            return ">>> " + FrequencyOfNotes.values()[0].getName();
        }
        //check if frequency bigger than last note
        if (Double.valueOf(twoDecimal.format(freq)) > FrequencyOfNotes.values()[FrequencyOfNotes.values().length - 1].getFrequency() + 6) {
            mFrequence.setTextColor(Color.RED);
            return FrequencyOfNotes.values()[FrequencyOfNotes.values().length - 1].getName() + " <<<";
        }
        //check which note is current frequency
        for (int i = 0; i < FrequencyOfNotes.values().length - 1; i++) {
            //if frequency sama like some note
            if ((Double.valueOf(twoDecimal.format(freq)) >= FrequencyOfNotes.values()[i].getFrequency() - 6)
                    && (Double.valueOf(twoDecimal.format(freq)) <= FrequencyOfNotes.values()[i].getFrequency() + 6)) {
                if((FrequencyOfNotes.values()[i] == FrequencyOfNotes.E4) || (FrequencyOfNotes.values()[i] == FrequencyOfNotes.F4) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.F4G4) ||(FrequencyOfNotes.values()[i] == FrequencyOfNotes.A4) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.B4) ||(FrequencyOfNotes.values()[i] == FrequencyOfNotes.C5) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.C5D5) || (FrequencyOfNotes.values()[i] == FrequencyOfNotes.E5) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.F5) || (FrequencyOfNotes.values()[i] == FrequencyOfNotes.F5G5)||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.A3) ||(FrequencyOfNotes.values()[i] == FrequencyOfNotes.A5) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.B3) ||(FrequencyOfNotes.values()[i] == FrequencyOfNotes.C4) ||
                        (FrequencyOfNotes.values()[i] == FrequencyOfNotes.C4D4)) {
                    mFrequence.setTextColor(Color.GREEN);
                }else{
                    mFrequence.setTextColor(Color.RED);
                }
                return FrequencyOfNotes.values()[i].getName();
            } else {
                //if frequency between two notes
                if (FrequencyOfNotes.values()[i].getFrequency() + 6 < Double.valueOf(twoDecimal.format(freq))
                        && Double.valueOf(twoDecimal.format(freq)) < FrequencyOfNotes.values()[i + 1].getFrequency() - 6) {
                    if (Double.valueOf(twoDecimal.format(freq)) - FrequencyOfNotes.values()[i].getFrequency() <
                            FrequencyOfNotes.values()[i + 1].getFrequency() - Double.valueOf(twoDecimal.format(freq))) {
                        mFrequence.setTextColor(Color.RED);
                        return FrequencyOfNotes.values()[i].getName() + " <<<";
                    } else {
                        mFrequence.setTextColor(Color.RED);
                        return ">>> " + FrequencyOfNotes.values()[i + 1].getName();
                    }
                }
            }
        }

        return null;
    }
}
