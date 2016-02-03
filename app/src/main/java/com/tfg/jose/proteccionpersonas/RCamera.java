package com.tfg.jose.proteccionpersonas;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.text.Layout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by jose on 2/02/16.
 */
public class RCamera implements SurfaceHolder.Callback {

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();

    private boolean cameraState;

    private Inicio inicio;

    private Camera mCamera;

    // Constructor
    public RCamera(Inicio ini){

        this.inicio = ini;

        this.cameraState = false;

        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);

        int width_cam = mCamera.getParameters().getPreviewSize().width;
        int height_cam = mCamera.getParameters().getPreviewSize().height;
        final float scale = inicio.getResources().getDisplayMetrics().density; // Para las dimensiones en dp.

        surfaceView = (SurfaceView) inicio.findViewById(R.id.surface_camera);
        surfaceView.getLayoutParams().width = (int) (height_cam / 12 * scale);
        surfaceView.getLayoutParams().height = (int) (width_cam / 12 * scale);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    // Recoloca los elementos al iniciar la grabación
    void pushElements(){

        // SurfaceView
        SurfaceView surface = (SurfaceView) inicio.findViewById(R.id.surface_camera);

        RelativeLayout.LayoutParams lp_1 = (RelativeLayout.LayoutParams) surface.getLayoutParams();
        lp_1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        surface.setLayoutParams(lp_1);
        surface.setVisibility(View.VISIBLE);

        // Panic Button
        Button b = (Button) inicio.findViewById(R.id.panicButton);

        RelativeLayout.LayoutParams lp_2 = (RelativeLayout.LayoutParams) b.getLayoutParams();
        lp_2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        b.setLayoutParams(lp_2);
    }

    // Funcion para comenzar a grabar
    protected void startRecording() throws IOException {

        pushElements();

        cameraState = true;

        mrec = new MediaRecorder();
        mCamera.unlock();

        mrec.setCamera(mCamera);

        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile("/sdcard/video.3gp");

        mrec.setOrientationHint(90);

        mrec.prepare();
        mrec.start();
    }

    // Funcion para parar de grabar
    protected void stopRecording() {
        cameraState = false;

        mrec.stop();
        mrec.release();
        mrec = null;

        Toast.makeText(inicio.getApplicationContext(), "El vídeo ha sido almacenado.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mCamera != null){
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
        }
        else {
            Toast.makeText(inicio.getApplicationContext(), "Cámera no disponible.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
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

    // Introduce el estado de la cámaracd
    void setCameraState(boolean state){
        cameraState = state;
    }
}
