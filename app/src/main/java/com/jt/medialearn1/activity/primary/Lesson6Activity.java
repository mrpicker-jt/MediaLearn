package com.jt.medialearn1.activity.primary;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jt.medialearn1.R;
import com.jt.medialearn1.util.AudioUtil;
import com.jt.medialearn1.util.MediaCodecUtil;

import java.io.File;

public class Lesson6Activity extends AppCompatActivity {
    String pcmPath;
    String aacPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson6);
        pcmPath = new File(getExternalFilesDir(null), "test.pcm").getAbsolutePath();
        aacPath = new File(getExternalFilesDir(null), "test.aac").getAbsolutePath();
    }

    public void lesson6Click(View view) {
        switch (view.getId()) {
            case R.id.convert_pcm_to_aac:
                MediaCodecUtil.pcmToAAC(44100, 1, pcmPath, aacPath,
                        val -> ThreadUtils.runOnUiThread(() ->{
                            ToastUtils.showShort(val);
                            AudioUtil.playAudio(val);
                        }));
                break;
        }
    }
}