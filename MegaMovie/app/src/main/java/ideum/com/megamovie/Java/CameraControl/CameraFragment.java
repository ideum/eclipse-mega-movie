/**
 * Captures images in either RAW and/or JPEG format. For performance
 * reasons, it does not provide a live preview. It uses manual camera settings
 * for exposure time, sensor sensitivity, and focus distance, and saves those settings
 * together with gps to custom metadata file. Code is primarily based on the Google
 * sample project available here: https://github.com/googlesamples/android-Camera2Raw
 */

package ideum.com.megamovie.Java.CameraControl;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import ideum.com.megamovie.Java.LocationAndTiming.LocationProvider;
import ideum.com.megamovie.Java.PatagoniaTest.MetadataWriter;

public class CameraFragment extends android.app.Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {

    public interface CaptureListener {
        void onCapture();
    }

    private List<CaptureListener> listeners = new ArrayList<>();

    public void addCaptureListener(CaptureListener listener) {
        listeners.add(listener);
    }

    /**
     * Determines whether camera session sends image data to surfaces
     * which handle JPEG/RAW images, respectively
     */
    private static final boolean ALLOWS_JPEG = true;
    private static final boolean ALLOWS_RAW = false;

    private static final String RAW_METADATA_FILENAME = "metadata_raw.txt";
    private static final String JPEG_METADATA_FILENAME = "metadata_jpeg.txt";
    private static final String DATA_DIRECTORY_NAME = "MegaMovie";

    private static final String TAG = "Camera Activity";

    /**
     * Request code for camera permissions
     */
    private static final int REQUEST_CAMERA_PERMISSIONS = 0;

    /**
     * Permissions required to take a picture.
     */
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    /**
     * Used to get correct orientation for saving JPEG images
     */

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

    /**
     * Location provider is queried when writing image metadata
     */
    private LocationProvider mLocationProvider;

    public void setLocationProvider(LocationProvider locationProvider) {
        mLocationProvider = locationProvider;
    }

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCharacteristics;
    public final AtomicInteger mRequestCounter = new AtomicInteger();
    private CameraCaptureSession mCameraCaptureSession;
    private CameraCaptureSession.CaptureCallback mCaptureSessionCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);

                    int requestId = (int) request.getTag();

                    String currentDateTime = generateTimeStamp();

                    /**
                     * Set up jpegBuild for request
                     */
                    if (ALLOWS_JPEG) {
                        ImageSaver.ImageSaverBuilder jpegBuilder = mJpegResultQueue.get(requestId);

                        File jpegRootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DATA_DIRECTORY_NAME + "/JPEG");
                        if (!jpegRootPath.exists()) {
                            jpegRootPath.mkdirs();
                        }

                        File jpegFile = new File(jpegRootPath,
                                "JPEG_" + currentDateTime + ".jpg");

                        if (jpegBuilder != null) {
                            jpegBuilder.setFile(jpegFile);
                        }
                    }

                    /**
                     * Set up rawBuilder for request
                     */
                    if (ALLOWS_RAW) {
                        ImageSaver.ImageSaverBuilder rawBuilder = mRawResultQueue.get(requestId);

                        File rawRootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DATA_DIRECTORY_NAME + "/RAW");
                        if (!rawRootPath.exists()) {
                            rawRootPath.mkdirs();
                        }
                        File rawFile = new File(rawRootPath,
                                "RAW_" + currentDateTime + ".dng");

                        if (rawBuilder != null) {
                            rawBuilder.setFile(rawFile);
                        }
                    }
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    int requestId = (int) request.getTag();

                    ImageSaver.ImageSaverBuilder jpegBuilder = mJpegResultQueue.get(requestId);
                    if (jpegBuilder != null) {
                        jpegBuilder.setResult(result);
                        /**
                         * Write metadata to file
                         */
                        String fileName = jpegBuilder.getFileName();
                        MetadataWriter writer = new MetadataWriter(result, fileName);
                        File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DATA_DIRECTORY_NAME);
                        File metadataFile = new File(rootPath, JPEG_METADATA_FILENAME);
                        try {
                            FileOutputStream stream = new FileOutputStream(metadataFile, true);
                            byte[] bytes = writer.getXMLString().getBytes();
                            stream.write(bytes);
                            MediaScannerConnection.scanFile(getActivity(), new String[]{metadataFile.getPath()},
                                    null, new MediaScannerConnection.MediaScannerConnectionClient() {
                                        @Override
                                        public void onMediaScannerConnected() {
                                            // Do nothing
                                        }

                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            Log.i(TAG, "Scanned" + path + ":");
                                            Log.i(TAG, "-> uri=" + uri);
                                        }
                                    });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    ImageSaver.ImageSaverBuilder rawBuilder = mRawResultQueue.get(requestId);
                    if (rawBuilder != null) {
                        rawBuilder.setResult(result);

                        /**
                         * Write metadata to file
                         */
                        String fileName = rawBuilder.getFileName();
                        MetadataWriter writer = new MetadataWriter(result, fileName);
                        File rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), DATA_DIRECTORY_NAME);
                        File metadataFile = new File(rootPath, RAW_METADATA_FILENAME);
                        try {
                            FileOutputStream stream = new FileOutputStream(metadataFile, true);
                            byte[] bytes = writer.getXMLString().getBytes();
                            stream.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
    private Size mJpegImageSize;
    private Size mRawImageSize;
    private int mSensorOrientation;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mJpegResultQueue = new TreeMap<>();
    private final TreeMap<Integer, ImageSaver.ImageSaverBuilder> mRawResultQueue = new TreeMap<>();

    private RefCountedAutoCloseable<ImageReader> mJpegImageReader;
    private final ImageReader.OnImageAvailableListener mOnJpegImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    dequeueAndSaveImage(mJpegResultQueue, mJpegImageReader);

                }
            };
    private RefCountedAutoCloseable<ImageReader> mRawImageReader;
    private final ImageReader.OnImageAvailableListener mOnRawImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    dequeueAndSaveImage(mRawResultQueue, mRawImageReader);

                }
            };

    //TODO: if user denies request, keep asking them until they say yes.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showMissingPermissionError();
                    return;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMissingPermissionError() {
        Activity activity = getActivity();
        if (activity != null) {
            Toast.makeText(activity, "This app needs camera permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
        setUpCamera();
        openCamera();
    }

    @Override
    public void onPause() {
        closeCamera();
        closeBackgroundThread();
        super.onPause();
    }

    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings) {

        captureStillImage(settings.exposureTime,
                settings.sensitivity,
                settings.focusDistance,
                settings.shouldSaveRaw,
                settings.shouldSaveJpeg);
    }

    private void captureStillImage(long duration, int sensitivity, float focusDistance, boolean shouldSaveRaw, boolean shouldSaveJpeg) {
        try {
            final CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
            if (shouldSaveJpeg) {
                captureRequestBuilder.addTarget(mJpegImageReader.get().getSurface());
                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            }

            if (shouldSaveRaw) {
                captureRequestBuilder.addTarget(mRawImageReader.get().getSurface());
            }

            captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, sensitivity);
            captureRequestBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, duration);
            captureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistance);
            captureRequestBuilder.set(CaptureRequest.JPEG_GPS_LOCATION, mLocationProvider.getLocation());


            captureRequestBuilder.setTag(mRequestCounter.getAndIncrement());
            for (CaptureListener listener : listeners) {
                listener.onCapture();
            }

            CaptureRequest request = captureRequestBuilder.build();
            if (shouldSaveJpeg) {
                ImageSaver.ImageSaverBuilder jpegBuilder = new ImageSaver.ImageSaverBuilder(getActivity()).setCharacteristics(mCharacteristics);
                mJpegResultQueue.put((int) request.getTag(), jpegBuilder);

            }
            if (shouldSaveRaw) {
                ImageSaver.ImageSaverBuilder rawBuilder = new ImageSaver.ImageSaverBuilder(getActivity()).setCharacteristics(mCharacteristics);
                mRawResultQueue.put((int) request.getTag(), rawBuilder);
            }
            mCameraCaptureSession.capture(request,
                    mCaptureSessionCallback,
                    mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createCameraSession() {
        try {
            List<Surface> surfaces = new ArrayList<>();
            if (ALLOWS_JPEG) {
                surfaces.add(mJpegImageReader.get().getSurface());
            }
            if (ALLOWS_RAW) {
                surfaces.add(mRawImageReader.get().getSurface());
            }
            mCameraDevice.createCaptureSession(surfaces,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mCameraCaptureSession = session;

                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getActivity().getApplicationContext(), "create camera session failed", Toast.LENGTH_SHORT).show();
                        }

                    }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void requestCameraPermissions() {
        FragmentCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void openCamera() {

        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }
        try {
            try {
                cameraManager.openCamera(mCameraID, mCameraDeviceStateCallback, mBackgroundHandler);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mJpegImageReader != null) {
            mJpegImageReader.close();
            mJpegImageReader = null;
        }
        if (mRawImageReader != null) {
            mRawImageReader.close();
            mRawImageReader = null;
        }
    }

    private void setUpCamera() {
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraID : mCameraManager.getCameraIdList()) {
                CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(cameraID);
                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                mCharacteristics = cc;
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mJpegImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size lhs, Size rhs) {
                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
                                        rhs.getWidth() * rhs.getHeight());
                            }
                        }
                );
                mRawImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.RAW_SENSOR)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size lhs, Size rhs) {
                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
                                        rhs.getWidth() * rhs.getHeight());
                            }
                        }
                );

                if (ALLOWS_JPEG) {
                    if (mJpegImageReader == null || mJpegImageReader.getAndRetain() == null) {
                        mJpegImageReader = new RefCountedAutoCloseable<>(
                                ImageReader.newInstance(mJpegImageSize.getWidth(),
                                        mJpegImageSize.getHeight(),
                                        ImageFormat.JPEG,
                        /*max images */50));

                    }
                }
                if (ALLOWS_RAW) {
                    if (mRawImageReader == null || mRawImageReader.getAndRetain() == null) {
                        mRawImageReader = new RefCountedAutoCloseable<>(
                                ImageReader.newInstance(mRawImageSize.getWidth(),
                                        mRawImageSize.getHeight(),
                                        ImageFormat.RAW_SENSOR,
                        /*max images */50));
                    }
                }
                if (ALLOWS_JPEG) {
                    mJpegImageReader.get().setOnImageAvailableListener(mOnJpegImageAvailableListener, mBackgroundHandler);
                }
                if (ALLOWS_RAW) {
                    mRawImageReader.get().setOnImageAvailableListener(mOnRawImageAvailableListener, mBackgroundHandler);
                }
                mCameraID = cameraID;
                mSensorOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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


    private void dequeueAndSaveImage(TreeMap<Integer, ImageSaver.ImageSaverBuilder> pendingQueue,
                                     RefCountedAutoCloseable<ImageReader> reader) {
        Map.Entry<Integer, ImageSaver.ImageSaverBuilder> entry = pendingQueue.firstEntry();
        ImageSaver.ImageSaverBuilder builder = entry.getValue();

        // Increment reference count to prevent ImageReader from being closed while we
        // are saving its Images in a background thread (otherwise their resources may
        // be freed while we are writing to a file).
        if (reader == null || reader.getAndRetain() == null) {
            Log.e(TAG, "Paused the activity before we could save the image," +
                    " ImageReader already closed.");
            pendingQueue.remove(entry.getKey());
            return;
        }

        Image image;
        try {
            image = reader.get().acquireNextImage();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }
        builder.setRefCountedReader(reader).setImage(image);
        handleCompletion(entry.getKey(), builder, pendingQueue);
    }

    private void handleCompletion(int requestId, ImageSaver.ImageSaverBuilder builder,
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
        private final Context mContext;

        private final RefCountedAutoCloseable<ImageReader> mReader;

        private ImageSaver(Image image, File file, CaptureResult captureResult,
                           CameraCharacteristics characteristics, Context context, RefCountedAutoCloseable<ImageReader> reader) {
            mImage = image;
            mFile = file;
            mCaptureResult = captureResult;
            mCharacteristics = characteristics;
            mReader = reader;
            mContext = context;
        }

        @Override
        public void run() {

            boolean success = false;
            int format = mImage.getFormat();
            switch (format) {
                case ImageFormat.JPEG: {
                    ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    FileOutputStream fileOutputStream = null;

                    try {
                        fileOutputStream = new FileOutputStream(mFile);
                        fileOutputStream.write(bytes);
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close();
                        closeOutput(fileOutputStream);
                    }
                }
                break;
                case ImageFormat.RAW_SENSOR: {
                    DngCreator dngCreator = new DngCreator(mCharacteristics, mCaptureResult);
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(mFile);
                        dngCreator.writeImage(output, mImage);
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close();
                        closeOutput(output);
                    }
                }
                break;
                default: {
                    Log.e(TAG, "Cannot save image, unexpected image format:" + format);
                    break;
                }
            }
            mReader.close();

            if (success) {
                MediaScannerConnection.scanFile(mContext, new String[]{mFile.getPath()},
                        null, new MediaScannerConnection.MediaScannerConnectionClient() {
                            @Override
                            public void onMediaScannerConnected() {
                                // Do nothing
                            }

                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i(TAG, "Scanned" + path + ":");
                                Log.i(TAG, "-> uri=" + uri);
                            }
                        });
            }

        }

        public static class ImageSaverBuilder {
            private Image mImage;
            private File mFile;
            private CaptureResult mCaptureResult;
            private CameraCharacteristics mCharacteristics;
            private RefCountedAutoCloseable<ImageReader> mReader;
            private Context mContext;

            public ImageSaverBuilder(final Context context) {
                mContext = context;
            }

            public synchronized ImageSaverBuilder setRefCountedReader(RefCountedAutoCloseable<ImageReader> reader) {
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
                if (!isComplete()) {
                    return null;
                }
                return new ImageSaver(mImage, mFile, mCaptureResult, mCharacteristics, mContext, mReader);
            }

            public synchronized String getSaveLocation() {
                return (mFile == null) ? "Unknown" : mFile.toString();
            }

            public synchronized String getFileName() {
                return (mFile == null) ? "Unknown" : mFile.getName();
            }

            public boolean isComplete() {
                return mImage != null && mFile != null && mCaptureResult != null
                        && mCharacteristics != null;
            }

        }
    }

    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String generateTimeStamp() {

        Calendar calendar = Calendar.getInstance();
        if (mLocationProvider != null) {
            Location location = mLocationProvider.getLocation();
            if (location != null) {
                long mills = mLocationProvider.getLocation().getTime();
                calendar.setTimeInMillis(mills);

            } else {
                Log.e(TAG, "location was null");
            }
        } else {
            Log.e(TAG, "location provider is null!");
        }

        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(calendar.getTime());

    }

    /**
     * A wrapper for an {@link AutoCloseable} object that implements reference counting to allow
     * for resource management.
     */
    public static class RefCountedAutoCloseable<T extends AutoCloseable> implements AutoCloseable {
        private T mObject;
        private long mRefCount = 0;

        /**
         * Wrap the given object.
         *
         * @param object an object to wrap.
         */
        public RefCountedAutoCloseable(T object) {
            if (object == null) throw new NullPointerException();
            mObject = object;
        }

        /**
         * Increment the reference count and return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T getAndRetain() {
            if (mRefCount < 0) {
                return null;
            }
            mRefCount++;
            return mObject;
        }

        /**
         * Return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        public synchronized T get() {
            return mObject;
        }

        /**
         * Decrement the reference count and release the wrapped object if there are no other
         * users retaining this object.
         */
        @Override
        public synchronized void close() {
            if (mRefCount >= 0) {
                mRefCount--;
                if (mRefCount < 0) {
                    try {
                        mObject.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        mObject = null;
                    }
                }
            }
        }
    }

}
