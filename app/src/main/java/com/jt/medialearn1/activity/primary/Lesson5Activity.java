package com.jt.medialearn1.activity.primary;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.jt.medialearn1.R;
import com.jt.medialearn1.util.AudioUtil;
import com.jt.medialearn1.util.MediaExtractUtil;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;

public class Lesson5Activity extends AppCompatActivity {
    File mp4File;
    StandardGSYVideoPlayer videoPlayer;
    StandardGSYVideoPlayer outputVideoPlayer;

    String rawVideoTrackPath;
    String rawAudioTrackPath;

    String dstAudioPath;
    String dstVideoPath;
    String newMp4Path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson5);
        mp4File = new File(getExternalFilesDir(null), "test.mp4");
        rawVideoTrackPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "testVideo.track";
        rawAudioTrackPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "testAudio.track";
        dstAudioPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "audio.wav";
        dstVideoPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "video.mp4";
        newMp4Path = getExternalFilesDir(null).getAbsolutePath() + File.separator + "new.mp4";

        if (!FileUtils.isFileExists(mp4File)) {
            ResourceUtils.copyFileFromAssets("test.mp4", mp4File.getAbsolutePath());
        }
        videoPlayer = findViewById(R.id.video_view);
        videoPlayer.setUp(mp4File.getAbsolutePath(), true, "Test");

        outputVideoPlayer = findViewById(R.id.video_view_merge);
    }

    public void lesson5Click(View view) {
        switch (view.getId()) {
            case R.id.raw_split_mp4:
                MediaExtractUtil.rawSplitMp4(mp4File.getAbsolutePath(), rawVideoTrackPath, rawAudioTrackPath);
                break;
            case R.id.audio_split_mp4:
                MediaExtractUtil.writeAudioTrackFile(mp4File.getAbsolutePath(), dstAudioPath);
                break;
            case R.id.video_split_mp4:
                MediaExtractUtil.writeVideoTrackFile(mp4File.getAbsolutePath(), dstVideoPath,
                        val -> {
                            if (val == 0) {
                                outputVideoPlayer.setUp(dstVideoPath, true, "Split Video");
                                outputVideoPlayer.startPlayLogic();
                            }
                        });
                break;
            case R.id.merge_mp4:
                MediaExtractUtil.splitAndMergeMp4(mp4File.getAbsolutePath(), newMp4Path,
                        val -> {
                            if (val == 0) {
                                outputVideoPlayer.setUp(newMp4Path, true, "Merged Video");
                                outputVideoPlayer.startPlayLogic();
                            }
                        });
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
        outputVideoPlayer.onVideoPause();
        AudioUtil.stopPlayAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
        outputVideoPlayer.onVideoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }
}