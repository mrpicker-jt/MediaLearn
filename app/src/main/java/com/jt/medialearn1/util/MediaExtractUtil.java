package com.jt.medialearn1.util;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jt.medialearn1.interfaces.IntConsumer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@SuppressLint("WrongConstant")
public class MediaExtractUtil {
    static final String TAG = MediaExtractUtil.class.getSimpleName();

    public static void rawSplitMp4(String mp4Path, String videoTrackPath,
                                   String audioTrackPath) {
        ThreadUtils.getSinglePool().execute(() -> {
            MediaExtractor mediaExtractor = new MediaExtractor();
            FileOutputStream videoOutFs = null;
            FileOutputStream audioOutFs = null;
            try {
                videoOutFs = new FileOutputStream(videoTrackPath);
                audioOutFs = new FileOutputStream(audioTrackPath);
                mediaExtractor.setDataSource(mp4Path);

                int trackCount = mediaExtractor.getTrackCount();
                int audioTrackIdx = -1, videoTrackIdx = -1;
                for (int i = 0; i < trackCount; i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);

                    Log.d(TAG, "mime:" + mime);
                    if (mime.startsWith("video/")) {
                        int width = format.getInteger(MediaFormat.KEY_WIDTH);
                        int height = format.getInteger(MediaFormat.KEY_HEIGHT);
                        Log.d(TAG, "width: " + width + " height " + height);
                        videoTrackIdx = i;
                    }
                    if (mime.startsWith("audio/")) {
                        audioTrackIdx = i;
                    }
                }

                ByteBuffer inputBuffer = ByteBuffer.allocate(500 * 1024);
                int readSize;
                //选择视频通道
                mediaExtractor.selectTrack(videoTrackIdx);
                while ((readSize = mediaExtractor.readSampleData(inputBuffer, 0)) > 0) {
                    byte[] bytes = new byte[readSize];
//                    Log.d(TAG, "video---" + "readSize:" + readSize + " sampleTrackIdx:"
//                            + mediaExtractor.getSampleTrackIndex() + " sampleTime:" + mediaExtractor.getSampleTime());
                    inputBuffer.get(bytes);
                    videoOutFs.write(bytes);
                    inputBuffer.clear();
                    mediaExtractor.advance();
                }

                //选择音频通道
                mediaExtractor.selectTrack(audioTrackIdx);
                while ((readSize = mediaExtractor.readSampleData(inputBuffer, 0)) > 0) {
                    byte[] bytes = new byte[readSize];
//                    Log.d(TAG, "audio---" + "readSize:" + readSize + " sampleTrackIdx:"
//                            + mediaExtractor.getSampleTrackIndex() + " sampleTime:" + mediaExtractor.getSampleTime());
                    inputBuffer.get(bytes);
                    audioOutFs.write(bytes);
                    inputBuffer.clear();
                    mediaExtractor.advance();
                }
                ThreadUtils.runOnUiThread(() -> ToastUtils.showShort("rawSplitMp4 finish"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mediaExtractor.release();
                if (videoOutFs != null) {
                    try {
                        videoOutFs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (audioOutFs != null) {
                    try {
                        audioOutFs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void writeAudioTrackFile(String mp4Path, String dstAudioPath) {
        ThreadUtils.getSinglePool().execute(() -> {
            MediaMuxer mediaMuxer = null;
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(mp4Path);
                int audioTrackIdx = -1;
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                        mediaExtractor.selectTrack(i);
                        mediaMuxer = new MediaMuxer(dstAudioPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        audioTrackIdx = mediaMuxer.addTrack(mediaFormat);
                    }
                }
                if (mediaMuxer == null) {
                    Log.d(TAG, "mediaMuxer null return");
                    return;
                }
                mediaMuxer.start();

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                int readSize;
                while ((readSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0) {
                    bufferInfo.offset = 0;
                    bufferInfo.size = readSize;
                    bufferInfo.flags = mediaExtractor.getSampleFlags();
                    bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                    mediaMuxer.writeSampleData(audioTrackIdx, byteBuffer, bufferInfo);
                    mediaExtractor.advance();
                }
                mediaExtractor.release();

                mediaMuxer.stop();
                mediaMuxer.release();

                ThreadUtils.runOnUiThread(() -> ToastUtils.showShort("分离音频结束"));
                AudioUtil.playAudio(dstAudioPath);

            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
            }

        });
    }


    public static void writeVideoTrackFile(String mp4Path, String dstVideoPath, IntConsumer consumer) {
        ThreadUtils.getSinglePool().execute(() -> {
            MediaMuxer mediaMuxer = null;
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(mp4Path);
                int videoTrackIdx = -1;
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        mediaExtractor.selectTrack(i);
                        mediaMuxer = new MediaMuxer(dstVideoPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        videoTrackIdx = mediaMuxer.addTrack(mediaFormat);
                    }
                }
                if (mediaMuxer == null) {
                    Log.d(TAG, "mediaMuxer null return");
                    return;
                }
                mediaMuxer.start();

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.presentationTimeUs = 0;
                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                int readSize;
                while ((readSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0) {
                    bufferInfo.offset = 0;
                    bufferInfo.size = readSize;
                    bufferInfo.flags = mediaExtractor.getSampleFlags();
                    bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                    mediaMuxer.writeSampleData(videoTrackIdx, byteBuffer, bufferInfo);
                    mediaExtractor.advance();
                }
                mediaExtractor.release();

                mediaMuxer.stop();
                mediaMuxer.release();

                ThreadUtils.runOnUiThread(() -> {
                    ToastUtils.showShort("分离视频结束");
                    consumer.accept(0);
                });
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
                consumer.accept(-1);
            }

        });
    }

    public static void splitAndMergeMp4(String mp4Path, String dstMp4Path, IntConsumer consumer) {
        ThreadUtils.getSinglePool().execute(() -> {
            MediaMuxer mediaMuxer = null;
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(mp4Path);
                int originAudioTrackIdx = -1;
                int originVideoTrackIdx = -1;
                int audioTrackIdx = -1;
                int videoTrackIdx = -1;
                mediaMuxer = new MediaMuxer(dstMp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        originVideoTrackIdx = i;
                        videoTrackIdx = mediaMuxer.addTrack(mediaFormat);
                    }

                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                        originAudioTrackIdx = i;
                        audioTrackIdx = mediaMuxer.addTrack(mediaFormat);
                    }
                }
                int readSize;
                mediaMuxer.start();

                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
                audioBufferInfo.presentationTimeUs = 0;
                ByteBuffer audioBuffer = ByteBuffer.allocate(500 * 1024);
                if (originAudioTrackIdx != -1) {
                    mediaExtractor.selectTrack(originAudioTrackIdx);
                    while ((readSize = mediaExtractor.readSampleData(audioBuffer, 0)) > 0) {
                        audioBufferInfo.offset = 0;
                        audioBufferInfo.size = readSize;
                        audioBufferInfo.flags = mediaExtractor.getSampleFlags();
                        audioBufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                        mediaMuxer.writeSampleData(audioTrackIdx, audioBuffer, audioBufferInfo);
                        mediaExtractor.advance();
                    }
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.presentationTimeUs = 0;
                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

                if (originVideoTrackIdx != -1) {
                    mediaExtractor.selectTrack(originVideoTrackIdx);
                    while ((readSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = readSize;
                        bufferInfo.flags = mediaExtractor.getSampleFlags();
                        bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                        mediaMuxer.writeSampleData(videoTrackIdx, byteBuffer, bufferInfo);
                        mediaExtractor.advance();
                    }
                }

                mediaExtractor.release();

                mediaMuxer.stop();
                mediaMuxer.release();

                ThreadUtils.runOnUiThread(() -> {
                    ToastUtils.showShort("合并结束");
                    consumer.accept(0);
                });
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
                consumer.accept(-1);
            }

        });
    }


    public static void splitAndMergeMp4(String audioPath, String videoPath, String dstMp4Path, IntConsumer consumer) {
        ThreadUtils.getSinglePool().execute(() -> {
            MediaMuxer mediaMuxer = null;
            MediaExtractor videoExtractor = new MediaExtractor();
            MediaExtractor audioExtractor = new MediaExtractor();
            try {
                videoExtractor.setDataSource(videoPath);
                audioExtractor.setDataSource(audioPath);

//                int frameRate = -1;
                int maxFrameSize = 0;
                int maxAudioSize = 0;
                int originAudioTrackIdx = -1;
                int originVideoTrackIdx = -1;
                int audioTrackIdx = -1;
                int videoTrackIdx = -1;
                mediaMuxer = new MediaMuxer(dstMp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                for (int i = 0; i < videoExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = videoExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        originVideoTrackIdx = i;
                        videoTrackIdx = mediaMuxer.addTrack(mediaFormat);
//                        frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                        maxFrameSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    }
                }

                for (int i = 0; i < audioExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = audioExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                        originAudioTrackIdx = i;
                        audioTrackIdx = mediaMuxer.addTrack(mediaFormat);
                        maxAudioSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                    }
                }

                int readSize;
                mediaMuxer.start();

                Log.d(TAG, "merge audio start " + audioTrackIdx + " ---------------------------------------------------------------");
                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer audioBuffer = ByteBuffer.allocate(maxAudioSize);
                if (originAudioTrackIdx != -1) {
                    audioExtractor.selectTrack(originAudioTrackIdx);
                    while ((readSize = audioExtractor.readSampleData(audioBuffer, 0)) > 0) {
                        audioBufferInfo.offset = 0;
                        audioBufferInfo.size = readSize;
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        Log.d(TAG, "audio--" + "flags: " + audioBufferInfo.flags + " timeUs:" + audioBufferInfo.presentationTimeUs);
                        mediaMuxer.writeSampleData(audioTrackIdx, audioBuffer, audioBufferInfo);
                        audioExtractor.advance();
                    }
                }


                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer byteBuffer = ByteBuffer.allocate(maxFrameSize);

                Log.d(TAG, "merge video start " + videoTrackIdx + " --------------------------------------------------------------");
                if (originVideoTrackIdx != -1) {
                    videoExtractor.selectTrack(originVideoTrackIdx);
                    while ((readSize = videoExtractor.readSampleData(byteBuffer, 0)) > 0) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = readSize;
                        bufferInfo.flags = videoExtractor.getSampleFlags();
                        bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        Log.d(TAG, "video--" + "flags: " + bufferInfo.flags + " timeUs:" + bufferInfo.presentationTimeUs);
                        mediaMuxer.writeSampleData(videoTrackIdx, byteBuffer, bufferInfo);
                        videoExtractor.advance();
                    }
                }

                audioExtractor.release();
                videoExtractor.release();

                mediaMuxer.stop();
                mediaMuxer.release();

                ThreadUtils.runOnUiThread(() -> {
                    ToastUtils.showShort("合并结束");
                    consumer.accept(0);
                });
            } catch (IOException e) {
                Log.d(TAG, "Error: " + e.getMessage());
                e.printStackTrace();
                consumer.accept(-1);
            }

        });
    }

}
