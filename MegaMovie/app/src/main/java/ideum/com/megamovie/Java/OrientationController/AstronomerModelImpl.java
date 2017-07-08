// Copyright 2008 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ideum.com.megamovie.Java.OrientationController;

import android.hardware.SensorManager;
import android.util.Log;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ideum.com.megamovie.Java.ApplicationConstants;
import ideum.com.megamovie.Java.Util.Geometry;
import ideum.com.megamovie.Java.Util.MiscUtil;
import ideum.com.megamovie.Java.Util.VectorUtil;
import ideum.com.megamovie.Java.provider.ephemeris.Planet;
import ideum.com.megamovie.Java.units.GeocentricCoordinates;
import ideum.com.megamovie.Java.units.LatLong;
import ideum.com.megamovie.Java.units.Matrix33;
import ideum.com.megamovie.Java.units.RaDec;
import ideum.com.megamovie.Java.units.Vector3;

import static ideum.com.megamovie.Java.Util.Geometry.addVectors;
import static ideum.com.megamovie.Java.Util.Geometry.calculateRADecOfZenith;
import static ideum.com.megamovie.Java.Util.Geometry.matrixMultiply;
import static ideum.com.megamovie.Java.Util.Geometry.matrixVectorMultiply;
import static ideum.com.megamovie.Java.Util.Geometry.scalarProduct;
import static ideum.com.megamovie.Java.Util.Geometry.scaleVector;
import static ideum.com.megamovie.Java.Util.Geometry.vectorProduct;

/**
 * The model of the astronomer.
 * <p>
 * <p>Stores all the data about where and when he is and where he's looking and
 * handles translations between three frames of reference:
 * <ol>
 * <li>Celestial - a frame fixed against the background stars with
 * x, y, z axes pointing to (RA = 90, DEC = 0), (RA = 0, DEC = 0), DEC = 90
 * <li>Phone - a frame fixed in the phone with x across the short side, y across
 * the long side, and z coming out of the phone screen.
 * <li>Local - a frame fixed in the astronomer's local position. x is due east
 * along the ground y is due north along the ground, and z points towards the
 * zenith.
 * </ol>
 * <p>
 * <p>We calculate the local frame in phone coords, and in celestial coords and
 * calculate a transform between the two.
 * In the following, N, E, U correspond to the local
 * North, East and Up vectors (ie N, E along the ground, Up to the Zenith)
 * <p>
 * <p>In Phone Space: axesPhone = [N, E, U]
 * <p>
 * <p>In Celestial Space: axesSpace = [N, E, U]
 * <p>
 * <p>We find T such that axesCelestial = T * axesPhone
 * <p>
 * <p>Then, [viewDir, viewUp]_celestial = T * [viewDir, viewUp]_phone
 * <p>
 * <p>where the latter vector is trivial to calculate.
 * <p>
 * <p>Implementation note: this class isn't making defensive copies and
 * so is vulnerable to clients changing its internal state.
 *
 * @author John Taylor
 */
public class AstronomerModelImpl implements AstronomerModel {

    public int CALIBRATION_METHOD = 1;


    private static final String TAG = MiscUtil.getTag(AstronomerModelImpl.class);
    private static final Vector3 POINTING_DIR_IN_PHONE_COORDS = new Vector3(0, 0, -1);
    private static final Vector3 SCREEN_UP_IN_PHONE_COORDS = new Vector3(0, 1, 0);
    private static final Vector3 SCREEN_RIGHT_IN_PHONE_COORDS = new Vector3(1, 0, 0);
    private Vector3 screenInPhoneCoords = SCREEN_UP_IN_PHONE_COORDS;
    private static final Vector3 AXIS_OF_EARTHS_ROTATION = new Vector3(0, 0, 1);
    private static final long MINIMUM_TIME_BETWEEN_CELESTIAL_COORD_UPDATES_MILLIS = 60000L;

    private MagneticDeclinationCalculator magneticDeclinationCalculator;
    private boolean autoUpdatePointing = true;
    private float fieldOfView = 45;  // Degrees
    private LatLong location = new LatLong(0f, 0f);
    private Clock clock = new RealClock();
    private long celestialCoordsLastUpdated = -1;


    private List<ModelChangeListener> listeners = new ArrayList<>();

    @Override
    public void addListener(ModelChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * The pointing comprises a vector into the phone's screen expressed in
     * celestial coordinates combined with a perpendicular vector along the
     * phone's longer side.
     */
    private Pointing pointing = new Pointing();
    private Pointing correctedPointing = new Pointing();

    /**
     * The sensor acceleration in the phone's coordinate system.
     */
    private Vector3 acceleration = ApplicationConstants.INITIAL_DOWN.copy();

    private Vector3 upPhone = scaleVector(acceleration, -1);

    /**
     * The sensor magnetic field in the phone's coordinate system.
     */
    private Vector3 magneticField = ApplicationConstants.INITIAL_SOUTH.copy();

    private boolean useRotationVector = false;

    private float[] rotationVector = new float[]{1, 0, 0, 0};

    /**
     * North along the ground in celestial coordinates.
     */
    private Vector3 trueNorthCelestial = new Vector3(1, 0, 0);

    /**
     * Up in celestial coordinates.
     */
    private Vector3 upCelestial = new Vector3(0, 1, 0);

    /**
     * East in celestial coordinates.
     */
    private Vector3 trueEastCelestial = AXIS_OF_EARTHS_ROTATION;

    /**
     * [North, Up, East]^-1 in phone coordinates.
     */
    private Matrix33 axesPhoneInverseMatrix = Matrix33.getIdMatrix();

    private Matrix33 correctionMatrix = Matrix33.getIdMatrix();

    private Vector3 storedNorthInPhoneCoordinates = new Vector3(0,1,0);
    private Matrix33 storedPhoneInLocalCoordinates = Matrix33.getIdMatrix();
    private Pointing correctedPointingNew = new Pointing();

//    private Vector3 localUpInPhoneCoordinates = new Vector3(0)

    /**
     * [North, Up, East] in celestial coordinates.
     */
    private Matrix33 axesMagneticCelestialMatrix = Matrix33.getIdMatrix();

    /**
     * @param magneticDeclinationCalculator A calculator that will provide the
     *                                      magnetic correction from True North to Magnetic North.
     */
    public AstronomerModelImpl(MagneticDeclinationCalculator magneticDeclinationCalculator) {
        setMagneticDeclinationCalculator(magneticDeclinationCalculator);
    }

    private boolean isCalibrated = false;

    private Vector3 getCalibratedNorthPhone() {
        Matrix33 axesPhone = axesPhoneInverseMatrix.clone().getInverse();
        Matrix33 transform = matrixMultiply(axesPhone,storedPhoneInLocalCoordinates);
        return matrixVectorMultiply(transform,storedNorthInPhoneCoordinates);
    }

    public void resetCalibration() {
        correctionMatrix = Matrix33.getIdMatrix();
        isCalibrated = false;
    }

    public void calibrate(GeocentricCoordinates targetCoordinates) {

        Matrix33 A = axesMagneticCelestialMatrix.getInverse();
        Vector3 lineOfSightInPhoneCoordinates = VectorUtil.negate(matrixVectorMultiply(A, targetCoordinates));

        float nz = lineOfSightInPhoneCoordinates.x;

        Vector3 northInPhoneCoordinates_0 = findPerpendicular(nz,upPhone,0);
        Vector3 northInPhoneCoordinates_1 = findPerpendicular(nz,upPhone,1);

        Vector3 east_0 = VectorUtil.crossProduct(northInPhoneCoordinates_0,upPhone);
        Vector3 east_1 = VectorUtil.crossProduct(northInPhoneCoordinates_1,upPhone);

        float eastZ = lineOfSightInPhoneCoordinates.z;

        float error_0 = Math.abs(east_0.z - eastZ);
        float error_1 = Math.abs(east_1.z - eastZ);

        Vector3 northInPhoneCoordinates = northInPhoneCoordinates_0;
        if (error_1 < error_0) {
            northInPhoneCoordinates = northInPhoneCoordinates_1;
        }

        storedNorthInPhoneCoordinates = northInPhoneCoordinates;
        storedPhoneInLocalCoordinates = axesPhoneInverseMatrix.clone();


        Vector3 eastInPhoneCoordinates = VectorUtil.crossProduct(northInPhoneCoordinates, upPhone);
        Vector3 perpInLocalCoordinates = matrixVectorMultiply(A, getPointing().getPerpendicular());
        perpInLocalCoordinates = VectorUtil.difference(perpInLocalCoordinates, VectorUtil.scale(lineOfSightInPhoneCoordinates, VectorUtil.dotProduct(perpInLocalCoordinates, lineOfSightInPhoneCoordinates)));
        perpInLocalCoordinates.normalize();
        Vector3 v1 = VectorUtil.crossProduct(perpInLocalCoordinates, lineOfSightInPhoneCoordinates);
        Matrix33 B = axesPhoneInverseMatrix.clone();
        Matrix33 B1 = new Matrix33(northInPhoneCoordinates, upPhone, eastInPhoneCoordinates, false);
        Matrix33 B2 = new Matrix33(v1, perpInLocalCoordinates, lineOfSightInPhoneCoordinates);
        correctionMatrix = matrixMultiply(B2, B.getInverse());
        isCalibrated = true;
    }

    private Matrix33 getCalibratedAxesPhoneInverse() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        calculateLocalNorthAndUpInPhoneCoordsFromSensors();

        Vector3 northPhone = getCalibratedNorthPhone();
        Vector3 eastPhone = VectorUtil.crossProduct(northPhone,upPhone);
        return new Matrix33(northPhone,upPhone,eastPhone,false);
    }



    private float quadraticFormula(float a, float b, float c, int root) {
        float d = (float) Math.sqrt(b * b - 4 * a * c);
        if (root == 0) {
            return (-b + d) / (2 * a);
        } else {
            return (-b - d) / (2 * a);
        }
    }

    private Vector3 findPerpendicular(float vz,Vector3 w,int root) {
        float vx = quadraticFormula(w.x*w.x + w.y*w.y,2*w.x*w.z*vz,vz*vz*w.z*w.z+vz*vz*w.y*w.y-w.y*w.y,root);
        float vy = (-vx*w.x - vz*w.z)/w.y;

        return new Vector3(vx,vy,vz);

    }

//  public void updateCorrectionMatrix() {
//    Matrix33 A = axesPhoneInverseMatrix.clone();
//    Matrix33 A1 = calibratePointingAtMoon();
//    correctionMatrix = matrixMultiply(A.getInverse(),A1);
//  }

    private void updateCorrectedPointingNew() {
        Matrix33 calibratedAxesPhoneInverse = getCalibratedAxesPhoneInverse();
        Matrix33 transform = matrixMultiply(axesMagneticCelestialMatrix,calibratedAxesPhoneInverse);
        Vector3 lineOfSight = matrixVectorMultiply(transform, POINTING_DIR_IN_PHONE_COORDS);
        Vector3 perpendicular = matrixVectorMultiply(transform, screenInPhoneCoords);

        correctedPointingNew.updateLineOfSight(lineOfSight);
        correctedPointingNew.updatePerpendicular(perpendicular);

    }

    private void updateCorrectedPointing() {

        calculateLocalNorthAndUpInCelestialCoords(false);
        calculateLocalNorthAndUpInPhoneCoordsFromSensors();

        Matrix33 transformCorrected = matrixMultiply(axesMagneticCelestialMatrix, matrixMultiply(correctionMatrix, axesPhoneInverseMatrix));

        Vector3 lineOfSight = matrixVectorMultiply(transformCorrected, POINTING_DIR_IN_PHONE_COORDS);
        Vector3 perpendicular = matrixVectorMultiply(transformCorrected, screenInPhoneCoords);
        correctedPointing.updateLineOfSight(lineOfSight);
        correctedPointing.updatePerpendicular(perpendicular);
    }

    public Pointing getCorrectedPointing() {

        if (isCalibrated) {
            if (CALIBRATION_METHOD == 1) {
                updateCorrectedPointing();
                return correctedPointing;
            } else {
                updateCorrectedPointingNew();

                return correctedPointingNew;
            }
        }
        return getPointing();
//        return correctedPointing;
    }

    @Override
    public void setHorizontalRotation(boolean value) {
        if (value) {
            screenInPhoneCoords = SCREEN_RIGHT_IN_PHONE_COORDS;
        } else {
            screenInPhoneCoords = SCREEN_UP_IN_PHONE_COORDS;
        }
    }

    @Override
    public void setAutoUpdatePointing(boolean autoUpdatePointing) {
        this.autoUpdatePointing = autoUpdatePointing;
    }

    @Override
    public float getFieldOfView() {
        return fieldOfView;
    }

    @Override
    public void setFieldOfView(float degrees) {
        fieldOfView = degrees;
    }

    @Override
    public float getMagneticCorrection() {
        return magneticDeclinationCalculator.getDeclination();
    }

    @Override
    public Date getTime() {
        return new Date(clock.getTimeInMillisSinceEpoch());
    }

    @Override
    public LatLong getLocation() {
        return location;
    }

    @Override
    public void setLocation(LatLong location) {
        this.location = location;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }

    @Override
    public Vector3 getPhoneUpDirection() {
        return upPhone;
    }

    private static final float TOL = 0.01f;

    @Override
    public void setPhoneSensorValues(Vector3 acceleration, Vector3 magneticField) {
        if (magneticField.length2() < TOL || acceleration.length2() < TOL) {
            Log.w(TAG, "Invalid sensor values - ignoring");
            Log.w(TAG, "Mag: " + magneticField);
            Log.w(TAG, "Accel: " + acceleration);
            return;
        }
        this.acceleration.assign(acceleration);
        this.magneticField.assign(magneticField);
        useRotationVector = false;
    }

    @Override
    public void setPhoneSensorValues(float[] rotationVector) {
        // TODO(jontayler): What checks do we need for this to be valid?
        // Note on some phones such as the Galaxy S4 this vector is the wrong size and needs to be
        // truncated to 4.
        System.arraycopy(rotationVector, 0, this.rotationVector, 0, Math.min(rotationVector.length, 4));
        useRotationVector = true;
    }

    @Override
    public GeocentricCoordinates getNorth() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(trueNorthCelestial);
    }

    @Override
    public GeocentricCoordinates getSouth() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(scaleVector(trueNorthCelestial,
                -1));
    }

    @Override
    public GeocentricCoordinates getZenith() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(upCelestial);
    }

    @Override
    public GeocentricCoordinates getNadir() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(scaleVector(upCelestial, -1));
    }

    @Override
    public GeocentricCoordinates getEast() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(trueEastCelestial);
    }

    @Override
    public GeocentricCoordinates getWest() {
        calculateLocalNorthAndUpInCelestialCoords(false);
        return GeocentricCoordinates.getInstanceFromVector3(scaleVector(trueEastCelestial,
                -1));
    }

    @Override
    public void setMagneticDeclinationCalculator(MagneticDeclinationCalculator calculator) {
        this.magneticDeclinationCalculator = calculator;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }


    /**
     * Updates the astronomer's 'pointing', that is, the direction the phone is
     * facing in celestial coordinates and also the 'up' vector along the
     * screen (also in celestial coordinates).
     * <p>
     * <p>This method requires that {@link #axesMagneticCelestialMatrix} and
     * {@link #axesPhoneInverseMatrix} are currently up to date.
     */
    private void calculatePointing() {
        if (!autoUpdatePointing) {
            return;
        }

        calculateLocalNorthAndUpInCelestialCoords(false);
        calculateLocalNorthAndUpInPhoneCoordsFromSensors();

        Matrix33 transform = matrixMultiply(axesMagneticCelestialMatrix, axesPhoneInverseMatrix);

        Vector3 viewInSpaceSpace = matrixVectorMultiply(transform, POINTING_DIR_IN_PHONE_COORDS);
        Vector3 screenUpInSpaceSpace = matrixVectorMultiply(transform, screenInPhoneCoords);

        pointing.updateLineOfSight(viewInSpaceSpace);
        pointing.updatePerpendicular(screenUpInSpaceSpace);

        for (ModelChangeListener listener : listeners) {
            listener.onModelChanged(this);
        }

    }

    /**
     * Calculates local North, East and Up vectors in terms of the celestial
     * coordinate frame.
     */
    private void calculateLocalNorthAndUpInCelestialCoords(boolean forceUpdate) {
        long currentTime = clock.getTimeInMillisSinceEpoch();
        if (!forceUpdate &&
                Math.abs(currentTime - celestialCoordsLastUpdated) <
                        MINIMUM_TIME_BETWEEN_CELESTIAL_COORD_UPDATES_MILLIS) {
            return;
        }
        celestialCoordsLastUpdated = currentTime;
        updateMagneticCorrection();
        RaDec up = calculateRADecOfZenith(getTime(), location);
        upCelestial = GeocentricCoordinates.getInstance(up);
        Vector3 z = AXIS_OF_EARTHS_ROTATION;
        float zDotu = scalarProduct(upCelestial, z);
        trueNorthCelestial = addVectors(z, scaleVector(upCelestial, -zDotu));
        trueNorthCelestial.normalize();
        trueEastCelestial = vectorProduct(trueNorthCelestial, upCelestial);

        // Apply magnetic correction.  Rather than correct the phone's axes for
        // the magnetic declination, it's more efficient to rotate the
        // celestial axes by the same amount in the opposite direction.
        Matrix33 rotationMatrix = Geometry.calculateRotationMatrix(
                magneticDeclinationCalculator.getDeclination(), upCelestial);

        Vector3 magneticNorthCelestial = matrixVectorMultiply(rotationMatrix,
                trueNorthCelestial);
        Vector3 magneticEastCelestial = vectorProduct(magneticNorthCelestial, upCelestial);

        axesMagneticCelestialMatrix = new Matrix33(magneticNorthCelestial,
                upCelestial,
                magneticEastCelestial);
    }

    // TODO(jontayler): with the switch to using the rotation vector sensor this is rather
    // convoluted and doing too much work.  It can be greatly simplified when we rewrite the
    // rendering module.

    /**
     * Calculates local North and Up vectors in terms of the phone's coordinate
     * frame from the magnetic field and accelerometer sensors.
     */
    private void calculateLocalNorthAndUpInPhoneCoordsFromSensors() {
        Vector3 magneticNorthPhone;
        Vector3 magneticEastPhone;
        if (useRotationVector) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
            // The up and north vectors are the 2nd and 3rd rows of this matrix.
            magneticNorthPhone = new Vector3(rotationMatrix[3], rotationMatrix[4], rotationMatrix[5]);
            upPhone = new Vector3(rotationMatrix[6], rotationMatrix[7], rotationMatrix[8]);
            magneticEastPhone = new Vector3(rotationMatrix[0], rotationMatrix[1], rotationMatrix[2]);
        } else {
            // TODO(johntaylor): we can reduce the number of vector copies done in here.
            Vector3 down = acceleration.copy();
            down.normalize();
            // Magnetic field goes *from* North to South, so reverse it.
            Vector3 magneticFieldToNorth = magneticField.copy();
            magneticFieldToNorth.scale(-1);
            magneticFieldToNorth.normalize();
            // This is the vector to magnetic North *along the ground*.
            magneticNorthPhone = addVectors(magneticFieldToNorth,
                    scaleVector(down, -scalarProduct(magneticFieldToNorth, down)));
            magneticNorthPhone.normalize();
            upPhone = scaleVector(down, -1);
            magneticEastPhone = vectorProduct(magneticNorthPhone, upPhone);
        }
        // The matrix is orthogonal, so transpose it to find its inverse.
        // Easiest way to do that is to construct it from row vectors instead
        // of column vectors.
        axesPhoneInverseMatrix = new Matrix33(magneticNorthPhone, upPhone, magneticEastPhone, false);
    }

    public String inverseMatrix() {
        return axesPhoneInverseMatrix.toString();
    }

    /**
     * Updates the angle between True North and Magnetic North.
     */
    private void updateMagneticCorrection() {
        magneticDeclinationCalculator.setLocationAndTime(location, getTimeMillis());
    }

    /**
     * Returns the user's pointing.  Note that clients should not usually modify this
     * object as it is not defensively copied.
     */
    @Override
    public Pointing getPointing() {
        calculatePointing();
        return pointing;
    }

    @Override
    public void setPointing(Vector3 lineOfSight, Vector3 perpendicular) {
        this.pointing.updateLineOfSight(lineOfSight);
        this.pointing.updatePerpendicular(perpendicular);
    }

    @Override
    public void setClock(Clock clock) {
        this.clock = clock;
        calculateLocalNorthAndUpInCelestialCoords(true);
    }

    @Override
    public long getTimeMillis() {
        return clock.getTimeInMillisSinceEpoch();
    }
}
