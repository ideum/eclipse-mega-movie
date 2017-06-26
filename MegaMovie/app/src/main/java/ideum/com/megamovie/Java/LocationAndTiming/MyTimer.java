package ideum.com.megamovie.Java.LocationAndTiming;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MyTimer {

    private class MyTimerListenerRunnable implements Runnable {
        private ArrayList<MyTimerListener> mListeners = new ArrayList<>();
        private Handler handler;

        public MyTimerListenerRunnable() {
            handler = new Handler(Looper.getMainLooper());
        }

        public void addListener(MyTimerListener listener) {
            mListeners.add(listener);
        }

        @Override
        public void run() {
            handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (MyTimerListener l : mListeners)
                        l.onTick();
                    }
                });
        }
    }

    // Timer runs for one year
    private static long COUNTDOWN_DURATION = Long.MAX_VALUE;
    public  long TICK_INTERVAL = 100; //milliseconds
    public static final String TAG = "MyTimer";

    public interface MyTimerListener {
        void onTick();
    }

    private CountDownTimer mCountDownTimer;
    final private List<MyTimerListener> mListeners = new ArrayList<>();
    private MyTimerListenerRunnable mMyTimerListenerRunnable = new MyTimerListenerRunnable();

    private ScheduledExecutorService mService;

    public MyTimer() {
    }

    public void addListener(MyTimerListener listener) {
        mMyTimerListenerRunnable.addListener(listener);
        mListeners.add(listener);
    }


    public void startTicking() {
        mService = Executors.newScheduledThreadPool(1);

//        Runnable myRunnable = new Runnable() {
//            @Override
//            public void run() {
//                Log.i("TAG", "Tick");
//                for (MyTimerListener l : mListeners)
//                    l.onTick();
//            }
//        };

        mService.scheduleAtFixedRate(mMyTimerListenerRunnable, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS);

//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//        mCountDownTimer = new CountDownTimer(COUNTDOWN_DURATION, TICK_INTERVAL) {
//
//                public void onTick(long millisUntilFinished) {
//                    for(MyTimerListener l : mListeners)
//                    l.onTick();
//                }
//
//                public void onFinish() {
//                }
//            }.start();
    }

    public void cancel() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        if (mService != null) {
            mService.shutdown();
        }
    }
}
