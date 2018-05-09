package com.txby.zxing.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.txby.zxing.R;
import com.txby.zxing.camera.CameraManager;
import com.txby.zxing.decoding.DecodeThread;
import com.txby.zxing.view.ViewfinderResultPointCallback;
import com.txby.zxing.view.ViewfinderView;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 */
public final class CaptureHandler extends Handler {

    private static final String TAG = CaptureHandler.class.getSimpleName();

    private final CaptureFragment fragment;
    private final DecodeThread decodeThread;

    private State state;

    private enum State {
        /**
         * state
         */
        PREVIEW, SUCCESS, DONE
    }

    public CaptureHandler(CaptureFragment fragment, Vector<BarcodeFormat> decodeFormats,
                          String characterSet, ViewfinderView viewfinderView) {
        this.fragment = fragment;
        decodeThread = new DecodeThread(fragment, decodeFormats, characterSet, new ViewfinderResultPointCallback(viewfinderView));
        decodeThread.start();
        state = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            // When one auto focus pass finishes, start another. This is the closest thing to continuous AF.
            // It does seem to hunt a bit, but I'm not sure what else to do.
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }

        } else if (message.what == R.id.restart_preview) {
            Log.d(TAG, "Got restart preview message");
            restartPreviewAndDecode();

        } else if (message.what == R.id.decode_succeeded) {
            Log.d(TAG, "Got decode succeeded message");
            state = State.SUCCESS;
            Bundle bundle = message.getData();
            Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
            fragment.handleDecode((Result) message.obj, barcode);

        } else if (message.what == R.id.decode_failed) {
            // We're decoding as fast as possible, so when one decode fails, start another.
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);

        } else if (message.what == R.id.return_scan_result) {
            Log.d(TAG, "Got return scan result message");
            fragment.getActivity().setResult(Activity.RESULT_OK, (Intent) message.obj);
            fragment.getActivity().finish();

        } else if (message.what == R.id.launch_product_query) {
            Log.d(TAG, "Got product query message");
            String url = (String) message.obj;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            fragment.getActivity().startActivity(intent);
        }
    }

    /**
     * 重启预览
     */
    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            fragment.drawViewfinder();
        }
    }

    /**
     * 停止退出
     */
    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            // continue
        }
        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }
}
