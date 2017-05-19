package ideum.com.megamovie.Java.NewUI;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import java.net.URI;

import ideum.com.megamovie.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryFragment extends Fragment {

    private VideoView mVideoView;

    public GalleryFragment() {
        // Required empty public constructor
    }


    public static GalleryFragment newInstance(String param1, String param2) {
        GalleryFragment fragment = new GalleryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        mVideoView = (VideoView) rootView.findViewById(R.id.video_view);

        Uri videoURi = Uri.parse("android.resource://ideum.com.megamovie/raw/eclipse_timelapse.mp4");

        mVideoView.setVideoURI(Uri.parse("android.resource://ideum.com.megamovie/raw/eclipse_timelapse"));
        mVideoView.start();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
