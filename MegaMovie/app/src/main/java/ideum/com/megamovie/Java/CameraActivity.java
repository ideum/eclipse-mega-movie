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
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.text.SimpleDateFormat;
import android.media.Image;
import android.media.ImageReader;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    private static boolean USES_TIMER = true;
    private static int TIMER_LENGTH = 5000;
    private static int TIMER_INTERVAL = 500;
    private static long SENSOR_EXPOSURE_TIME = 5*10000000;
    private static int SENSOR_SENSITIVITY = 500;


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
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureSessionCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                   captureStillImage();
                }
            };

    private CaptureRequest.Builder mCaptureRequestBuilder;
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
    private static File mImageFile;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(mImageReader.acquireNextImage()));
                }
            };
    private static class ImageSaver implements Runnable {

        private final Image mImage;

        private ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(mImageFile);
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
    }

    public void loadMapActivity(View view) {

        startActivity(new Intent(this,MapActivity.class));
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
        takePhoto();
    }

    public void takePhoto() {
        try {
            mImageFile = createImageFile();
        } catch(IOException e) {
            e.printStackTrace();
        }
        captureStillImage();
    }

    private void captureStillImage() {
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION,getOrientation(rotation));
            mCaptureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY,SENSOR_SENSITIVITY);
            mCaptureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, SENSOR_EXPOSURE_TIME);
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(),
                    mCaptureSessionCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraSession() {
        try {

            mCameraDevice.createCaptureSession(Arrays.asList(mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mCameraCaptureSession = session;
                            if (USES_TIMER) {
                                startTimer();
                            }
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
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mImageSize = Collections.min(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size lhs, Size rhs) {
                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
                                        rhs.getWidth() * rhs.getHeight());
                            }
                        }
                );
                mImageReader = ImageReader.newInstance(mImageSize.getWidth(),
                        mImageSize.getHeight(),
                        ImageFormat.JPEG,
                        1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mBackgroundHandler);
                mCameraID = cameraID;
                mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            }
        } catch(CameraAccessException e) {
            e.printStackTrace();
        }
    }

    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);
        }
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName,".jpg", storageDirectory);

        return image;
    }

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
}
