package com.example.abhay.sensor;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by Abhay on 1/21/2016.
 */
public class ServiceDemo extends Service implements SensorEventListener{
    private PendingIntent pendingIntent;
    private SensorManager mSensorManager;
    private Sensor mGeomagnetic;
    private float x;
    private float y;
    private float z;
    private String mag;
    private BufferedWriter mBufferedWriter;
    private final String filepath = "/sdcard/YOLO.txt";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Toast.makeText(this, "MyAlarmService.onCreate()", Toast.LENGTH_LONG).show();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this,mGeomagnetic, SensorManager.SENSOR_DELAY_NORMAL);


        Intent myIntent = new Intent(this, ServiceDemo.class);

        pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);


        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);



        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        //calendar.add(Calendar.SECOND, 10);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5000, pendingIntent);

        /*try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //stopSelf();
        return START_NOT_STICKY;
    }

    //call this function in service
    @SuppressLint("NewApi") @Override
    public void onTaskRemoved(Intent rootIntent) {

        super.onTaskRemoved(rootIntent);

        Log.e("On task removed", "Yes");

        Intent restartServiceIntent = new Intent(getApplicationContext(), ServiceDemo.class);
        restartServiceIntent.setPackage(getApplicationContext().getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1,
                restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

/*        Intent myIntent = new Intent(this, ServiceDemo.class);

        pendingIntent = PendingIntent.getService(this, 0, myIntent, 0);


        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);



        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        //calendar.add(Calendar.SECOND, 10);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5000, pendingIntent);*/
        // Toast.makeText(this, "Start ", Toast.LENGTH_LONG).show();


    }

    @Override
    public void onDestroy() {
       // mSensorManager.unregisterListener(this);
        Log.e("On destroy", "Yes");
        super.onDestroy();
        stopSelf();
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

        Intent service = new Intent(getApplicationContext(), ServiceDemo.class);
        getApplicationContext().stopService(service);
        mSensorManager.unregisterListener(this);
      //  mrunning = false;

        //Toast.makeText(this, "MyAlarmService.onDestroy()", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            mag= String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
            WriteFile(filepath,mag);

//            tvx.setText("X = "+ String.valueOf(x));
//            tvy.setText("Y = "+ String.valueOf(y));
//            tvz.setText("Z = "+ String.valueOf(z));
        }
    }
    public void CreateFile(String path)
    {
        File f = new File(path);
        try {
            Log.d("ACTIVITY", "Create a File.");
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void WriteFile(String filepath, String str)
    {
        mBufferedWriter = null;

        if (!FileIsExist(filepath))
            CreateFile(filepath);

        try
        {
            mBufferedWriter = new BufferedWriter(new FileWriter(filepath, true));
            mBufferedWriter.write(str);
            mBufferedWriter.newLine();
            mBufferedWriter.flush();
            mBufferedWriter.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean FileIsExist(String filepath)
    {
        File f = new File(filepath);

        if (! f.exists())
        {
            Log.e("ACTIVITY", "File does not exist.");
            return false;
        }
        else
            return true;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}