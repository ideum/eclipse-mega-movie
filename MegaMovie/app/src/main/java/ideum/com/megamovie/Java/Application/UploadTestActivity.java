package ideum.com.megamovie.Java.Application;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.List;
import java.util.Random;

import ideum.com.megamovie.R;

public class UploadTestActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        UploadFragment.UploadListener {

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE};


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

    int filesUploaded = 0;
    int totalFiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_test);

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }



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
                startUpload();
            }
        });

        signIn();
        //updateUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startUpload() {
        sessionID = generateSessionId();
        mUploadFragment.uploadFilesInDirectory("Megamovie practice Jul 24 18:35 PM",sessionID,idToken);
        uploadButton.setEnabled(false);
        uploadInProgress = true;
        updateUI();
    }

    private boolean isSignedIn() {
        return userId != null;
    }

    private void updateUI() {
        if (uploadInProgress) {
            uploadProgressTextView.setText("upload progress: " + String.valueOf(filesUploaded) + "\\" + String.valueOf(totalFiles));
        }
        if (isSignedIn()) {
            emailTextView.setText("You are signed in as: \n\n" + email);
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            if (!uploadInProgress) {
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
        uploadProgressTextView.setText("Upload Completed: " + String.valueOf(uploadedFileNames.size()) + " images");
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
}
