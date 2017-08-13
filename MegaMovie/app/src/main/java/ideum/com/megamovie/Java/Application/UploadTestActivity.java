package ideum.com.megamovie.Java.Application;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class UploadTestActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        UploadFragment.UploadListener {

    public static final String TAG = "UploadTestActivity";

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};


    private Boolean uploadInProgress = false;

    private String userId;
    String idToken;
    private String sessionID;
    private String email;
    private String name;

    private UploadFragment mUploadFragment;

    private static final int RC_SIGN_IN = 9002;
    private GoogleApiClient mGoogleApiClient;

    TextView emailTextView;
    SignInButton signInButton;
    Button signOutButton;
    Button uploadButton;
    TextView uploadProgressTextView;
    TextView fileSummaryTextView;

    int filesUploaded = 0;
    int totalFiles = 0;

    String directoryName;

    private Set<Integer> uploadIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_test);

        uploadIds = new HashSet<>();

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Image Upload");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        directoryName = getDirectoryNameFromPreferences();



        mUploadFragment = new UploadFragment();
        mUploadFragment.addListener(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(
                android.R.id.content, mUploadFragment).commit();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        mGoogleApiClient.connect();

        fileSummaryTextView = (TextView) findViewById(R.id.file_summary_text_view);
        uploadProgressTextView = (TextView) findViewById(R.id.upload_progress_text_view);

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        emailTextView = (TextView) findViewById(R.id.email_text_view);


        uploadButton = (Button) findViewById(R.id.upload_button);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startUpload();
                uploadToS3();
            }
        });

        totalFiles = checkNumberOfFilesInDirectory();
        if (totalFiles == 0) {
            fileSummaryTextView.setText("You do not currently have any eclipse iamges to upload. You can return after photographing the eclipse.");
        } else {
            fileSummaryTextView.setText(String.format("You currently have %d files reading to upload to the archive",totalFiles));
        }

    }
//
//    private void uploadFileToS3(File file) {
//        TransferObserver observer = transferUtility.upload(
//                "megamovie",
//                file.getName(),
//                file);
//
//        observer.setTransferListener(new TransferListener() {
//            @Override
//            public void onStateChanged(int id, TransferState state) {
//                Log.i(TAG, String.valueOf(id) + " state: " + state.toString() );
//            }
//
//            @Override
//            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                //Log.i(TAG, "progress changed:" + String.valueOf(id) + " " + String.valueOf(bytesCurrent) + "/" + String.valueOf(bytesTotal));
//            }
//
//            @Override
//            public void onError(int id, Exception ex) {
//                Log.i(TAG, "Error: " + String.valueOf(id) + " " + ex.toString());
//            }
//        });
//
//    }


    private void uploadToS3() {

        uploadIds.clear();
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
            uploadIds.add(observer.getId());

            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    Log.i(TAG, String.valueOf(id) + " state: " + state.toString() );
                    if (state == TransferState.COMPLETED || state == TransferState.FAILED) {
                       uploadIds.remove(id);
                        if (uploadIds.isEmpty()) {
                            Log.i(TAG,"uploading finished");
                            Toast.makeText(getApplicationContext(),"Finished uploading",Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    //Log.i(TAG, "progress changed:" + String.valueOf(id) + " " + String.valueOf(bytesCurrent) + "/" + String.valueOf(bytesTotal));
                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.i(TAG, "Error: " + String.valueOf(id) + " " + ex.toString());
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


    private void startUpload() {
        sessionID = generateSessionId();
        mUploadFragment.uploadFilesInDirectory(getDirectoryNameFromPreferences(),sessionID,idToken);
        uploadButton.setEnabled(false);
        uploadInProgress = true;
        updateUI();
    }

    private boolean isSignedIn() {
        return userId != null;
    }

    private void updateUI() {
        if (uploadInProgress) {
            uploadProgressTextView.setText("Upload in Progress");
        }
        if (isSignedIn()) {
            emailTextView.setText("You are signed in as: \n\n" + email);
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            if (!uploadInProgress && totalFiles != 0) {
                uploadButton.setEnabled(true);
            }
        } else {
            emailTextView.setText("Please sign in to proceed.");
            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            uploadButton.setEnabled(false);

        }
    }

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                idToken = null;
                userId = null;
                email = null;
                name = null;
                updateUI();
            }
        });
    }


    private void showConfirmationDialog(final List<String> fileNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
       String message = String.format("You have uploaded %d images to the Megamovie archive.",fileNames.size());
        message += "\nWould you like to submit your images to the Megamovie Archive?";
        builder.setMessage(message)
                .setTitle(getResources().getString(R.string.safety_warning_title))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mUploadFragment.sendConfirmation(sessionID,idToken, fileNames);
                    }
                })
                .setNegativeButton("No",null)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            idToken = acct.getIdToken();
            userId = acct.getId();
            email = acct.getEmail();
            name = acct.getDisplayName();
            updateUI();
            mUploadFragment.createProfileIfNecessary(userId,name, email,idToken);
        }
    }

    private String generateSessionId() {
        Random random = new Random();

        String id = "";
        while(id.length() < 64) {
            id = id + Integer.toHexString(random.nextInt());
        }

        return id;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
    public void onUploadProgress(Integer filesUploaded, Integer filesTotal) {
        this.filesUploaded = filesUploaded;
        this.totalFiles = filesTotal;
        updateUI();


    }

    @Override
    public void onUploadComplete(List<String> uploadedFileNames) {
        showConfirmationDialog(uploadedFileNames);
        uploadProgressTextView.setText("Upload Completed!");
        uploadInProgress = false;
        updateUI();
    }

    @Override
    public void onConfirmationResponseReceived(Boolean isSuccess) {
        if (isSuccess) {
            Toast.makeText(this, "Images submitted succesfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Submission failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUploadFailed() {
        Toast.makeText(this, "UploadFailed", Toast.LENGTH_SHORT).show();
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

    public class AmazonService extends AsyncTask<String, Boolean, Boolean> {
        Context mContext;
        public AmazonService(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    mContext,
                    "us-east-1:0245a165-48e2-48de-9735-9c0ad3260039", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
            AmazonS3Client client =
                    new AmazonS3Client(credentialsProvider);
            TransferUtility transferUtility = new TransferUtility(client, mContext);
            TransferObserver observer = transferUtility.upload("elevator-app","Video/",new File("dummy.txt") );
            Log.d("Test", observer.getId() + " " + observer.getBytesTransferred());

            return true;
        }
    }
}
