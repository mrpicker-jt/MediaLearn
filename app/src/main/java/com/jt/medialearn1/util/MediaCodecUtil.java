package com.jt.medialearn1.util;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.jt.medialearn1.interfaces.StringConsumer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodecUtil {
    static final String TAG = MediaCodecUtil.class.getSimpleName();
    static final int TIMEOUT_USEC = 10000;
    static long presentationTimeUs = 0;

    public static void pcmToAAC(int sampleRate, int channelCount, String pcmPath, String aacPath, StringConsumer consumer) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int[] audioTrackIdx = {-1};
        byte[] bytes = new byte[1024];
        ThreadUtils.getSinglePool().execute(() -> {
            try {
                MediaCodec encoder = createMediaCodec(sampleRate, channelCount);
                MediaMuxer mediaMuxer = new MediaMuxer(aacPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                encoder.start();
                FileInputStream fileInputStream = new FileInputStream(pcmPath);
                while (fileInputStream.available() > 0) {
                    int size = fileInputStream.read(bytes);
                    buffer.clear();
                    if (size > 0) {
                        buffer.put(bytes);
                        buffer.position(size);
                        buffer.flip();
                    }
                    int inputBufferIdx = encoder.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufferIdx >= 0) {
                        ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIdx);
                        inputBuffer.clear();
                        inputBuffer.put(buffer);
                        if (size <= 0) {
                            Log.d(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                            encoder.queueInputBuffer(inputBufferIdx, 0, 0, getPTSUs(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            encoder.queueInputBuffer(inputBufferIdx, 0, size, getPTSUs(), 0);
                        }
                    } else if (inputBufferIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    }
                    int encodeStatus;
                    do {
                        encodeStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                        if (encodeStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

                        } else if (encodeStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            audioTrackIdx[0] = mediaMuxer.addTrack(encoder.getOutputFormat());
                            mediaMuxer.start();
                        } else if (encodeStatus < 0) {
                            Log.e(TAG, "encoderStatus < 0");
                        } else {
                            ByteBuffer byteBuffer = encoder.getOutputBuffer(encodeStatus);
                            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                bufferInfo.size = 0;
                            }
                            if (bufferInfo.size != 0 && audioTrackIdx[0] >= 0) {
                                bufferInfo.presentationTimeUs = getPTSUs();
                                Log.d(TAG, "发送音频数据 " + bufferInfo.size);
                                mediaMuxer.writeSampleData(audioTrackIdx[0], byteBuffer, bufferInfo);
                                presentationTimeUs = bufferInfo.presentationTimeUs;
                            }
                            encoder.releaseOutputBuffer(encodeStatus, false);
                        }
                    } while (encodeStatus >= 0);
                }
                encoder.stop();
                encoder.release();
                mediaMuxer.stop();
                mediaMuxer.release();
                consumer.accept(aacPath);
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
                consumer.accept(e.getMessage());
                e.printStackTrace();
            }

        });
    }

    /**
     * get next encoding presentationTimeUs
     *
     * @return
     */
    private static long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        // presentationTimeUs should be monotonic
        // otherwise muxer fail to write
        if (result < presentationTimeUs)
            result = (presentationTimeUs - result) + result;
        return result;
    }

    private static MediaCodec createMediaCodec(int sampleRate, int channelCount) throws IOException {
        String mimeType = MediaFormat.MIMETYPE_AUDIO_AAC;
        MediaCodec encoder = MediaCodec.createEncoderByType(mimeType);
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        format.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * 4);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return encoder;
    }
}
