package de.ifgi.igiapp.igi_app.Gestures;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import de.ifgi.igiapp.igi_app.Bus.AnswerAvailableEvent;
import de.ifgi.igiapp.igi_app.Bus.BusProvider;
import de.ifgi.igiapp.igi_app.MapsActivity;

public class GestureService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 1500;
    private static final int SHAKE_THRESHOLD_NEG = -1500;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GestureService getService() {
            // Return this instance of LocalService so clients can call public methods
            return GestureService.this;
        }
    }


    public GestureService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("GestureService bound", "");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(getApplicationContext(), "GestureService started!", Toast.LENGTH_SHORT).show();

        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
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

                    BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_LEFT));
                }

                if (speedY > SHAKE_THRESHOLD || speedY < SHAKE_THRESHOLD_NEG) {
                    Log.i("Speed of y = " + speedY, "");

                    BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_UP));
                }

                if (speedZ > SHAKE_THRESHOLD || speedZ < SHAKE_THRESHOLD_NEG) {
                    Log.i("Speed of z = " + speedZ, "");

                    BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.ZOOM_OUT));
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
