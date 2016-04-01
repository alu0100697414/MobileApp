package com.tfg.jose.proteccionpersonas;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackgroundVideoRecorder extends Service implements RtspClient.Callback, Session.Callback,  SurfaceHolder.Callback {

    private WindowManager windowManager;
    private net.majorkernelpanic.streaming.gl.SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;

    private Session mSession;
    private static RtspClient mClient;

    @Override
    // Creamos una nueva surfaceview, se le pone tamaño de 1x1 en la parte superior izquierda y se añade el callback para el servicio
    public void onCreate() {

        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        surfaceView = new SurfaceView(this);

        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

        // Initialize RTSP client
        initRtspClient();
    }

    @Override  // Método llamado despues de crear la surface (inicializa y empieza la grabación)
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

//        camera = Camera.open();
//
//        Camera.Parameters params = camera.getParameters();
//        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        camera.setParameters(params);
//
//        mediaRecorder = new MediaRecorder();
//
//        camera.unlock();
//
//        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//        mediaRecorder.setCamera(camera);
//        mediaRecorder.setOrientationHint(90);
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//
//        mediaRecorder.setOutputFile(
//                Environment.getExternalStorageDirectory()+"/"+
//                        DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime())+
//                        ".mp4"
//        );
//
//        try { mediaRecorder.prepare(); }
//        catch (Exception e) {}
//
//        mediaRecorder.start();

        toggleStreaming();
    }

    private void initRtspClient() {
        // Configures the SessionBuilder
        mSession = SessionBuilder.getInstance()
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_NONE)
                .setAudioQuality(new AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(surfaceView).setPreviewOrientation(0)
                .setCallback(this).build();

        // Configures the RTSP client
        mClient = new RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);
        surfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
        String ip, port, path;

        // We parse the URI written in the Editext
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
        Matcher m = uri.matcher(StreamingConfig.STREAM_URL);
        m.find();
        ip = m.group(1);
        port = m.group(2);
        path = m.group(3);

        mClient.setCredentials(StreamingConfig.PUBLISHER_USERNAME, StreamingConfig.PUBLISHER_PASSWORD);
        mClient.setServerAddress(ip, Integer.parseInt(port));
        mClient.setStreamPath("/" + path);
    }

    private void toggleStreaming() {

        Log.i("ESTOY:","Entre en el toggleeee");

        if (!mClient.isStreaming()) {
            // Start camera preview
            mSession.startPreview();

            // Start video stream
            mClient.startStream();
        } else {
            // already streaming, stop streaming
            // stop camera preview
            mSession.stopPreview();

            // stop streaming
            mClient.stopStream();
        }
    }

    @Override  // Para de grabar y elimina la surface (Destructor)
    public void onDestroy() {

        toggleStreaming();

//        mediaRecorder.stop();
//        mediaRecorder.reset();
//        mediaRecorder.release();
//
//        camera.lock();
//        camera.release();

        mClient.release();
        mSession.release();

        windowManager.removeView(surfaceView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onRtspUpdate(int message, Exception exception) {

    }

    @Override
    public void onBitrateUpdate(long bitrate) {

    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {

    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {

    }

    @Override
    public void onSessionStarted() {

    }

    @Override
    public void onSessionStopped() {

    }
}