package com.gaswa.calculatrice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.TextView;

import com.gaswa.calculatrice.donnee.BDD;
import com.gaswa.calculatrice.donnee.ItemHistorique;
import com.gaswa.calculatrice.mode.handwritting_recognition.HandWrittingRecognition;
import com.gaswa.calculatrice.mode.image_recognition.ImageRecognition;
import com.gaswa.calculatrice.mode.voice_recognition.VoiceRecognition;

import java.util.concurrent.Executors;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BDD bdd;
    ImageRecognition imageRecognition;
    VoiceRecognition voiceRecognition;
    @LayoutRes int layoutActuel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bdd = BDD.getInstance(getApplicationContext());

        layoutActuel = R.layout.mode_basic;
        changementVue(layoutActuel);

        imageRecognition = new ImageRecognition(this);
        voiceRecognition = new VoiceRecognition(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TextView calcul = findViewById(R.id.calcul);
            calcul.setShowSoftInputOnFocus(false);
        }
    }

    public View changementVue(@LayoutRes int layout)
    {
        setContentView(R.layout.activity_main);
        ViewStub stub = findViewById(R.id.mode_principal);
        stub.setLayoutResource(layout);

        return stub.inflate();
    }

    public void handWrittingPrevious(View view)
    {
        HandWrittingRecognition handWrittingRecognition = findViewById(R.id.handwritting_view);

        if(handWrittingRecognition != null)
        {
            handWrittingRecognition.previous();
        }
    }

    public void handWrittingForward(View view)
    {
        HandWrittingRecognition handWrittingRecognition = findViewById(R.id.handwritting_view);

        if(handWrittingRecognition != null)
        {
            handWrittingRecognition.forward();
        }
    }

    public void handWrittingMode(View view)
    {
        layoutActuel = R.layout.mode_handwritting;
        changementVue(layoutActuel);

        HandWrittingRecognition handWrittingRecognition = findViewById(R.id.handwritting_view);
        handWrittingRecognition.setResultatListener(texte -> {
            TextView calcul = findViewById(R.id.calcul);
            TextView resultat = findViewById(R.id.resultat);

            boolean verif = texte.length() > 0 && texte.charAt(texte.length() - 1) == '=';
            if(verif)
            {
                texte = texte.substring(0, texte.length() - 1);
            }

            if(MainActivity.this.verification(texte))
            {
                calcul.setText(texte);

                if(verif)
                {
                    MainActivity.this.resolution(MainActivity.this.findViewById(R.id.resolution));
                }
                else
                {
                    MainActivity.this.resultatPartiel();
                }
            }
            else
            {
                calcul.setText("");
                resultat.setText("");
            }
        });
    }

    public void normalMode(View view)
    {
        layoutActuel = R.layout.mode_basic;
        changementVue(layoutActuel);
    }

    public void handWrittingNormal(View view)
    {
        HandWrittingRecognition handWrittingRecognition = findViewById(R.id.handwritting_view);

        if(handWrittingRecognition != null)
        {
            handWrittingRecognition.normalMode();
        }
    }

    public void handWrittingDelete(View view)
    {
        HandWrittingRecognition handWrittingRecognition = findViewById(R.id.handwritting_view);

        if(handWrittingRecognition != null)
        {
            handWrittingRecognition.deleteMode();
        }
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
            EditText editText = (EditText)calcul;
            calcul.setShowSoftInputOnFocus(false);
            int selectionStart = sharedPref.getInt(getString(R.string.selection_start_id), calcul.length());
            int selectionEnd = sharedPref.getInt(getString(R.string.selection_end_id), calcul.length());

            if(selectionStart == selectionEnd)
            {
                editText.setSelection(selectionStart);
            }
            else
            {
                editText.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        TextView calcul = findViewById(R.id.calcul);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.calcul_id), calcul.getText().toString());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            editor.putInt(getString(R.string.selection_start_id), calcul.getSelectionStart());
            editor.putInt(getString(R.string.selection_end_id), calcul.getSelectionEnd());
        }

        editor.apply();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView calcul = findViewById(R.id.calcul);
        outState.putString(getString(R.string.calcul_id), calcul.getText().toString());
        outState.putInt(getString(R.string.layout_actuel_id), layoutActuel);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            outState.putInt(getString(R.string.selection_start_id), calcul.getSelectionStart());
            outState.putInt(getString(R.string.selection_end_id), calcul.getSelectionEnd());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TextView calcul = findViewById(R.id.calcul);
        String valeur = savedInstanceState.getString(getString(R.string.calcul_id), "");
        layoutActuel = savedInstanceState.getInt(getString(R.string.layout_actuel_id), layoutActuel);

        changementVue(layoutActuel);

        calcul.setText(valeur);
        resultatPartiel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            EditText editText = (EditText)calcul;
            int selectionStart = savedInstanceState.getInt(getString(R.string.selection_start_id), calcul.length());
            int selectionEnd = savedInstanceState.getInt(getString(R.string.selection_end_id), calcul.length());

            if(selectionStart == selectionEnd)
            {
                editText.setSelection(selectionStart);
            }
            else
            {
                editText.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageRecognition.onActivityResult(requestCode, resultCode, data);
        voiceRecognition.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        imageRecognition.onRequestPermissionsResult(requestCode, grantResults);
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

        if(texteDebut.length() >= 3 && texteDebut.substring(texteDebut.length() - 3).matches("^[\\d)]([+−]{2}|[×÷]{2})$"))
        {

            texteDebut = texteDebut.substring(0, texteDebut.length() - 2) + texteDebut.charAt(texteDebut.length() - 1);
            texte = texteDebut + texteFin;
        }
        else
        {
            positionDebut += character.getText().length();
        }

        if(verification(texte))
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

    public boolean verification(String texte)
    {

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

        return texte.matches("^(−?\\(+)*−?(\\d*|\\d+(,\\d*)?)((?<!\\))\\d\\)?(([+−]|[×÷]−?)(\\(−?)*(\\d*|\\d+(,\\d*)?))?)*$") && nbParentheseOuvrante >= nbParentheseFermante;
    }

    public void camera(View view)
    {
        normalMode(null);
        imageRecognition.pick();
    }

    public void voice(View view)
    {
        normalMode(null);
        voiceRecognition.pick();
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
            EditText editText = (EditText)calcul;
            editText.setSelection(calcul.length());
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
}
