package ideum.com.megamovie.Java;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ideum.com.megamovie.R;



public class CameraPreviewFragment extends Fragment {

//    public CameraPreviewFragment() {
//        // Required empty public constructor
//    }
//
//    public static CameraPreviewFragment newInstance() {
//        CameraPreviewFragment fragment = new CameraPreviewFragment();
//        Bundle args = new Bundle();
//
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }



}
