package in.apssdc.engineering.itirequirements.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.helper.ConnectionCheck;
import in.apssdc.engineering.itirequirements.model.Images;
import in.apssdc.engineering.itirequirements.model.Recordings;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SoundRecording extends Fragment {

    private String path_save = "";
    private static final int REQUEST_PERMISSION_CODE = 1000;
    private EditText soundRec_et;
    Button rec_audio_but;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    String selected_dis, selected_college;
    File rec_dir;
    Button uploadRecording_but;
    List rec_names, rec_paths;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_sound_recording, container, false);
        if (!checkPermissionFromDevice())
            requestPermission();
        soundRec_et = v.findViewById(R.id.editText);
        rec_audio_but = v.findViewById(R.id.recordAudio);
        uploadRecording_but = v.findViewById(R.id.uploadRecording);
        rec_names = new ArrayList();
        rec_paths = new ArrayList();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        sharedPreferences = getActivity().getSharedPreferences("signindetails", MODE_PRIVATE);
        if (sharedPreferences.contains("SELECTED_DISTRICT")) {
            selected_dis = sharedPreferences.getString("SELECTED_DISTRICT", null);
            selected_college = sharedPreferences.getString("SELECTED_COLLEGE", null);
        }
        path_save = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ITIRecordings";
        rec_dir = new File(path_save);
        if (!rec_dir.exists()) {
            rec_dir.mkdirs();
        }
        rec_audio_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rec_name = soundRec_et.getText().toString();
                if (rec_name.equalsIgnoreCase("")) {
                    soundRec_et.setError("Please enter name and Designation");
                } else {
                    if (checkPermissionFromDevice()) {
                        String path = path_save + "/" + rec_name + ".wav";
                        int color = getResources().getColor(R.color.colorPrimaryDark);
                        AndroidAudioRecorder.with(getActivity()).setFilePath(path)
                                .setColor(color)
                                .setRequestCode(0)
                                .setSource(AudioSource.MIC)
                                .setChannel(AudioChannel.STEREO)
                                .setSampleRate(AudioSampleRate.HZ_48000)
                                .setAutoStart(true)
                                .setKeepDisplayOn(true)
                                .record();

                    } else {
                        requestPermission();
                    }
                }
            }
        });
        uploadRecording_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionCheck.checkConnection(getContext())) {
                    loadRecordingsAndUpload(rec_dir);
                } else {
                    ConnectionCheck.showConnectionDisabledAlert();
                }

            }
        });
        return v;
    }

    private void loadRecordingsAndUpload(File rec_dir) {
        String extention = ".wav";
        File[] listFile = rec_dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    loadRecordingsAndUpload(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(extention)) {
                        rec_names.add(listFile[i].getName());
                        rec_paths.add(listFile[i].getAbsolutePath());
                        Toast.makeText(getActivity(), "" + rec_names.get(i), Toast.LENGTH_SHORT).show();
                        final StorageReference filesToUpload = storageReference.child(selected_dis).child(selected_college).child((String) rec_names.get(i));
                        final int finalI = i;
                        filesToUpload.putFile(Uri.parse((String) "file://" + rec_paths.get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filesToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                        @Override
                                                                                        public void onSuccess(Uri uri) {
                                                                                            Recordings recordings = new Recordings(uri.toString(), (rec_names.get(finalI)).toString());
                                                                                            // Toast.makeText(getContext(), uri.toString(), Toast.LENGTH_SHORT).show();
                                                                                            databaseReference.child(selected_dis).child(selected_college).child("Sound Recordings").child(String.valueOf(new Date().getTime())).setValue(recordings);
                                                                                            Toast.makeText(getContext(), "Successfully Uploaded ", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                );
                            }
                        });
                    }
                }
            }
            if (listFile != null) {
                for (File file : listFile) {
                    file.delete();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                soundRec_et.setText("");
                Toast.makeText(getContext(), "Recording Saved", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getContext(), "Oops not saved", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WAKE_LOCK
                }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_res = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_res = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        int wave_lock_res = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WAKE_LOCK);
        return write_external_storage_res == PackageManager.PERMISSION_GRANTED &&
                record_audio_res == PackageManager.PERMISSION_GRANTED && wave_lock_res == PackageManager.PERMISSION_GRANTED;
    }


}
