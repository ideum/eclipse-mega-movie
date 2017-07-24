package ideum.com.megamovie.Java.CameraControl;

import ideum.com.megamovie.Java.LocationAndTiming.LocationProvider;

/**
 * Created by MT_User on 7/20/2017.
 */

public interface ManualCamera {
    public void takePhotoWithSettings(CaptureSequence.CaptureSettings settings);
    public void setLocationProvider(LocationProvider provider);
    public void addCaptureListener(CameraFragment.CaptureListener listener);
    public void setDirectoryName(String directoryName);
}
