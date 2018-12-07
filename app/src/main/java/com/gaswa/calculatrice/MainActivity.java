package com.gaswa.calculatrice;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gaswa.calculatrice.donnee.BDD;
import com.gaswa.calculatrice.donnee.ItemHistorique;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {
    private BDD bdd;
    private final int PICK_IMAGE_REQUEST_CODE = 10;
    private final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bdd = BDD.getInstance(getApplicationContext());

        setContentView(R.layout.activity_main);

        // pickImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView calcul = findViewById(R.id.calcul);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String texte = sharedPref.getString(getString(R.string.calcul_id), calcul.getText().toString());

        calcul.setText(texte);
        resultatPartiel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            calcul.setShowSoftInputOnFocus(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TextView calcul = findViewById(R.id.calcul);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.calcul_id), calcul.getText().toString());
        editor.apply();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            EditText editText = (EditText)calcul;
            editText.setSelection(calcul.getText().length());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView calcul = findViewById(R.id.calcul);
        outState.putString(getString(R.string.calcul_id), calcul.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView calcul = findViewById(R.id.calcul);
        String valeur = savedInstanceState.getString(getString(R.string.calcul_id), "");

        calcul.setText(valeur);
        resultatPartiel();
    }

    public void inserer(View view) {
        TextView calcul = findViewById(R.id.calcul);
        TextView character = (TextView)view;

        String texte = calcul.getText().toString();
        int positionDebut = 0;
        String texteDebut;
        String texteFin;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            positionDebut = calcul.getSelectionStart();
            int positionFin = calcul.getSelectionEnd();

            String caractere = character.getText().toString();
            texteDebut = texte.substring(0, positionDebut);
            texteFin = texte.substring(positionFin);

            texteDebut += caractere;
            texte = texteDebut + texteFin;
        }
        else
        {
            texte += character.getText().toString();
            texteDebut = texte;
            texteFin = "";
        }

        int nbParentheseOuvrante = 0;
        int nbParentheseFermante = 0;

        for(int i = 0; i < texte.length(); i++)
        {
            switch (texte.charAt(i))
            {
                case '(':
                    nbParentheseOuvrante++;
                    break;

                case ')':
                    nbParentheseFermante++;
            }
        }

        if(texteDebut.length() >= 3 && texteDebut.substring(texteDebut.length() - 3).matches("^[\\d)]([+−]{2}|[×÷]{2})$"))
        {

            texteDebut = texteDebut.substring(0, texteDebut.length() - 2) + texteDebut.charAt(texteDebut.length() - 1);
            texte = texteDebut + texteFin;
        }
        else
        {
            positionDebut += character.getText().length();
        }

        if(texte.matches("^(−?\\(+)*−?(\\d*|\\d+(,\\d*)?)\\)*(\\d\\)*(([+−]|[×÷]−?)(\\(−?)*(\\d*|\\d+(,\\d*)?))?)*$") && nbParentheseOuvrante >= nbParentheseFermante)
        {
            calcul.setText(texte);


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                EditText editText = (EditText)calcul;
                editText.setSelection(positionDebut);
            }
        }

        resultatPartiel();
    }

    public void historique(View view)
    {
        Intent intent = new Intent(this, Historique.class);
        startActivity(intent);
    }

    public void vider(View view) {
        TextView calcul = findViewById(R.id.calcul);
        calcul.setText("");

        resultatPartiel();
    }

    public void effacer(View view)
    {
        TextView calcul = findViewById(R.id.calcul);
        String texte = calcul.getText().toString();
        int positionDebut = 0;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            positionDebut = calcul.getSelectionStart();
            int positionFin = calcul.getSelectionEnd();

            if(positionDebut == positionFin)
            {
                positionDebut = Math.max(positionDebut - 1, 0);
            }

            String texteDebut = texte.substring(0, positionDebut);
            String texteFin = texte.substring(positionFin);

            texte = texteDebut + texteFin;
        }
        else
        {
            texte = texte.substring(0, Math.max(texte.length() - 1, 0));
        }


        calcul.setText(texte);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            EditText editText = (EditText)calcul;
            editText.setSelection(positionDebut);
        }

        resultatPartiel();
    }

    public void resolution(View view)
    {
        TextView resultat = findViewById(R.id.resultat);
        TextView calcul = findViewById(R.id.calcul);
        String texteCalcul = calcul.getText().toString();

        resultatPartiel();

        String texteResultat = resultat.getText().toString();

        if(texteCalcul.matches("^.*\\d.*[+−×÷].*\\d.*$") && texteResultat.matches("^−?\\d+(,\\d*)?$")) {
            int nbParentheseOuvrante = 0;
            int nbParentheseFermante = 0;
            StringBuilder texteCalculBuilder;
            final ItemHistorique itemHistorique;

            for(int i = 0; i < texteCalcul.length(); i++)
            {
                switch (texteCalcul.charAt(i))
                {
                    case '(':
                        nbParentheseOuvrante++;
                        break;

                    case ')':
                        nbParentheseFermante++;
                }
            }

            while(texteCalcul.matches("^.*[^\\d)]$"))
            {
                texteCalcul = texteCalcul.substring(0, texteCalcul.length() - 1);
            }

            texteCalculBuilder = new StringBuilder(texteCalcul);

            for(int i = nbParentheseFermante; i < nbParentheseOuvrante; i++)
            {
                texteCalculBuilder.append(')');
            }

            itemHistorique = new ItemHistorique();
            itemHistorique.calcul = texteCalculBuilder.toString();
            itemHistorique.resultat = texteResultat;

            Executors.newSingleThreadExecutor().execute(() -> bdd.itemHistoriqueDao().insert(itemHistorique));

            calcul.setText(texteResultat);
            resultat.setText("");
        }
    }

    public void resultatPartiel()
    {
        TextView resultat = findViewById(R.id.resultat);
        TextView calcul = findViewById(R.id.calcul);
        String texte = calcul.getText().toString().replace(',', '.');

        if(texte.length() > 0 && texte.matches("^.*\\d.*[+−×÷].*$"))
        {
            try
            {
                while(texte.matches("^.*[^\\d)]$"))
                {
                    texte = texte.substring(0, texte.length() - 1);
                }

                double valeur = Calcul.solution(texte);

                if(Double.isNaN(valeur))
                {
                    texte = "Indéfinie";
                }
                else if(Double.isInfinite(valeur))
                {
                    texte = "Infinie";
                }
                else if(Math.round(valeur) == valeur)
                {
                    texte = Long.toString(Math.round(valeur));
                }
                else
                {
                    texte = Double.toString(valeur).replace('.', ',');
                }
            }
            catch (ArithmeticException e)
            {
                texte = "Impossible";
            }
        }
        else
        {
            texte = "";
        }

        resultat.setText(texte);
    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI
            );
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
        } else {
            String[] permissions;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            if(data != null) {
                Uri uri = data.getData();

                if (uri != null) {
                    try {
                        FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(this, uri);

                        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                                .getOnDeviceTextRecognizer();

                        detector.processImage(image)
                                        .addOnSuccessListener(firebaseVisionText -> {
                                            // Task completed successfully
                                            // ...
                                            Log.i("Resulat image", firebaseVisionText.getText());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
