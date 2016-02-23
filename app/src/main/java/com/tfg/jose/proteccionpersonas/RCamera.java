package com.tfg.jose.proteccionpersonas;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by jose on 2/02/16.
 *
 * Clase para la grabación de vídeo.
 */

public class RCamera implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();

    private boolean cameraState; // True si la camara esta grabando

    private Context mContext;
    private Activity mActivity;

    private Camera mCamera;

    // Constructor
    public RCamera(Context context, Activity activity){

        this.mContext = context;
        this.mActivity = activity;

        this.cameraState = false;

        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);

        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);

        surfaceView = (SurfaceView) mActivity.findViewById(R.id.surface_camera);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // Funcion para comenzar a grabar
    protected void startRecording() throws IOException {

        surfaceView.setVisibility(View.VISIBLE);

        cameraState = true;

        mrec = new MediaRecorder();
        mCamera.unlock();

        mrec.setCamera(mCamera);

        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile("/sdcard/video.3gp");

        mrec.setOrientationHint(90);

        mrec.prepare();
        mrec.start();

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mActivity, "Ha comenzado una grabación.", Toast.LENGTH_SHORT).show();
            }
        });    }

    // Funcion para parar de grabar
    protected void stopRecording() {
        cameraState = false;

        mrec.stop();
        mrec.release();

        Toast.makeText(mContext, "La grabación ha sido almacenada.", Toast.LENGTH_SHORT).show();
    }

    void stopCamera(){
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int hight) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){}

    // Devuelve la variable de la camara
    Camera getmCamera(){
        return mCamera;
    }

    // Devuelve el estado de la cámara
    boolean getCameraState(){
        return cameraState;
    }

    // Devuelve la variable de grabación de vídeo
    MediaRecorder getMrec(){
        return mrec;
    }

    // Devuelve el SurfaceHolder
    SurfaceHolder getSurfaceHolder(){
        return surfaceHolder;
    }

    // Inicializa la grabación de vídeo
    void setMrec(MediaRecorder m){
        mrec = m;
    }

    // Introduce el estado cd la cámaracd
    void setCameraState(boolean state){
        cameraState = state;
    }
}
