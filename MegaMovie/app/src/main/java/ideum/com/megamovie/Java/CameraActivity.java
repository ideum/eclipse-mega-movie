package ideum.com.megamovie.Java;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CameraActivity extends AppCompatActivity {
    private static int TIMER_LENGTH = 1000;
    private static int TIMER_INTERVAL = 100;
    private static long SENSOR_EXPOSURE_TIME = 5*1000000;
    private static int SENSOR_SENSITIVITY = 1000;


    private int MY_PERMISSIONS_CAMERA;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCharacteristics;
    private final AtomicInteger mRequestCounter = new AtomicInteger();
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureSessionCallback =

            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    String currentDataTime = generateTimeStamp();
                    File jpegFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "JPEG_" + currentDataTime + ".jpg");
                    ImageSaver.ImageSaverBuilder jpegBuilder;
                    int requestId = (int) request.getTag();
                    jpegBuilder = mJpegResultQueue.get(requestId);
                    if (jpegBuilder != null) {
                        jpegBuilder.setFile (jpegFile);
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    int requestId = (int) request.getTag();
                    ImageSaver.ImageSaverBuilder jpegBuilder = mJpegResultQueue.get(requestId);
                    if (jpegBuilder != null) {
                        jpegBuilder.setResult(result);
                    }
                }
            };

    private CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    createCameraSession();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                }
            };
    private String mCameraID;
    private Size mImageSize;
    private int mSensorOrientation;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mJpegResultQueue = new TreeMap<>();

    private ImageReader mJpegReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                        dequeueAndSaveImage(mJpegResultQueue,mJpegReader);

                }
            };

    public void loadCalibrationActivity(View view) {

        startActivity(new Intent(this,CalibrationActivity.class));
    }
    public void loadResultsActivity(View view) {
        startActivity(new Intent(this,ResultsActivity.class));
    }

    private void startTimer() {
        new CountDownTimer(TIMER_LENGTH, TIMER_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                takePhoto();
            }

            public void onFinish() {
                Toast.makeText(getApplicationContext(),"done!",Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ideum.com.megamovie.R.layout.activity_camera);

        setUpCamera();
        openCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
    }

    @Override
    public void onPause() {
        closeBackgroundThread();
        closeCamera();
        super.onPause();
    }

    public void takePhotoButtonPressed(View view) {
        startTimer();
    }

    public void takePhoto() {

        captureStillImage();
    }

    private void captureStillImage() {
        try {
            final CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
            captureRequestBuilder.addTarget(mJpegReader.getSurface());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,getOrientation(rotation));
            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,SENSOR_SENSITIVITY);
            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, SENSOR_EXPOSURE_TIME);
            captureRequestBuilder.setTag(mRequestCounter.getAndIncrement());

            CaptureRequest request = captureRequestBuilder.build();

            ImageSaver.ImageSaverBuilder jpegBuilder = new ImageSaver.ImageSaverBuilder().setCharacteristics(mCharacteristics);

            mJpegResultQueue.put((int) request.getTag(),jpegBuilder);

            mCameraCaptureSession.capture(request,
                    mCaptureSessionCallback,
                    mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraSession() {
        try {

            mCameraDevice.createCaptureSession(Arrays.asList(mJpegReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mCameraCaptureSession = session;

                        }
                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(),"create camera session failed",Toast.LENGTH_SHORT).show();
                        }

                    },null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA},
                    MY_PERMISSIONS_CAMERA);
        }
        try {
            cameraManager.openCamera(mCameraID,mCameraDeviceStateCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {

    }

    private void setUpCamera() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : mCameraManager.getCameraIdList()) {
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                mCharacteristics = cc;
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size lhs, Size rhs) {
                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
                                        rhs.getWidth() * rhs.getHeight());
                            }
                        }
                );
                mJpegReader = ImageReader.newInstance(mImageSize.getWidth(),
                        mImageSize.getHeight(),
                        ImageFormat.JPEG,
                        /*max images */5);
                mJpegReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);
                mCameraID = cameraID;
                mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

//    File createImageFile() throws IOException {
//        Calendar calendar = Calendar.getInstance();
//        String timeStamp = String.valueOf(calendar.getTimeInMillis());
//        String imageFileName = "IMAGE_" + timeStamp + "_";
//        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
//                    0);
//        }
//        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
//
//        File image = File.createTempFile(imageFileName,".jpg", storageDirectory);
//
//        return image;
//    }

    private void openBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2 background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBackgroundThread = null;
        mBackgroundHandler = null;
    }


    private void dequeueAndSaveImage(TreeMap<Integer, ImageSaver.ImageSaverBuilder> pendingQueue,
                                     ImageReader reader) {
        Map.Entry<Integer, ImageSaver.ImageSaverBuilder> entry = pendingQueue.firstEntry();
        ImageSaver.ImageSaverBuilder builder = entry.getValue();

        Image image;
        try {
            image = reader.acquireNextImage();
        } catch(IllegalStateException e) {
            e.printStackTrace();
            return;
        }
        builder.setReader(reader).setImage(image);
        handleCompletionLocked(entry.getKey(),builder,pendingQueue);
    }

    private void handleCompletionLocked(int requestId, ImageSaver.ImageSaverBuilder builder,
                                        TreeMap<Integer, ImageSaver.ImageSaverBuilder> queue) {
        if (builder == null) return;
        ImageSaver saver = builder.buildIfComplete();
        if (saver != null) {
            queue.remove(requestId);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(saver);
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;
        private final File mFile;
        private final CaptureResult mCaptureResult;
        private final CameraCharacteristics mCharacteristics;
        private final ImageReader mReader;

        private ImageSaver(Image image, File file, CaptureResult captureResult,
                           CameraCharacteristics characteristics, ImageReader imageReader) {
            mImage = image;
            mFile = file;
            mCaptureResult = captureResult;
            mCharacteristics = characteristics;
            mReader = imageReader;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(mFile);
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if(fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        public static class ImageSaverBuilder {
            private Image mImage;
            private File mFile;
            private CaptureResult mCaptureResult;
            private CameraCharacteristics mCharacteristics;
            private ImageReader mReader;

            public synchronized ImageSaverBuilder setReader(ImageReader reader) {
                if (reader == null) throw new NullPointerException();
                mReader = reader;
                return this;
            }

            public synchronized ImageSaverBuilder setImage(final Image image) {
                if (image == null) throw new NullPointerException();
                mImage = image;
                return this;
            }

            public synchronized ImageSaverBuilder setFile(final File file) {
                if (file == null) throw new NullPointerException();
                mFile = file;
                return this;
            }

            public synchronized ImageSaverBuilder setResult(final CaptureResult result) {
                if (result == null) throw new NullPointerException();
                mCaptureResult = result;
                return this;
            }

            public synchronized ImageSaverBuilder setCharacteristics(final CameraCharacteristics characteristics) {
                if (characteristics == null) throw new NullPointerException();
                mCharacteristics = characteristics;
                return this;
            }

            public synchronized ImageSaver buildIfComplete() {
                if(!isComplete()) {
                    return null;
                }
                return new ImageSaver(mImage, mFile, mCaptureResult, mCharacteristics, mReader);
            }

            public synchronized String getSaveLocation() {
                return (mFile == null) ? "Unknown" : mFile.toString();
            }

            public boolean isComplete() {
                return mImage != null && mFile != null && mCaptureResult != null
                        && mCharacteristics != null;
            }

        }

    }

private static String generateTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss_SSS", Locale.US);
    return sdf.format(new Date());
}


}
