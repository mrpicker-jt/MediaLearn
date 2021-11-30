package com.jt.medialearn1.manager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PcmPlayManager {
    AudioTrack audioTrack;

    int sampleRateInHz;
    int channelConfig;
    int audioFormat;

    public PcmPlayManager(int sampleRateInHz, int channelConfig, int audioFormat) {
        this.sampleRateInHz = sampleRateInHz;
        this.channelConfig = channelConfig;
        this.audioFormat = audioFormat;
    }

    public void playStatic(File audio) {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat,
                (int) audio.length(),
                AudioTrack.MODE_STATIC);
        byte[] bytes = FileIOUtils.readFile2BytesByChannel(audio);
        audioTrack.write(bytes, 0, bytes.length);
        audioTrack.play();
    }

    public void playStream(File audioFile) {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
        int minSize = AudioTrack.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_MONO, audioFormat);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat,
                minSize,
                AudioTrack.MODE_STREAM);
        ThreadUtils.getSinglePool().execute(() -> {
            FileInputStream fio = null;
            try {
                fio = new FileInputStream(audioFile);
                byte[] buffer = new byte[minSize];
                while (fio.available() > 0) {
                    int readCount = fio.read(buffer);
                    if (readCount > 0) {
                        audioTrack.write(buffer, 0, readCount);
                        audioTrack.play();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fio != null) {
                    try {
                        fio.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
