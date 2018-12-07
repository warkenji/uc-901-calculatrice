package com.gaswa.calculatrice.mode.image_recognition;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ImageRecognition {
    private Activity activity;
    private final int PICK_IMAGE_REQUEST_CODE = 10;
    private final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 11;

    public ImageRecognition(Activity activity)
    {
        this.activity = activity;
    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
            );
            intent.setType("image/*");
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
        } else {
            String[] permissions;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(
                        activity,
                        permissions,
                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                );
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            if(data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    try {
                        FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(activity, uri);

                        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                                .getOnDeviceTextRecognizer();

                        detector.processImage(image)
                                .addOnSuccessListener(firebaseVisionText -> {

                                })
                                .addOnFailureListener(
                                        Throwable::printStackTrace);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // pick image after request permission success
                    pickImage();
                }
                break;
        }
    }
}
