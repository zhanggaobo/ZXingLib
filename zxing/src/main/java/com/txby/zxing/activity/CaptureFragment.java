package com.txby.zxing.activity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.txby.zxing.R;
import com.txby.zxing.camera.CameraManager;
import com.txby.zxing.decoding.InactivityTimer;
import com.txby.zxing.utils.CodeUtils;
import com.txby.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * 自定义实现的扫描Fragment
 */
public class CaptureFragment extends Fragment implements SurfaceHolder.Callback, EasyPermissions.PermissionCallbacks {

    private static final float BEEP_VOLUME = 0.10f;
    private static final long VIBRATE_DURATION = 200L;

    private ViewfinderView viewfinderView;
    private SurfaceHolder surfaceHolder;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;

    private Camera camera;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private boolean vibrate;
    private CaptureHandler handler;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;

    private AnalyzeCallback analyzeCallback;
    private CameraInitCallBack cameraInitCallBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getActivity().getApplicationContext());

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this.getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = null;
        Bundle bundle = getArguments();

        if (bundle != null) {
            int layoutId = bundle.getInt(CodeUtils.LAYOUT_ID);
            if (layoutId != -1) {
                view = inflater.inflate(layoutId, null);
            }
        }
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_capture, null);
        }
        viewfinderView = (ViewfinderView) view.findViewById(R.id.viewfinder_view);
        SurfaceView surfaceView = (SurfaceView) view.findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 相机
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        // 提示音
        playBeep = true;
        AudioManager audioService = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (audioService != null && audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();

        // 震动
        vibrate = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        inactivityTimer.shutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (camera != null && CameraManager.get().isPreviewing()) {
            camera.stopPreview();
            CameraManager.get().getPreviewCallback().setHandler(null, 0);
            CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
            CameraManager.get().setPreviewing(false);
        }
    }


    //////

    /**
     * 初始化相机
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            if (cameraInitCallBack != null) {
                cameraInitCallBack.callBack(e);
            }
            return;
        }
        if (cameraInitCallBack != null) {
            cameraInitCallBack.callBack(null);
        }
        if (handler == null) {
            handler = new CaptureHandler(this, decodeFormats, characterSet, viewfinderView);
        }
    }

    /**
     * 初始化提示音
     */
    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //When the beep has finished playing, rewind to queue up another one.
                    mediaPlayer.seekTo(0);
                }
            });

            AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }


    /**
     * Handler scan result
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeFailed();
            }
        } else {
            if (analyzeCallback != null) {
                analyzeCallback.onAnalyzeSuccess(barcode, result.getText());
            }
        }
    }

    /**
     * 播放声音
     */
    private void playBeepSoundAndVibrate() {
        if (playBeep) {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setAnalyzeCallback(AnalyzeCallback analyzeCallback) {
        this.analyzeCallback = analyzeCallback;
    }

    public void setCameraInitCallBack(CameraInitCallBack callBack) {
        this.cameraInitCallBack = callBack;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }
}
