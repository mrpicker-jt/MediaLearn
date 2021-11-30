package com.jt.medialearn1.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcmRecordManager {

    boolean isRecording;

    AudioRecord audioRecord;
    int readBufferSize;
    byte[] data;
    FileOutputStream pcmFos;

    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    RecordListener listener;

    public PcmRecordManager(int sampleRateInHz, int channelConfig, int audioFormat) {
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
        readBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        data = new byte[readBufferSize];
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    public void startRecord(String dstPath, RecordListener listener) {
        if (isRecording) {
            return;
        }
        this.listener = listener;
        try {
            pcmFos = new FileOutputStream(dstPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            listener.onFinished(-1, e.getMessage());
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, readBufferSize);
        audioRecord.startRecording();
        isRecording = true;
        listener.onStart();
        beginPcmThread();
    }

    private void beginPcmThread() {
        ThreadUtils.getSinglePool().execute(() -> {
            while (isRecording) {
                int read = audioRecord.read(data, 0, readBufferSize);
                Log.d("TAG", read + "");
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        pcmFos.write(data);
                    } catch (IOException e) {
                        Log.d("TAG", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            try {
                audioRecord.stop();
                audioRecord.release();
                pcmFos.close();
                listener.onFinished(0, null);
            } catch (IOException e) {
                listener.onFinished(-1, e.getMessage());
                Log.d("TAG", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void stopRecord() {
        isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public interface RecordListener {
        void onStart();

        void onFinished(int code, String error);
    }

}
