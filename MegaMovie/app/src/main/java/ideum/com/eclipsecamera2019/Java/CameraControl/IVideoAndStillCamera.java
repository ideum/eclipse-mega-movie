package ideum.com.eclipsecamera2019.Java.CameraControl;

import ideum.com.eclipsecamera2019.Java.LocationAndTiming.LocationProvider;
import ideum.com.eclipsecamera2019.Java.OrientationController.Clock;

public interface IVideoAndStillCamera {
    void takePhotoWithSettings(CaptureSequence.CaptureSettings settings);
    void startRecordingVideo(CaptureSequence.CaptureSettings settings);
    void stopRecordingVideo();
    void addCaptureListener(ICameraCaptureListener captureListener);
    void setDirectoryName(String directoryName);
    void setLocationProvider(LocationProvider locationProvider);
    void setTimeProvider(Clock timeProvider);
}


