package com.android.example.gawala.Generel.Fraagments;

import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.example.gawala.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImageViewerFragment extends DialogFragment {
    private static final String ARG_URL = "url";
    private static final String ARG_TRANSITION_NAME = "transitionName";
    private PhotoView mPhotoView;

    private String url;
    private String transitionname;


    public static ImageViewerFragment newInstance(String url, String transitionName) {

        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TRANSITION_NAME, transitionName);
        ImageViewerFragment fragment = new ImageViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        postponeEnterTransition();
//        android:theme="@style/Theme.AppCompat.Light.NoActionBar.FullScreen"
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenNoStatusBar);
//        postponeEnterTransition();

        if (getArguments() != null) {
            url = getArguments().getString(ARG_URL);
            transitionname = getArguments().getString(ARG_TRANSITION_NAME);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(android.R.transition.explode));
////            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.explode));
//        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_image_viewer, container, false);
        mPhotoView = rootView.findViewById(R.id.pv_frag_imageViewer);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mPhotoView.setTransitionName(transitionname);
//        }
        Glide.with(getActivity()).load(url).into(mPhotoView);
//        startPostponedEnterTransition();

        return rootView;
    }
}