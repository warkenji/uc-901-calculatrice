package com.gaswa.calculatrice.mode.image_recognition;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gaswa.calculatrice.MainActivity;
import com.gaswa.calculatrice.R;
import com.gaswa.calculatrice.mode.Recognition;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.*;

public class ImageRecognition extends Recognition {
    private MainActivity activity;
    private final int IMAGE_REQUEST_CODE = 10;
    private final int IMAGE_PERMISSION_CODE = 11;
    private final int CAMERA_REQUEST_CODE = 12;
    private final int CAMERA_PERMISSION_CODE = 13;

    public ImageRecognition(MainActivity activity)
    {
        this.activity = activity;
    }

    public void pick() {
        String[] items = {"Caméra", "Galerie"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);

        dialog.setItems(items, (dialogInterface, which) -> {
            switch (which) {
                case 0:
                    pickCamera();
                    break;
                case 1:
                    pickImage();
                    break;
            }
        });

        dialog.show();
    }

    private void pickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ActivityCompat.checkSelfPermission(activity, READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{READ_EXTERNAL_STORAGE},
                        IMAGE_PERMISSION_CODE);
            } else {
                Intent imageIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI
                );
                imageIntent.setType("image/*");
                activity.startActivityForResult(imageIntent, IMAGE_REQUEST_CODE);
            }
        }
    }

    private void pickCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ActivityCompat.checkSelfPermission(activity, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{CAMERA},
                        CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FirebaseVisionImage image = null;
        if(resultCode == Activity.RESULT_OK && data != null) {
            try {
        switch(requestCode) {
            case IMAGE_REQUEST_CODE:
                Uri uri = data.getData();

                if (uri != null) {
                        activity.findViewById(R.id.calcul);
                        image = FirebaseVisionImage.fromFilePath(activity, uri);
                }
                break;

            case CAMERA_REQUEST_CODE:
                Bundle bundle = data.getExtras();

                if(bundle != null)
                {
                    Bitmap bitmap = (Bitmap)bundle.get("data");

                    if(bitmap != null)
                    {
                        image = FirebaseVisionImage.fromBitmap(bitmap);
                    }
                }
                break;
            }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(image != null)
        {
            final TextView calcul = activity.findViewById(R.id.calcul);
            final TextView resultat = activity.findViewById(R.id.resultat);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            detector.processImage(image)
                    .addOnSuccessListener(firebaseVisionText -> {
                        String texte = activity.conversion(firebaseVisionText.getText());
                        boolean verif = texte.length() > 0 && texte.charAt(texte.length() - 1) == '=';
                        if(verif)
                        {
                            texte = texte.substring(0, texte.length() - 1);
                        }

                        if(activity.verification(texte))
                        {
                            calcul.setText(texte);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                EditText editText = (EditText) calcul;
                                editText.setSelection(calcul.length());
                            }

                            if(verif)
                            {
                                activity.resolution(activity.findViewById(R.id.resolution));
                            }
                            else
                            {
                                activity.resultatPartiel();
                            }
                        }
                        else
                        {
                            calcul.setText("");
                            resultat.setText("");
                        }
                    })
                    .addOnFailureListener(e-> {
                        calcul.setText("");
                        resultat.setText("");
                        e.printStackTrace();
                    });
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == IMAGE_PERMISSION_CODE || requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == IMAGE_PERMISSION_CODE) {
                    pickImage();
                }
                else
                {
                    pickCamera();
                }
            }
            else
            {
                String message;
                if (requestCode == IMAGE_PERMISSION_CODE) {
                    message = "Permission d'accès à la galerie refusé.";
                }
                else
                {
                    message = "Permission d'accès à la caméra refusé.";
                }

                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
