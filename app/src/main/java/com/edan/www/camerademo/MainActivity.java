package com.edan.www.camerademo;

import android.Manifest;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final CharSequence CAMERA_FONT = "0";
    private static final CharSequence CAMERA_BACK = "1";
    private CameraManager mCameraManager;
    private String mCameraId;
    private ImageReader mImageReader;
    private Handler mBackgroundHandler;
    private AssistStructure.ViewNode mPreviewSize;
    private CameraCharacteristics mCharacteristics;
    private boolean mFlashSupported;
    private Handler mMainHandler;
    private MediaProjectionManager mediaProjectionManager;

    boolean isrun = false;//用来标记录屏的状态private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;//录制视频的工具private int width, height, dpi;//屏幕宽高和dpi，后面会用到
    private ScreenRecorder screenRecorder;//这个是自己写的录视频的工具类，下文会放完整的代码
    Thread thread;//录视频要放在线程里去执行
    private int mWidth;
    private int mHeight;
    private int mDpi;
    private Button mStartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
       checkPermission();
    }

    private void init() {

        mStartBtn = (Button) findViewById(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        mWidth = outMetrics.widthPixels;

        mHeight = outMetrics.heightPixels;
        mDpi = outMetrics.densityDpi;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 102);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 103);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 104);
        }
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = mediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, 101);//正常情况是要执行到这里的,作用是申请捕捉屏幕
        } else {
            Toast.makeText(this, "Android版本太低，无法使用该功能", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 102) {
            Toast.makeText(this, "缺少读写权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 103) {
            Toast.makeText(this, "缺少录音权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == 104) {
            Toast.makeText(this, "缺少相机权限", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode != 101) {
            Log.e("HandDrawActivity", "error requestCode =" + requestCode);
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "捕捉屏幕被禁止", Toast.LENGTH_SHORT).show();
            return;
        }
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection != null) {
            screenRecorder = new ScreenRecorder(mWidth, mHeight, mediaProjection, mDpi);
        }
        thread = new Thread() {
            @Override
            public void run() {
                screenRecorder.startRecorder();//跟ScreenRecorder有关的下文再说，总之这句话的意思就是开始录屏的意思
            }
        };
        thread.start();
    //    binding.startPlayer.setText("停止");//开始和停止我用的同一个按钮，所以开始录屏之后把按钮文字改一下
        isrun = true;//录屏状态改成真
    }


    @Override
    public void onClick(View view) {
        screenRecorder.stop();
    }
}
