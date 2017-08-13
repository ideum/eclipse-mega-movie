package ideum.com.megamovie.Java.Application;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;


import java.io.File;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class UploadActivity extends AppCompatActivity{

    public static final String TAG = "UploadTestActivity";

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};


Button uploadButton;

    TextView uploadProgressTextView;
    TextView fileSummaryTextView;
    TextView uploadErrorsTextView;
    int totalFiles = 0;


    String directoryName;

    private Set<Integer> initializedUploadIds;
    private Set<Integer> inProgressUploadIds;
    private Set<Integer> completedUploadIds;
    private Set<Integer> errorUploadIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        initializedUploadIds = new HashSet<>();
        completedUploadIds = new HashSet<>();
        inProgressUploadIds = new HashSet<>();
        errorUploadIds = new HashSet<>();

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Image Upload");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        directoryName = getDirectoryNameFromPreferences();

        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpload();
            }
        });

        fileSummaryTextView = (TextView) findViewById(R.id.file_summary_text_view);

        uploadProgressTextView = (TextView) findViewById(R.id.upload_progress_text_view);

        uploadErrorsTextView = (TextView) findViewById(R.id.upload_errors_text_view);

        totalFiles = checkNumberOfFilesInDirectory();

        initiateUI();

    }

    private void enableUploadButton() {
        uploadButton.setEnabled(true);
        uploadButton.setBackgroundColor(Color.parseColor("#4285f4"));
    }

    private void disableUploadButton() {
        uploadButton.setEnabled(false);
        uploadButton.setBackgroundColor(Color.parseColor("#dddddd"));
    }


    private void initiateUI() {
        if (totalFiles == 0) {
            fileSummaryTextView.setText("You do not currently have any eclipse iamges to upload. You can return after photographing the eclipse.");
            disableUploadButton();
            uploadProgressTextView.setVisibility(View.GONE);
            uploadErrorsTextView.setVisibility(View.GONE);
        } else {
            fileSummaryTextView.setText(String.format("You currently have %d files reading to upload to the archive",totalFiles));
        }

        //uploadProgressTextView.setText("You do not currently have any uploads in progress");
        updateUI();
    }


    private void updateUI() {

        uploadProgressTextView.setText(String.format("Files Uploaded: %d/%d",numCompleted(),totalFiles));

        if (numErrors() > 0) {
            uploadErrorsTextView.setText(String.format("Upload errors: %d",numErrors()));
        } else {
            uploadErrorsTextView.setText("");
        }
    }

    private void checkProgressStatus() {
        if (numInProgress() == 0) {
            onUploadComplete();
        } else {
            onUploadInProgress();
        }
        updateUI();
    }

    private void onUploadInProgress() {
        disableUploadButton();
    }

    private void onUploadComplete() {
        enableUploadButton();
        Log.i(TAG,"uploading finished");
        Toast.makeText(getApplicationContext(),"Upload Complete",Toast.LENGTH_SHORT).show();
    }

    private int numInitialized() {
        return initializedUploadIds.size();
    }

    private int numCompleted() {
        return completedUploadIds.size();
    }

    private int numInProgress() {
        return inProgressUploadIds.size();
    }

    private int numErrors() {
        return errorUploadIds.size();
    }




    private void startUpload() {

        initializedUploadIds.clear();
        inProgressUploadIds.clear();
        completedUploadIds.clear();
        errorUploadIds.clear();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                getString(R.string.sw3_identity_pool_id_bc),
                Regions.US_EAST_1);

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);

        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), directoryName);

        File[] files = directory.listFiles();

        for (int i = 0; i < files.length;i ++ ) {
            File imageFile = directory.listFiles()[i];

            TransferObserver observer = transferUtility.upload(
                    getString(R.string.sw_bucket_bc),
                    imageFile.getName(),
                    imageFile);
            Log.i(TAG, "starting upload: " + String.valueOf(observer.getId()) );
            initializedUploadIds.add(observer.getId());

            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.i(TAG, String.valueOf(id) + " state: " + state.toString() );
                     if (state == TransferState.IN_PROGRESS) {
                        inProgressUploadIds.add(id);
                    }
                    else if (state == TransferState.COMPLETED) {
                        inProgressUploadIds.remove(id);
                         completedUploadIds.add(id);

                    }
                    checkProgressStatus();
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    //Log.i(TAG, "progress changed:" + String.valueOf(id) + " " + String.valueOf(bytesCurrent) + "/" + String.valueOf(bytesTotal));
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i(TAG, "Error: " + String.valueOf(id) + " " + ex.toString());
                    inProgressUploadIds.remove(id);
                    errorUploadIds.add(id);
                    checkProgressStatus();
                }
            });

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
        return preferences.getString(getString(R.string.megamovie_directory_name),"Megamovie_Images");
    }

    private String generateSessionId() {
        Random random = new Random();

        String id = "";
        while(id.length() < 64) {
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
