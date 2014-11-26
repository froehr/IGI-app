package de.ifgi.igiapp.igi_app.Gestures;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class GestureService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1000;
    private static final int SHAKE_THRESHOLD_NEG = -1000;

    public GestureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("GestureService bound", "");

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(getApplicationContext(), "GestureService started!", Toast.LENGTH_SHORT).show();
        Log.d("GestureService started", "");

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // TODO: Implement to perform one-time setup procedures

        Log.d("GestureService created", "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this, mAccelerometer);

        Toast.makeText(getApplicationContext(), "GestureService stopped!", Toast.LENGTH_SHORT).show();
        Log.d("GestureService destroyed", "");
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mSensor = event.sensor;

        if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];      // lateral left to right
            float y = event.values[1];      // vertical down to up
            float z = event.values[2];      // plane out to in

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 10) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
                float speedX = (x - last_x)/ diffTime * 10000;
                float speedY = (y - last_y)/ diffTime * 10000;
                float speedZ = (z - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {

                }

                if (speedX > SHAKE_THRESHOLD || speedX < SHAKE_THRESHOLD_NEG) {
                    Log.i("Speed of x = " + speedX, "");
                }

                if (speedY > SHAKE_THRESHOLD || speedY < SHAKE_THRESHOLD_NEG) {
                    Log.i("Speed of y = " + speedY, "");
                }

                if (speedZ > SHAKE_THRESHOLD || speedZ < SHAKE_THRESHOLD_NEG) {
                    Log.i("Speed of z = " + speedZ, "");
                }

                last_x = x;
                last_y = y;
                last_z = z;

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
