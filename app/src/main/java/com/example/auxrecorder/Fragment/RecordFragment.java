package com.example.auxrecorder.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.auxrecorder.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements View.OnClickListener {

    private static final int AUDIO_PERMISSION_CODE = 89 ;
    private NavController navController;
    private ImageButton list_btn;
    private ImageButton record_btn;
    private boolean is_recording = false;
    private String recording_permission = Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private String record_file;
    private Chronometer chronoTimer;
    private TextView record_file_name;

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        list_btn = view.findViewById(R.id.record_list_button);
        record_btn = view.findViewById(R.id.record_button);
        chronoTimer = view.findViewById(R.id.record_timer);
        record_file_name = view.findViewById(R.id.record_filename);

        list_btn.setOnClickListener(this);
        record_btn.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.record_list_button) {
            handleRecordListButtonClick();
        } else if (id == R.id.record_button) {
            handleRecordButtonClick();
        } else {
            // Handle other cases or do nothing
        }
    }

    private void handleRecordListButtonClick() {
        if (is_recording) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Audio still recording")
                    .setMessage("Are you sure you want to stop recording?")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        if (navController != null) {
                            navController.navigate(R.id.action_recordFragment_to_recordListFragment);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            if (navController != null) {
                navController.navigate(R.id.action_recordFragment_to_recordListFragment);
            }
        }
    }

    private void handleRecordButtonClick() {
        if (is_recording) {
            // Stop Recording
            stop_recording();
            if (record_btn != null) {
                record_btn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.record_btn_stopped));
            }
            is_recording = false;
        } else {
            // Start Recording
            if (checkAudioPermission()) {
                start_recording();
                if (record_btn != null) {
                    record_btn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.record_btn_recording));
                }
                is_recording = true;
            }
        }
    }

    private void start_recording()
    {
        chronoTimer.setBase(SystemClock.elapsedRealtime());
        chronoTimer.start();



        String rec_path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_YYYY_hh_mm_ss", Locale.CANADA);
        Date date = new Date();

        record_file = "auxRec"+ simpleDateFormat.format(date) +".3gp";

        record_file_name.setText("Recording File Name: \n"+ record_file);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(rec_path + "/" + record_file);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }


    private void stop_recording()
    {
        chronoTimer.stop();
        is_recording = false;
        record_file_name.setText("Recording Stopped, File Saved: \n"+ record_file);



        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    private boolean checkAudioPermission()
    {
        if(ActivityCompat.checkSelfPermission(getContext(),recording_permission )== PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]{recording_permission}, AUDIO_PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(is_recording) {
            stop_recording();
        }
    }
}