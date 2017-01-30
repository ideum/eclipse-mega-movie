package ideum.com.megamovie.Java;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.*;


public class MyTimer {
    private static long COUNTDOWN_DURATION = 100000000;
    private static long TICK_INTERVAL = 10; //milliseconds
    public interface MyTimerListener {
         void onTick();
    }
    private CountDownTimer mCountDownTimer;
    private MyTimerListener mListener;

    public MyTimer(MyTimerListener listener) {
        mListener = listener;
    }

    public void startTicking() {
        mCountDownTimer = new CountDownTimer(COUNTDOWN_DURATION, TICK_INTERVAL) {

                public void onTick(long millisUntilFinished) {
                    mListener.onTick();
                    Log.e("MyTimer","Beep");
                }

                public void onFinish() {
                }
            }.start();
    }

    public void cancel() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }




}
