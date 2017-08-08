package ideum.com.megamovie.Java.Application;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener{


    public interface CompletionListener {
        public void onUploadComplete(List<String> uploadedFileNames);
    }

    private List<CompletionListener> completionListeners = new ArrayList<>();
    public void addCompletionListener(CompletionListener listener) {
        completionListeners.add(listener);
    }


    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private final static String TAG = "UploadFragment";
    String idToken;
    private String sessionID;
    private FirebaseAuth mAuth;

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private Queue<File> fileQueue = new LinkedList<>();
    private List<String> uploadedFileNames = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasAllPermissionsGranted()) {
            requestCameraPermissions();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(),this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onStart() {
        super.onStart();
        signIn();
    }

    public void uploadFilesInDirectory(String directoryName) {
        sessionID = generateSessionId();
        UploadFilesTask task = new UploadFilesTask();
        task.execute(directoryName,sessionID,idToken);
    }


    private void uploadNext() {
        File nextFile = fileQueue.poll();
        if (nextFile != null) {
            uploadedFileNames.add(nextFile.getName());
            uploadFile(nextFile);
        } else {
            onUploadComplete();
        }
    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Would you like to submit your images to the MegamovieArchive?")
                .setTitle(getResources().getString(R.string.safety_warning_title))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendConfirmation();
                    }
                })
                .setNegativeButton("No",null)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendConfirmation()  {

        String url = "https://test.eclipsemega.movie/services/photo/confirm";
         MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();

        JSONArray jsonNames = new JSONArray(uploadedFileNames);

        try {
            json.put("filesnames",jsonNames);
            json.put("upload_session_id",sessionID);
            json.put("anonymous_photo","false");
            json.put("equatorial_mount","false");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        RequestBody requestBody = RequestBody.create(JSON,json.toString());
        Log.i(TAG,"json string: " + json);

        final Request request = new Request.Builder()
                .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                .header("x-idtoken", idToken)
                .header("x-image-bucket", "app")
                .url(url)
                .post(requestBody)
                .build();

        Log.i(TAG,"Request url: " + request.urlString());
        Log.i(TAG,"Request body: " + request.body().toString());
        Log.i(TAG,"Request headers: " + request.headers().toString());


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.i(TAG,"response headers: " + response.headers().toString());
                Log.i(TAG,"response body " + response.body().toString());
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    if (response.code() == 200) {
                        Toast.makeText(getActivity(),"Upload Successful",Toast.LENGTH_SHORT);
                    } else {
                        onUploadFailed();
                    }
                }
            }
        });
    }

    private void onUploadComplete() {
        showConfirmationDialog();

        for(CompletionListener listener : completionListeners) {
            listener.onUploadComplete(uploadedFileNames);
        }
    }

    private void onUploadFailed() {
            Toast.makeText(getActivity(),"Upload failed",Toast.LENGTH_SHORT);
    }

    void uploadFile(File file) {

        RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("jpg"), file))
                    .build();

            String url = "https://test.eclipsemega.movie/services/upload/";

            final Request request = new Request.Builder()
                    .header("x-uploadsessionid", sessionID)
                    .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                    .header("x-image-bucket", "app")
                    .header("x-idtoken", idToken)
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        Log.i("UploadFragment",String.valueOf(response.code()));
                        if (response.code() == 200) {
                            uploadNext();
                        } else {
                            onUploadFailed();
                        }
                    }
                }
            });

    }

    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {
//        signInResultTextView.setText("Sign in Result: " + result.isSuccess());

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            idToken = acct.getIdToken();

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
        ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private class UploadFilesTask extends AsyncTask<String,String,Integer> {
        private String sessionId;
//        private List<String> uploadedFileNames = new ArrayList<>();

        @Override
        protected Integer doInBackground(String... strings) {
            uploadedFileNames.clear();
            String directoryName = strings[0];
            sessionId = strings[1];
            String idToken = strings[2];

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),directoryName);
            if(!directory.isDirectory()) {
                return null;
            }
            File[] files = directory.listFiles();
            if(files == null) {
                return null;
            }

            for(int i = 0; i < files.length;i++) {
                uploadFile(files[i],sessionId,idToken);
                publishProgress(files[i].getName());
            }


            return files.length;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            uploadedFileNames.add(values[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            showConfirmationDialog();
        }

        void uploadFile(File file, String sessionId, String token) {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("jpg"), file))
                    .build();

            String url = "https://test.eclipsemega.movie/services/upload/";

            final Request request = new Request.Builder()
                    .header("x-uploadsessionid", sessionId)
                    .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                    .header("x-image-bucket", "app")
                    .header("x-idtoken", token)
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        Log.i("UploadFileTask",String.valueOf(response.code()));
                        if (response.code() == 200) {
                            //uploadNext();
                        } else {
                            //onUploadFailed();
                        }
                    }
                }
            });
        }
    }


}
