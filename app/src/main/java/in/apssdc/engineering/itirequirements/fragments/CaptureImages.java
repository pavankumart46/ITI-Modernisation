package in.apssdc.engineering.itirequirements.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.apssdc.engineering.itirequirements.helper.ConnectionCheck;
import in.apssdc.engineering.itirequirements.model.Images;
import in.apssdc.engineering.itirequirements.R;
import in.apssdc.engineering.itirequirements.model.Videos;

import static android.content.Context.MODE_PRIVATE;

public class CaptureImages extends Fragment {

    private EditText imageName_et;
    String dir_path = "";
    private ImageView imageView;
    private VideoView videoView;
    String img_name = "";
    String video_name = "";
    Button captureImageButton, recordVideoButton, uploadImages;
    File dir, video_dir;
    List photo_names, photo_paths, video_names, video_paths;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;
    String selected_dis, selected_college;
    Spinner image_category;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_capture_images, container, false);
        imageName_et = v.findViewById(R.id.imageName);
        imageView = v.findViewById(R.id.capturedImage);
        captureImageButton = v.findViewById(R.id.capture);
        recordVideoButton = v.findViewById(R.id.recordVideo);
        uploadImages = v.findViewById(R.id.uploadImages);
        videoView = v.findViewById(R.id.capturedVideo);
        photo_names = new ArrayList();
        photo_paths = new ArrayList();
        video_names = new ArrayList();
        video_paths = new ArrayList();
        image_category = v.findViewById(R.id.imageCategorySpinnerId);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        sharedPreferences = getActivity().getSharedPreferences("signindetails", MODE_PRIVATE);
        if (sharedPreferences.contains("SELECTED_DISTRICT")) {
            selected_dis = sharedPreferences.getString("SELECTED_DISTRICT", null);
            selected_college = sharedPreferences.getString("SELECTED_COLLEGE", null);
        }
        dir_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ITIImages";
        dir = new File(dir_path);
        if (!dir.exists())
            dir.mkdirs();
        File filepath = Environment.getExternalStorageDirectory();
        video_dir = new File(filepath.getAbsolutePath() + "/" + "ITIVideos" + "/");
        if (!video_dir.exists()) {
            video_dir.mkdirs();
        }
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_name = imageName_et.getText().toString();
                if (img_name.equalsIgnoreCase("")) {
                    imageName_et.setError("Please enter image name");
                } else if (image_category.getSelectedItem().toString().equalsIgnoreCase("Select Category of Image")) {
                    Toast.makeText(getActivity(), "Please choose Image Category", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 2);
                }
            }
        });
        recordVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                video_name = imageName_et.getText().toString();
                if (video_name.equalsIgnoreCase("")) {
                    imageName_et.setError("Please enter image name");
                } else {
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    startActivityForResult(intent, 3);
                }
            }
        });
        uploadImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectionCheck.checkConnection(getContext())) {
                    loadImagesFromInternalStorageAndUpload(dir);
                    loadVideosFromInternalStorageAndUpload(video_dir);
                } else {
                    ConnectionCheck.showConnectionDisabledAlert();
                }

            }
        });
        return v;
    }

    private void loadVideosFromInternalStorageAndUpload(File video_dir) {
        String extention = ".mp4";
        File[] listFile = video_dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    loadVideosFromInternalStorageAndUpload(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(extention)) {
                        video_names.add(listFile[i].getName());
                        video_paths.add(listFile[i].getAbsolutePath());
                        Toast.makeText(getActivity(), "" + video_names.get(i), Toast.LENGTH_SHORT).show();
                        final StorageReference filesToUpload = storageReference.child(selected_dis).child(selected_college).child((String) video_names.get(i));
                        final int finalI = i;
                        filesToUpload.putFile(Uri.parse((String) "file://" + video_paths.get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filesToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Videos videos = new Videos(uri.toString(), video_names.get(finalI).toString());
                                        databaseReference.child(selected_dis).child(selected_college).child("Videos").child(String.valueOf(new Date().getTime())).setValue(videos);
                                        Toast.makeText(getContext(), "Successfully Uploaded Videos Files", Toast.LENGTH_SHORT).show();
                                    }
                                });
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

    private void loadImagesFromInternalStorageAndUpload(final File dir) {
        String extention = ".jpg";
        String[] imaCatAndNameArray;
        File[] listFile = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    loadImagesFromInternalStorageAndUpload(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(extention)) {
                        photo_names.add(listFile[i].getName());
                        photo_paths.add(listFile[i].getAbsolutePath());
                        Toast.makeText(getActivity(), "" + photo_names.get(i), Toast.LENGTH_SHORT).show();
                        String photoInfo = photo_names.get(i).toString();
                        imaCatAndNameArray = photoInfo.split(",");
                        // 0 represents image category and 1 represents image name
                        final StorageReference filesToUpload = storageReference.child(selected_dis).child(selected_college).child((String) imaCatAndNameArray[1]);
                        final int finalI = i;
                        final String[] finalImageCategoryAndNameArray = imaCatAndNameArray;
                        filesToUpload.putFile(Uri.parse((String) "file://" + photo_paths.get(i))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                filesToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                                        @Override
                                                                                        public void onSuccess(Uri uri) {
                                                                                            Images images = new Images(uri.toString(),finalImageCategoryAndNameArray[1]);
                                                                                            databaseReference.child(selected_dis).child(selected_college).child("Images").child(finalImageCategoryAndNameArray[0]).child(String.valueOf(new Date().getTime())).setValue(images);
                                                                                            Toast.makeText(getContext(), "Successfully Uploaded Files", Toast.LENGTH_SHORT).show();
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

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 2) {
                if (data != null) {
                    imageName_et.setText("");
                    videoView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    Bitmap bmp = (Bitmap) data.getExtras().get("data");

                    String f_name = Environment.getExternalStorageDirectory().toString() + File.separator + "ITIImages";
                    File file = new File(f_name, image_category.getSelectedItem().toString() + "," + img_name + ".jpg");
                    try {
                        FileOutputStream fos = new FileOutputStream(file);
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.flush();
                        fos.close();

                        Bitmap b = BitmapFactory.decodeStream(new FileInputStream(file));
                        Glide.with(getContext()).load(b).into(imageView);

                        Toast.makeText(getContext(), "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (requestCode == 3) {
                if (data != null) {
                    imageView.setVisibility(View.GONE);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoPath(data.getData().toString());
                    videoView.start();
                    imageName_et.setText("");
                    try {
                        File newfile;
                        AssetFileDescriptor videoAsset = getActivity().getContentResolver().openAssetFileDescriptor(data.getData(), "r");
                        FileInputStream in = videoAsset.createInputStream();
                        newfile = new File(video_dir, video_name + ".mp4");
                        if (newfile.exists()) newfile.delete();
                        OutputStream out = new FileOutputStream(newfile);
                        // Copy the bits from instream to outstream
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        in.close();
                        out.close();
                        Log.v("", "Copy file successful.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }
}
