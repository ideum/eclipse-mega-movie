package ideum.com.megamovie.Java.Application;

import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;

//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonArrayRequest;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by MT_User on 7/31/2017.
 */

public class MegamovieProfileCreator {
    private Context mContext;
    private String userId;
    private static final String TAG = "MegamovieProfileCreator";
    private boolean profileExists = false;



    public MegamovieProfileCreator(Context context, String userId) {

        mContext = context;
        this.userId = userId;

        String token = generatedIdToken();
        OkHttpClient client = new OkHttpClient();
        String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);


        com.squareup.okhttp.Request request = new Request.Builder()
                .header("Authorization","Basic dGVzdDpkYXJrZW4=")
                .header("x-idtoken",token)
                .url(url)
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
                    if(response.code() == 200) {
                        profileExists = true;
                    }
                }
            }
        });
    }

    public boolean uploadFile() {


        OkHttpClient client = new OkHttpClient();

        File rootDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Megamovie practice Jul 24 18:35 PM" );
        File file = rootDirectory.listFiles()[0];


        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file",file.getName(), RequestBody.create(MediaType.parse("jpg"),file))
                .build();

        String url = "https://test.eclipsemega.movie/services/upload/";
        String sessionID = generateSessionId();

        final Request request = new Request.Builder()
                .header("x-uploadsessionid",generateSessionId())
                .header("Authorization","Basic dGVzdDpkYXJrZW4=")
                .header("x-image-bucket","app")
                .header("x-idtoken",generatedIdToken())
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
                   Log.i(TAG,response.toString());
                }
            }
        });




        return true;
    }

    private String generatedIdToken() {
        return "eyJhbGciOiJSUzI1NiIsImtpZCI6IjU2NjE5YWRiMjJkMWE1NDU2MjAzNmJmNTEwODBmZjZjZjdjZTNjZjIifQ.eyJhenAiOiIyOTA4Mjg5NzkyNzItOGEyMDQzZjBmNnBkbjg2ODhxb29pNTYyM2hpZTF1MWcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyOTA4Mjg5NzkyNzItOGEyMDQzZjBmNnBkbjg2ODhxb29pNTYyM2hpZTF1MWcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTE2MzgyMzM2NDgzMTMxNDY4NTYiLCJlbWFpbCI6Im1lZ2Ftb3ZpZWFwcHRlc3RAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJCaTYxZ19objFvV0oyMnNlbnUwMV9nIiwiaXNzIjoiYWNjb3VudHMuZ29vZ2xlLmNvbSIsImlhdCI6MTUwMTYxMzAzNSwiZXhwIjoxNTAxNjE2NjM1LCJuYW1lIjoiQnJheHRvbiBDb2xsaWVyIiwicGljdHVyZSI6Imh0dHBzOi8vbGg0Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tOHREM1VWcFd2a28vQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQU1wNVZVcnpaeEhLTW85OXRXeFEyY2lNcE9qNTU2eGtndy9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoiQnJheHRvbiIsImZhbWlseV9uYW1lIjoiQ29sbGllciIsImxvY2FsZSI6ImVuIn0.SKLiqzkjisxkOyjdU1XO9_E2-tge6dhKJmWv1MQdZjLrmXxWjnlCmysLi6Eopm2iSmaiJDYB-waTXkE66brOQYSVaSNcNtNy44A-FZOkFwWDaaB6YPiCaBzEemQvp7GbGcHfPyYgrQGYANZsQloYN-1zQrrhe02wos4baKBRVuIdLT6wcn6HD-poc4ZIpCP-TszEWbvCMnQaz04cKs1TuonPcInyyMuIs0iStpoG-UlEk1rgYS5uf1L-bXbDjrmcBPF24HSrs5iZT57dqgEcR-YH1YjtTjYEOXANRcquaJg1j-_mfJJ-iHUEmkYS5gy2k9LTQP6SCt-UUzw8TeQbvA";
    }

    private String generateSessionId() {
        Random random = new Random();

        String id = "";
        while(id.length() < 64) {
            id = id + Integer.toHexString(random.nextInt());
        }

        return id;

    }


    public boolean checkProfileExists() {
        return profileExists;
    }

    private void updateProfileExists() {

        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjM4ZDNlMTNmY2ZkMGVhODI3YjU3MTk3ZjRkNjY1Y2VlNjBlYmY2YjAifQ.eyJhenAiOiIyOTA4Mjg5NzkyNzItOGEyMDQzZjBmNnBkbjg2ODhxb29pNTYyM2hpZTF1MWcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIyOTA4Mjg5NzkyNzItOGEyMDQzZjBmNnBkbjg2ODhxb29pNTYyM2hpZTF1MWcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTc2NTA4NzkwNTIzMTcyMTAwNjAiLCJoZCI6ImlkZXVtLmNvbSIsImVtYWlsIjoiYnJheHRvbkBpZGV1bS5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IkFodDFqSGdOSm0wVTZNWVoxcG5zd3ciLCJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiaWF0IjoxNTAxNjAyNTE3LCJleHAiOjE1MDE2MDYxMTcsIm5hbWUiOiJCcmF4dG9uIENvbGxpZXIiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDYuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1UVkZYelJ2N1dlRS9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BTXA1VlVxN0M4YmVWWEZqMWhXWkx1cFJqVXpTdWxnN1lnL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbWUiOiJCcmF4dG9uIiwiZmFtaWx5X25hbWUiOiJDb2xsaWVyIiwibG9jYWxlIjoiZW4ifQ.aSix2CE9SpDOnOo3CucQ9fDmKgozbKPLzu1vs1NcTfj_pLRA-U_mqjvKXICyTIec_NQb2YbPd6gqHGijsW7PVFiw6B7kc0_h6aYngyslH8Nt0KPsrvIIxh23Yw4PpqE0x6hQ9CAlUWhTwNq_2oB96FRY5X953xHNBbGKoNDaUaY4xl6JWrPtU5GKn-G9AHITXTusZ0wu46p4Akgr6e9vsm-mk811syjh-GEkp6bTYoiWOfzTB0bBcnNyRmQ1_qRcVM_U9QWKQiSUTFwtnEgFJUSxx-9Kk7kVr8C5VnSgdtZqNzM_9Pr0xdtSbb1Qg6-NQHRtYPxLlL1H6-N8xvw05Q";

        OkHttpClient client = new OkHttpClient();
        String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);


        com.squareup.okhttp.Request request = new Request.Builder()
                .header("Authorization","Basic dGVzdDpkYXJrZW4=")
                .header("x-idtoken",token)
                .url(url)
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
                    if(response.code() == 200) {
                       profileExists = true;
                    }
                }
            }
        });

//         RequestQueue queue = Volley.newRequestQueue(mContext);
//         String url = "https://test.eclipsemega.movie/services/user/profile/" + getSHA256Hash(userId);
//
//         Map<String,String> headers = new HashMap<>();
//
//         headers.put("authorization", "Basic dGVzdDpkYXJrZW4=");
//
//         StringRequestWithHeaders stringRequest = new StringRequestWithHeaders(Request.Method.GET, headers, url, new Response.Listener<String>() {
//             @Override
//             public void onResponse(String response) {
//                Log.i(TAG,response.toString());
//
//             }
//         }, new Response.ErrorListener() {
//             @Override
//             public void onErrorResponse(VolleyError error) {
//                 Log.i(TAG,error.toString());
//             }
//         });
//
//         queue.add(stringRequest);
//         // Return dummy value for now

    }

//    class StringRequestWithHeaders extends StringRequest {
//        private Map<String, String> headers;
//
//        public StringRequestWithHeaders(int method, Map<String, String> headers, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
//
//            super(method, url, listener, errorListener);
//            this.headers = headers;
//        }
//
//        @Override
//        public Map<String, String> getHeaders() throws AuthFailureError {
//            return headers;
//        }
//    }

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

}
