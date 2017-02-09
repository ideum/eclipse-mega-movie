package ideum.com.megamovie.Java;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;


public class MyTimer {
    // Timer runs for one year
    private static long COUNTDOWN_DURATION = Long.MAX_VALUE;
    private static long TICK_INTERVAL = 10; //milliseconds
    public static final String TAG = "MyTimer";
    public interface MyTimerListener {
         void onTick();
    }
    private CountDownTimer mCountDownTimer;
    private MyTimerListener mListener;

    private ScheduledExecutorService mService;

    public MyTimer(MyTimerListener listener) {
        mListener = listener;
        mService = Executors.newScheduledThreadPool(1);
    }

    public void startTicking() {
//        mService = Executors.newScheduledThreadPool(1);
//
//        Runnable myRunnable = new Runnable() {
//            @Override
//                    public void run() {
//                mListener.onTick();
//            }
//        };
//
//        mService.scheduleAtFixedRate(myRunnable,0,500, TimeUnit.MILLISECONDS);

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mCountDownTimer = new CountDownTimer(COUNTDOWN_DURATION, TICK_INTERVAL) {

                public void onTick(long millisUntilFinished) {
                    mListener.onTick();
                }

                public void onFinish() {
                }
            }.start();
    }

    public void cancel() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
//        if (mService != null) {
//            mService.shutdown();
//        }
    }
}
