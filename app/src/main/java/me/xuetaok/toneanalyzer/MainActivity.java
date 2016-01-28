package me.xuetaok.toneanalyzer;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;

import android.media.MediaRecorder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.TextView;
import org.jtransforms.fft.DoubleFFT_1D;



public class MainActivity extends Activity {

    private boolean isRecording = false;
    private AudioRecord audioInput = null;
    private int bufferSize=0;
    private int SampleRate = 44100;
    private int blockSize = 400000;
    //private int blockSize = Integer.MAX_VALUE/2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonHandlers();
        enableButtons(false);
        bufferSize = AudioRecord.getMinBufferSize(SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    private void setButtonHandlers() {
        (findViewById(R.id.startBtn)).setOnClickListener(btnClick);
        (findViewById(R.id.resetBtn)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        (findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.startBtn, !isRecording);
        enableButton(R.id.resetBtn, isRecording);
    }

    private void stopRecording() {
        if(audioInput!=null){
            isRecording=false;
            audioInput.stop();
            audioInput.release();
            audioInput = null;
        }
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.startBtn: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.resetBtn: {
                    enableButtons(false);
                    stopRecording();
                    resetTest(v);
                    break;
                }
            }
        }
    };

    public void startRecording()
    {
        TextView t=(TextView)findViewById(R.id.resultLabel);
        isRecording=true;
        audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, SampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, blockSize);
        short[] buffer = new short[blockSize];


        //while(isRecording) {
            try {
                audioInput.startRecording();

                final int bufferReadResult= audioInput.read(buffer, 0, blockSize);
              //  final double[] toTransform = new double[bufferSize];

                double fft[]=new double[blockSize];

                DoubleFFT_1D fftData = new DoubleFFT_1D(blockSize);
                buffer=HanningWindow(buffer,0,blockSize);
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    fft[i] = (double) (buffer[i] / 32768.0F); // signed 16 bit
                }

                fftData.realForward(fft);

                //Log.v("Recording!", String.valueOf(bufferSize));
                double[] magnitude = new double[blockSize];

                for (int i = 0; i < blockSize/2; i++) {
                    magnitude[i] = Math.sqrt((fft[2*i] * fft[2*i]) + (fft[2*i+1] * fft[2*i+1]));
                }

                double peak_val = Double.MIN_VALUE;
                int peak = -1;
                for (int i = 0; i < blockSize/2; i++) {
                    double val = magnitude[i];

                    if (val > peak_val && SampleRate*i/blockSize >= 200 && SampleRate*i/blockSize <=4000) {
                        peak_val = val;
                        peak = i;
                    }
                }
               /* for(double d: fft) {
                    System.out.println(d);
                }*/

                //double freq = (SampleRate * peak)/blockSize;
                double freq = Math.round((SampleRate * peak) / blockSize);
                if (freq >= 200 && freq <=4000) {
                    t.setText(String.valueOf(freq));
                }
                else{
                    t.setText("Out of Range!");
                }
                //int  = (int) Math.round(doubleVar);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            finally {
                stopRecording();
            }

       // }
    }

    public short[] HanningWindow(short[] signal_in, int pos, int size)
    {
        for (int i = pos; i < pos + size; i++)
        {
            int j = i - pos;
            signal_in[i] = (short) (signal_in[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size)));
        }
        return signal_in;
    }

    private void resetTest(View v)
    {
        TextView t=(TextView)findViewById(R.id.resultLabel);
        t.setText("Ready!");
    }

}
