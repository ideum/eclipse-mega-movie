package ideum.com.megamovie.Java.Application;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class UploadActivity extends AppCompatActivity {

    public static final String TAG = "UploadTestActivity";

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    Button uploadButton;
    Button cancelButton;

    CheckBox licenseAgreementCheckBox;
    CheckBox privacyAgreementCheckBox;

    TextView uploadProgressTextView;
    TextView fileSummaryTextView;
    TextView uploadErrorsTextView;

    int totalFiles = 0;

    String directoryName;
    private String sessionId;



    private List<TransferObserver> observers;
    private TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);


        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                getString(R.string.sw3_identity_pool_id_bc),
                Regions.US_EAST_1);

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        transferUtility = new TransferUtility(s3, getApplicationContext());

        observers = new ArrayList<>();

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.image_upload));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        directoryName = getDirectoryNameFromPreferences();

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableUploadButton();
                startUpload();
            }
        });

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelUpload();
            }
        });
        cancelButton.setVisibility(View.INVISIBLE);

        licenseAgreementCheckBox = (CheckBox) findViewById(R.id.license_agreement_check_box);
        licenseAgreementCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserAcknowledgement();
            }
        });

        privacyAgreementCheckBox = (CheckBox) findViewById(R.id.privacy_agreement_check_box);
        privacyAgreementCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserAcknowledgement();
            }
        });


        fileSummaryTextView = (TextView) findViewById(R.id.file_summary_text_view);
        uploadProgressTextView = (TextView) findViewById(R.id.upload_progress_text_view);
        uploadErrorsTextView = (TextView) findViewById(R.id.upload_errors_text_view);


        totalFiles = checkNumberOfFilesInDirectory();

        initiateUI();

        checkUserAcknowledgement();

        sessionId = getSessionId();
        Log.i("session_id",sessionId);

    }

    private void checkUserAcknowledgement() {
        boolean acknowledged = licenseAgreementCheckBox.isChecked() && privacyAgreementCheckBox.isChecked();
        if (acknowledged) {
            enableUploadButton();
        } else {
            disableUploadButton();
        }
    }

    private void enableUploadButton() {
        uploadButton.setEnabled(true);
        uploadButton.setBackgroundColor(Color.parseColor("#4285f4"));
    }

    private void disableUploadButton() {
        uploadButton.setEnabled(false);
        uploadButton.setBackgroundColor(Color.parseColor("#dddddd"));
    }

    private void showNoImagesAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.no_image_upload_warning))
                .setPositiveButton(getString(R.string.got_it), null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void initiateUI() {

        //String licenseText = "I verify that I own these photos and am licensing them under the <a href=\"https://creativecommons.org/publicdomain/zero/1.0/ \">CC0 license.</a>";
        String licenseText = getString(R.string.license) + " " + "<a href=\"https://creativecommons.org/publicdomain/zero/1.0/ \">CC0 license.</a>";
        licenseAgreementCheckBox.setText(Html.fromHtml(licenseText));
        licenseAgreementCheckBox.setMovementMethod(LinkMovementMethod.getInstance());

        fileSummaryTextView.setText(String.format(getString(R.string.you_have_d_photos_to_upload), totalFiles));
//        if (totalFiles == 0) {
//            fileSummaryTextView.setText("You do not currently have any eclipse iamges to upload. You can return after photographing the eclipse.");
//            //disableUploadButton();
//            uploadProgressTextView.setVisibility(View.GONE);
//            uploadErrorsTextView.setVisibility(View.GONE);
//        } else {
//            fileSummaryTextView.setText(String.format("You currently have %d files ready to upload to the archive", totalFiles));
//        }

        //uploadProgressTextView.setText("You do not currently have any uploads in progress");
        updateUI();
    }


    private void updateUI() {

        uploadProgressTextView.setText(String.format(getString(R.string.files_uploaded), numCompleted(), totalFiles));

        if (numErrors() > 0) {
            uploadErrorsTextView.setText(String.format(getString(R.string.upload_errors), numErrors()));
        } else {
            uploadErrorsTextView.setText("");
        }
    }

    private void onNetworkWaiting() {
        cancelUpload();
        clearListeners();
        Toast.makeText(getApplicationContext(), getString(R.string.no_network_connection_found), Toast.LENGTH_SHORT).show();
       Log.i("upload","Canceling");
    }

    private void checkProgressStatus() {
        if (numWaitingForNetwork() != 0) {
            onNetworkWaiting();
            return;
        }
        Log.i("upload","in progress: " + String.valueOf(numInProgress()));
        if (numInProgress() == 0) {
            onUploadComplete();
        } else {
            onUploadInProgress();
        }
        updateUI();
    }

    private void cancelUpload() {
        transferUtility.cancelAllWithType(TransferType.UPLOAD);
        enableUploadButton();
        cancelButton.setVisibility(View.INVISIBLE);
    }

    private void onUploadInProgress() {
      //  disableUploadButton();
    }

    private void onUploadComplete() {
        enableUploadButton();
        cancelButton.setVisibility(View.INVISIBLE);
        Log.i(TAG, "uploading finished");
        Toast.makeText(getApplicationContext(), getString(R.string.upload_complete), Toast.LENGTH_SHORT).show();
    }

//    private int numInitialized() {
//        return initializedUploadIds.size();
//    }

    private int numCompleted() {
        int count = 0;
        for(TransferObserver obs : observers) {
            if (obs.getState() == TransferState.COMPLETED) {
                count++;
            }
        }
        return count;
    }

    private int numInProgress() {
        int count = 0;
        for(TransferObserver obs : observers) {
            if (obs.getState() == TransferState.IN_PROGRESS) {
                count++;
            }
        }
        return count;
    }

    private int numErrors() {
        int count = 0;
        for(TransferObserver obs : observers) {
            if (obs.getState() == TransferState.FAILED) {
                count++;
            }
        }
        return count;
    }

    private int numWaitingForNetwork() {
        int count = 0;
        for(TransferObserver obs : observers) {
            if (obs.getState() == TransferState.WAITING_FOR_NETWORK) {
                count++;
            }
        }
        return count;
    }

    private void clearListeners() {
        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }

        observers.clear();
    }


    private void startUpload() {
        if (totalFiles == 0) {
            showNoImagesAlert();
            return;
        }
//        cancelButton.setVisibility(View.VISIBLE);
        clearListeners();



//        waitingForNetworkIds.clear();
//        initializedUploadIds.clear();
//        inProgressUploadIds.clear();
//        completedUploadIds.clear();
//        errorUploadIds.clear();

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), directoryName);

        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++) {
            File imageFile = directory.listFiles()[i];

            TransferObserver observer = transferUtility.upload(
                    getString(R.string.sw_bucket_bc),
                    sessionId + "_" + imageFile.getName(),
                    imageFile);
            Log.i(TAG, "starting upload: " + String.valueOf(observer.getId()));
           // initializedUploadIds.add(observer.getId());
            observers.add(observer);

            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.i(TAG, String.valueOf(id) + " state: " + state.toString());

//                    if (state == TransferState.WAITING_FOR_NETWORK) {
//                        waitingForNetworkIds.add(id);
//                    }
//
//                    if (state == TransferState.IN_PROGRESS) {
//                        inProgressUploadIds.add(id);
//                    } else if (state == TransferState.COMPLETED) {
//                        inProgressUploadIds.remove(id);
//                        completedUploadIds.add(id);
//
//                    }
                    checkProgressStatus();
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    checkProgressStatus();
                    //Log.i(TAG, "progress changed:" + String.valueOf(id) + " " + String.valueOf(bytesCurrent) + "/" + String.valueOf(bytesTotal));
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i(TAG, "Error: " + String.valueOf(id) + " " + ex.toString());
//                    inProgressUploadIds.remove(id);
//                    errorUploadIds.add(id);
                    checkProgressStatus();
                }
            });

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (observers != null && !observers.isEmpty()) {
            for (TransferObserver observer : observers) {
                observer.cleanTransferListener();
            }
        }

    }

    private Integer checkNumberOfFilesInDirectory() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), directoryName);

        if (directory == null) {
            return 0;
        }
        if (!directory.isDirectory()) {
            return 0;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return 0;
        }

        return files.length;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private String getDirectoryNameFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(getString(R.string.megamovie_directory_name), "Megamovie_Images");
    }

    private String getSessionId() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sessionId = prefs.getString(getString(R.string.session_id_key),"");
        if (sessionId.equals("")) {
            sessionId = generateSessionId();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.session_id_key),sessionId);
            editor.commit();
        }

        return sessionId;

    }

    private String generateSessionId() {
        Random random = new Random();

        String id = "";
        while (id.length() < 64) {
            id = id + Integer.toHexString(random.nextInt());
        }

        return id;
    }


    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
