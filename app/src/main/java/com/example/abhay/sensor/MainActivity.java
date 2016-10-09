package com.example.abhay.sensor;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class
        MainActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor mGeomagnetic;
    TextView title,tvx,tvy,tvz;
    RelativeLayout layout;
    private String mag;
    private final String filepath = "/sdcard/YOLO.txt";
    private BufferedWriter mBufferedWriter;
    private float x;
    private float y;
    private float z;
    private PendingIntent pendingIntent;

    public static final int MSG_DONE = 1;
    public static final int MSG_ERROR = 2;
    public static final int MSG_STOP = 3;

    private boolean mrunning;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private Handler uiHandler = new Handler(){
        public void handleMessage(Message msg){
            String str = (String) msg.obj;
            switch (msg.what)
            {
                case MSG_DONE:

                    Snackbar.make(layout, str, Snackbar.LENGTH_LONG).show();
                    break;
                case MSG_ERROR:
                    Toast.makeText(getBaseContext(),str,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGeomagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this,mGeomagnetic, SensorManager.SENSOR_DELAY_NORMAL);
        layout = (RelativeLayout) findViewById(R.id.relative);
        title = (TextView)findViewById(R.id.name);
        tvx = (TextView)findViewById(R.id.xval);
        tvy = (TextView)findViewById(R.id.yval);
        tvz = (TextView)findViewById(R.id.zval);
        title.setText("MAGNETIC FLIELD");

        mHandlerThread = new HandlerThread("Working Thread");
        mHandlerThread.start();

        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(r);
    }

    private Runnable r = new Runnable(){
        @Override
        public void run ()
        {
            while(true)
            {
                if (mrunning)
                {
                    Message msg1 = new Message();
                    try
                    {
                        WriteFile(filepath,mag);
                        msg1.what = MSG_DONE;
                        msg1.obj = "Started to write to SD 'YOLO.txt'";
                    }
                    catch (Exception e)
                    {
                        msg1.what = MSG_ERROR;
                        msg1.obj = e.getMessage();
                    }
                    uiHandler.sendMessage(msg1);
                }
                else
                {
                    Message msg2 = new Message();
                    msg2.what = MSG_STOP;
                    uiHandler.sendMessage(msg2);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    public void onStartClick(View view)
    {
        start();
    }

    public void onStopClick(View view)
    {
        stop();
    }


    private synchronized void start()
    {

        Intent myIntent = new Intent(MainActivity.this, ServiceDemo.class);

        pendingIntent = PendingIntent.getService(MainActivity.this, 0, myIntent, 0);



        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);



        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        //calendar.add(Calendar.SECOND, 10);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 5000, pendingIntent);
        mrunning = true;
    }

    private synchronized void stop()
    {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);

       Intent service = new Intent(getApplicationContext(), ServiceDemo.class);
        getApplicationContext().stopService(service);
        mSensorManager.unregisterListener(this);
        mrunning = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        // TODO Auto-generated method stub

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
            mag= String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
            Date date = new java.util.Date();
            Timestamp currentTime = new Timestamp(date.getTime());
            mag=currentTime+ String.valueOf(x) + ", " + String.valueOf(y) + ", " + String.valueOf(z);
            tvx.setText("X = "+ String.valueOf(x));
            tvy.setText("Y = "+ String.valueOf(y));
            tvz.setText("Z = "+ String.valueOf(z));
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

//    @Override
//    protected void onPause()
//    {
//        // TODO Auto-generated method stub
//        mSensorManager.unregisterListener(this);
//        Toast.makeText(this, "Unregister", Toast.LENGTH_LONG).show();
//        super.onPause();
//    }
}