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
import android.text.method.MovementMethod;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Bus;

import de.ifgi.igiapp.igi_app.Bus.AnswerAvailableEvent;
import de.ifgi.igiapp.igi_app.Bus.BusProvider;
import de.ifgi.igiapp.igi_app.MapsActivity;

public class GestureService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private static final int SHAKE_THRESHOLD = 15;
    private static final int SHAKE_THRESHOLD_NEG = -15;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    private final MovingAverage movingAverageX = new MovingAverage(10);
    private final MovingAverage movingAverageY = new MovingAverage(10);
    private final MovingAverage movingAverageZ = new MovingAverage(10);

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
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(getApplicationContext(), "GestureService started!", Toast.LENGTH_SHORT).show();

        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

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

            //Log.i("Accelerometer = ", "" + x + " , " + y + " , " + z);

            movingAverageX.add(x);
            movingAverageY.add(y);
            movingAverageZ.add(z);

            Log.i("MovingAverage = ", "" + movingAverageX.getAverage() + " , " + movingAverageY.getAverage() + " , " + movingAverageZ.getAverage());

            //Pose Gestures
            if (movingAverageX.getAverage() > 5) {
                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_LEFT));
            }

            if (movingAverageX.getAverage() < -5) {
                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_RIGHT));
            }

            if (movingAverageY.getAverage() > 8) {
                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_DOWN));
            }

            if (movingAverageY.getAverage() < -1) {
                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_UP));
            }

/*            // Shaking
            if (x > SHAKE_THRESHOLD || x < SHAKE_THRESHOLD_NEG) {
                Log.i("Speed of x = " + x, "");

                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_LEFT));
            }

            if (y > SHAKE_THRESHOLD || y < SHAKE_THRESHOLD_NEG) {
                Log.i("Speed of y = " + y, "");

                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.PAN_UP));
            }

            if (z > SHAKE_THRESHOLD+5 || z < SHAKE_THRESHOLD_NEG+5) {
                Log.i("Speed of z = " + z, "");

                BusProvider.getInstance().post(new AnswerAvailableEvent(BusProvider.ZOOM_OUT));
            }*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
