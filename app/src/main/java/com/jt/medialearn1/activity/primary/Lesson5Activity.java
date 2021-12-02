package com.jt.medialearn1.activity.primary;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.jt.medialearn1.R;
import com.jt.medialearn1.util.MediaCodeUtil;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;

public class Lesson5Activity extends AppCompatActivity {
    File mp4File;
    StandardGSYVideoPlayer videoPlayer;

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
        rawAudioTrackPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "video.mp4";
        rawAudioTrackPath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "new.mp4";

        if (!FileUtils.isFileExists(mp4File)) {
            ResourceUtils.copyFileFromAssets("test.mp4", mp4File.getAbsolutePath());
        }
        videoPlayer = findViewById(R.id.video_view);
        videoPlayer.setUp(mp4File.getAbsolutePath(), true, "Test");
    }

    public void lesson5Click(View view) {
        switch (view.getId()) {
            case R.id.raw_split_mp4:
                MediaCodeUtil.rawSplitMp4(mp4File.getAbsolutePath(), rawVideoTrackPath, rawAudioTrackPath);
                break;
            case R.id.audio_split_mp4:
                MediaCodeUtil.writeAudioTrackFile(mp4File.getAbsolutePath(),dstAudioPath);
                break;
            case R.id.video_split_mp4:
                break;
            case R.id.merge_mp4:
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }
}