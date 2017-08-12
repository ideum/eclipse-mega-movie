package ideum.com.megamovie.Java.Application;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;


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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment
        implements GoogleApiClient.OnConnectionFailedListener {


    public interface UploadListener {
        void onUploadProgress(Integer filesUploaded,Integer filesTotal);
        void onUploadComplete(List<String> uploadedFileNames);
        void onConfirmationResponseReceived(Boolean isSuccess);
        void onUploadFailed();
    }

    public void addListener(UploadListener listener) {
        completionListeners.add(listener);
    }
    private List<UploadListener> completionListeners = new ArrayList<>();


    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private final static String TAG = "UploadFragment";
    //    private String userId;
//    private String idToken;
//    private String sessionID;
//    private String email;
//    private String name;
    private FirebaseAuth mAuth;



    private Queue<File> fileQueue = new LinkedList<>();
    //    private List<String> uploadedFileNames = new ArrayList<>();
    private OkHttpClient client = new OkHttpClient();

    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onStart() {
        super.onStart();
        //signIn();
    }

    public void createMegamovieAccount(String userId, String name, String email, String idToken) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();


        try {
            json.put("name", name);
            json.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(JSON, json.toString());

        final Request request = new Request.Builder()
                .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                .header("x-idtoken", idToken)
                .url(url)
                .put(requestBody)
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
                    if (response.code() == 200) {
                        Log.i(TAG, "Success!");
                    } else {
                        onUploadFailed();
                    }
                }
            }
        });
    }

    public void uploadFilesInDirectory(String directoryName, String sessionId, String idToken) {
        UploadFilesTask task = new UploadFilesTask();
        task.execute(directoryName, sessionId, idToken);
    }


    void sendConfirmation(String sessionId, String idToken, List<String> uploadedFileNames) {
        ConfirmationTask task = new ConfirmationTask(sessionId,idToken,uploadedFileNames);
        task.execute();

    }

    private void onUploadFailed() {
        Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }


    public void createProfileIfNecessary(String userId,String name,String email,  String idToken) {
        CheckMMProfileExistsTask task = new CheckMMProfileExistsTask();
        task.execute(userId, name, email, idToken);

    }

    private void onProfileChecked(boolean result, String userId, String name,String email, String idToken) {
        Toast.makeText(getActivity(), "profile exists: " + String.valueOf(result), Toast.LENGTH_LONG).show();
        if (!result) {
            createMMProfile(userId,name,email,idToken);
        }
    }

    public void createMMProfile(String userId, String name, String email, String idToken) {
        CreateMMProfileTask task = new CreateMMProfileTask();
        task.execute(userId, name, email, idToken);
    }

    private void onProfileCreated(boolean result) {
        Toast.makeText(getActivity(), "profile created successfully: " + String.valueOf(result), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    static String getSHA256Hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(text.getBytes());
            byte[] digest = md.digest();

            return bin2hex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }

    private class ConfirmationTask extends AsyncTask<Void,Void,Boolean> {
        private String sessionId;
        private String idToken;
        private List<String> fileNames;

        public ConfirmationTask(String sessionId,String idToken,List<String> fileNames) {
            this.sessionId = sessionId;
            this.idToken = idToken;
            this.fileNames = fileNames;
        }


        @Override
        protected Boolean doInBackground(Void... voids) {
            return sendConfirmation(sessionId,idToken,fileNames);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            for(UploadListener listener : completionListeners) {
                listener.onConfirmationResponseReceived(aBoolean);
            }
        }

        boolean sendConfirmation(String sessionId, String idToken, List<String> fileNames) {

            String url = "https://test.eclipsemega.movie/services/photo/confirm";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();

            JSONArray jsonNames = new JSONArray(fileNames);

            try {
                json.put("filenames", jsonNames);
                json.put("upload_session_id", sessionId);
                json.put("anonymous_photo", false);
                json.put("equatorial_mount", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            RequestBody requestBody = RequestBody.create(JSON, json.toString());
            Log.i(TAG, "json string: " + json);

            final Request request = new Request.Builder()
                    .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                    .header("x-idtoken", idToken)
                    .header("X-IDEUM-APP-SECRET", "abf31acfccb6194e4a4c888764e2b426403a380f75cb0a038d875ed1c5ca572c")
                    .url(url)
                    .post(requestBody)
                    .build();

            boolean isSuccess = false;
            try {
                Response response = client.newCall(request).execute();
                Log.i("upload","confirmation response: " + String.valueOf(response.code()));
                if (response.code() == 200) {
                    isSuccess = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }

    }


    private class CreateMMProfileTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            String userId = strings[0];
            String name = strings[1];
            String email = strings[2];
            String idToken = strings[3];
            String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject json = new JSONObject();


            try {
                json.put("name", name);
                json.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody requestBody = RequestBody.create(JSON, json.toString());

            final Request request = new Request.Builder()
                    .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                    .header("x-idtoken", idToken)
                    .url(url)
                    .put(requestBody)
                    .build();

            Boolean isSuccess = false;
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    isSuccess = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            onProfileCreated(aBoolean);
        }
    }

    private class CheckMMProfileExistsTask extends AsyncTask<String, Void, Boolean> {

        private String userId;
        private String idToken;
        private String email;
        private String name;

        @Override
        protected Boolean doInBackground(String... strings) {
            userId = strings[0];
            name = strings[1];
            email = strings[2];
            idToken = strings[3];


            OkHttpClient client = new OkHttpClient();
            String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);

            com.squareup.okhttp.Request request = new Request.Builder()
                    .header("Authorization", "Basic dGVzdDpkYXJrZW4=")
                    .header("x-idtoken", idToken)
                    .url(url)
                    .build();

            boolean profileExists = false;
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    profileExists = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return profileExists;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            onProfileChecked(result, userId, name, email, idToken);
        }
    }

    private class UploadFilesTask extends AsyncTask<String, Integer, List<String>> {
        private String sessionId;
        private List<String> uploadedFileNames = new ArrayList<>();

        @Override
        protected List<String> doInBackground(String... strings) {
            String directoryName = strings[0];
            sessionId = strings[1];
            String idToken = strings[2];

            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), directoryName);
            if (!directory.isDirectory()) {
                return null;
            }
            File[] files = directory.listFiles();
            if (files == null) {
                return null;
            }

            int uploadNumber = 0;
            int totalUploads = files.length;

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                Boolean isSuccess = uploadFile(file, sessionId, idToken);
                if (isSuccess) {
                    uploadedFileNames.add(file.getName());
                    publishProgress(uploadNumber,totalUploads);
                    uploadNumber++;
                }
            }
            return uploadedFileNames;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("progress","progress update");
            for (UploadListener listener : completionListeners) {
                listener.onUploadProgress(values[0],values[1]);
            }
        }

        @Override
        protected void onPostExecute(List<String> fileNames) {

            for (UploadListener listener : completionListeners) {
                listener.onUploadComplete(fileNames);
            }
        }

        Boolean uploadFile(File file, String sessionId, String token) {

            RequestBody requestBody = new MultipartBuilder()
                    .type(MultipartBuilder.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse(getMimeType(file.getAbsolutePath())), file))
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

            Boolean isSuccess = false;

            Log.i("upload","starting upload: " + file.getName());
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    isSuccess = true;
                    Log.i("upload", "file uploaded : " + file.getName());
                } else {
                    Log.i("upload", "upload failed: " + String.valueOf(response.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isSuccess;
        }
    }


}
