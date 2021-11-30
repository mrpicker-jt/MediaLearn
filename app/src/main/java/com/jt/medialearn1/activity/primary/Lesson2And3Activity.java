package com.jt.medialearn1.activity.primary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jt.medialearn1.R;
import com.jt.medialearn1.manager.PcmPlayManager;
import com.jt.medialearn1.manager.PcmRecordManager;
import com.jt.medialearn1.util.PcmToWavUtil;

import java.io.File;
import java.io.IOException;

public class Lesson2And3Activity extends AppCompatActivity {

    final String TAG = this.getClass().getSimpleName();


    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    PcmRecordManager pcmRecordManager;
    PcmPlayManager pcmPlayManager;


    File pcmFile;
    File wavFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson2_and_3);
        init();
    }

    private void init() {
        pcmRecordManager = new PcmRecordManager(sampleRateInHz, channelConfig, audioFormat);
        pcmPlayManager = new PcmPlayManager(sampleRateInHz, channelConfig, audioFormat);
        pcmFile = new File(getExternalFilesDir(null), "test.pcm");
        wavFile = new File(getExternalFilesDir(null), "test.wav");

    }

    private void startRecord() {
        PermissionUtils.permission(Manifest.permission.RECORD_AUDIO).callback(new PermissionUtils.SimpleCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {
                pcmRecordManager.startRecord(pcmFile.getAbsolutePath(), new PcmRecordManager.RecordListener() {
                    @Override
                    public void onStart() {
                        ((TextView) findViewById(R.id.record_start_stop)).setText("stop record pcm");
                    }

                    @Override
                    public void onFinished(int code, String error) {
                        runOnUiThread(() -> {
                            ((TextView) findViewById(R.id.record_start_stop)).setText("start record pcm");
                            if (code == 0) {
                                ToastUtils.showShort("录制PCM成功");
                            } else {
                                ToastUtils.showShort(error);
                            }
                        });
                    }
                });
            }

            @Override
            public void onDenied() {

            }
        }).request();
    }


    private void stopRecord() {
        pcmRecordManager.stopRecord();
    }

    private void convertWav() {
        if (!FileUtils.isFileExists(pcmFile)) {
            return;
        }
        ThreadUtils.getSinglePool().execute(() -> {
            FileUtils.delete(wavFile);
            try {
                PcmToWavUtil.PCMToWAV(pcmFile, wavFile, 1, sampleRateInHz, 16);
                ToastUtils.showShort("转换成功");
            } catch (IOException e) {
                ToastUtils.showShort(e.getMessage());
                e.printStackTrace();
            }
        });
    }


    public void click(View view) {
        switch (view.getId()) {
            case R.id.record_start_stop:
                if (pcmRecordManager.isRecording()) {
                    stopRecord();
                } else {
                    startRecord();
                }
                break;
            case R.id.convert_wav:
                convertWav();
                break;
            case R.id.play_pcm_static:
                pcmPlayManager.playStatic(pcmFile);
                break;
            case R.id.play_pcm_stream:
                pcmPlayManager.playStream(pcmFile);
                break;
        }
    }
}