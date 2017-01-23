package com.tfg.jose.proteccionpersonas.main;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.android.volley.toolbox.Volley;
import com.tfg.jose.proteccionpersonas.R;
import com.tfg.jose.proteccionpersonas.gps.GPSTracker;
import com.tfg.jose.proteccionpersonas.webservices.Config;
import com.tfg.jose.proteccionpersonas.webservices.Request;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.io.IOException;
import java.net.NetworkInterface;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackgroundVideoRecorder extends Service implements RtspClient.Callback, Session.Callback,  SurfaceHolder.Callback {

    private WindowManager windowManager;
    private net.majorkernelpanic.streaming.gl.SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;

    private Session mSession;
    private static RtspClient mClient;

    private DBase protectULLDB;

    private NetworkInfo mWifi;

    private List<String> info_server;

    private String userName;
    private String userNumber;
    private String macAddress;
    private String latitude;
    private String longitude;

    @Override
    public void onCreate() {
        // Creamos una nueva surfaceview, se le pone tamaño de 1x1 en la parte superior izquierda
        // y se añade el callback para el servicio
        windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        // Recogemos los datos necesarios de la base de datos
        protectULLDB = new DBase(getApplicationContext());

        info_server = new ArrayList<String>();
        info_server = protectULLDB.recuperarINFO_SERVER("1");

        List<Contact> contacto;
        contacto = protectULLDB.recuperarINFO_USUARIO();

        if(!contacto.isEmpty()){
            userName = contacto.get(0).getName();
            userNumber = contacto.get(0).getNumber();
        } else {
            userName = getString(R.string.no_definido);
            userNumber = getString(R.string.no_definido);
        }

        surfaceView = new SurfaceView(this);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(surfaceView, layoutParams);
        surfaceView.getHolder().addCallback(this);

        // Obtenemos la posición GPS de la víctima
        GPSTracker gps;
        gps = new GPSTracker(this);

        if (gps.canGetLocation() && gps != null){
            latitude = String.valueOf(gps.getLatitude());
            longitude = String.valueOf(gps.getLongitude());
        } else {
            latitude = "null";
            longitude = "null";
        }

        // Obtenemos la MAC del dispositivo
        macAddress = getWifiMacAddress();
        Log.d("MAC Address = ", macAddress);

        // Comprobamos si está conectado el móvil a una wifi
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        // Si está conectado, enviamos el vídeo en streaming.
        if (mWifi.isConnected()) {
            // Inicializa el cliente RTSP
            initRtspClient();
            Config.requestQueue = Volley.newRequestQueue(this);
            try {
                // newUser(MAC, shortUrl, serverUrl)
                Request.newUser(macAddress,info_server.get(4).toString(),info_server.get(0).toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }

    // Devuelve la MAC del dispositivo
    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac==null){
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) { }

        return "";
    }

    @Override
    // Método llamado despues de crear la surface (inicializa y empieza la grabación)
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // Si el wifi está desconectado, guardamos el vídeo.
        if (!mWifi.isConnected()) {

            camera = Camera.open();
            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(params);

            mediaRecorder = new MediaRecorder();

            camera.unlock();

            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
            mediaRecorder.setCamera(camera);
            mediaRecorder.setOrientationHint(90);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
            mediaRecorder.setOutputFile(
                    Environment.getExternalStorageDirectory()+"/"+
                            DateFormat.format("yyyy-MM-dd_kk-mm-ss", new Date().getTime())+
                            ".mp4"
            );

            try { mediaRecorder.prepare(); }
            catch (Exception e) {}

            mediaRecorder.start();
        }
        else {
            // Sino, iniciamos el vídeo en streaming
            toggleStreaming();
        }
    }

    private void initRtspClient() {
        // Configures the SessionBuilder
        mSession = SessionBuilder.getInstance()
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(8000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setSurfaceView(surfaceView)
                .setCallback(this).build();

        // Configures the RTSP client
        mClient = new RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);

        String ip, port, path;

        // We parse the URI written in the Editext
        Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
        Matcher m = uri.matcher(info_server.get(1).toString() + macAddress);
        m.find();
        ip = m.group(1);
        port = m.group(2);
        path = m.group(3);

        mClient.setCredentials(info_server.get(2).toString(), info_server.get(3).toString());
        mClient.setServerAddress(ip, Integer.parseInt(port));
        mClient.setStreamPath("/" + path);
    }

    private void toggleStreaming() {

        if (!mClient.isStreaming()) {
            mSession.startPreview(); // Start camera preview
            mClient.startStream(); // Start video stream
        } else {
            // If already streaming, stop streaming
            mSession.stopPreview(); // stop camera preview
            mClient.stopStream(); // stop streaming
        }
    }

    @Override
    // Para de grabar y elimina la surface (Destructor)
    public void onDestroy() {

        // Si se está enviando un vídeo en streaming, lo paramos
        if (mWifi.isConnected()) {
            toggleStreaming();

            try {
                // streamOffline(MAC,server url)
                Request.streamOffline(macAddress,info_server.get(0).toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            mClient.release();
            mSession.release();
        }

        // En caso de que se esté guardando en el dispositivo, se termina de grabar
        else {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();

            camera.lock();
            camera.release();
        }

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
        try {
            // streamOnline(MAC, username, number of user, latitude, longitude, short url, server url)
            Request.streamOnline(macAddress,userName,userNumber,latitude,longitude,info_server.get(4).toString(),info_server.get(0).toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSessionStopped() {

    }
}