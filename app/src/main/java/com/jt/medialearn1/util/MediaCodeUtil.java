package com.jt.medialearn1.util;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodeUtil {
    static final String TAG = MediaCodeUtil.class.getSimpleName();

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

                MediaFormat videoFormat = mediaExtractor.
                        getTrackFormat(videoTrackIdx);
                MediaFormat audioFormat = mediaExtractor.getTrackFormat(audioTrackIdx);

//                MediaCodec videoMCc = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));
//                videoMCc.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//                ByteBuffer inputBuffer = videoMCc.getInputBuffer(0);

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
                int frameRate = -1;
                int audioTrackIdx = -1;
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                        mediaExtractor.selectTrack(i);
                        mediaMuxer = new MediaMuxer(dstAudioPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        audioTrackIdx = mediaMuxer.addTrack(mediaFormat);
                    }
                    if (mediaFormat.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                        frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                    }
                }
                if (mediaMuxer == null) {
                    Log.d(TAG,"mediaMuxer null return");
                    return;
                }
                mediaMuxer.start();

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                bufferInfo.presentationTimeUs = 0;
                ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
                int readSize = 0;
                while ((readSize = mediaExtractor.readSampleData(byteBuffer, 0)) > 0) {
                    bufferInfo.offset = 0;
                    bufferInfo.size = readSize;
                    bufferInfo.flags = mediaExtractor.getSampleFlags();
                    bufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;
                    mediaMuxer.writeSampleData(audioTrackIdx, byteBuffer, bufferInfo);
                    mediaExtractor.advance();
                }
                mediaExtractor.release();

                mediaMuxer.stop();
                mediaMuxer.release();

                ThreadUtils.runOnUiThread(() -> ToastUtils.showShort("分离音频结束"));
                AudioUtil.playAudio(dstAudioPath);

            } catch (IOException e) {
                Log.d(TAG,"Error: "+e.getMessage());
                e.printStackTrace();
            }

        });
    }

}
