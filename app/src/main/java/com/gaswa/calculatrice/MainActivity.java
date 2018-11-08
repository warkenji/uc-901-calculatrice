package com.gaswa.calculatrice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gaswa.calculatrice.donnee.BDD;
import com.gaswa.calculatrice.donnee.ItemHistorique;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private BDD bdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bdd = BDD.getInstance(getApplicationContext());

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView calcul = findViewById(R.id.calcul);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String texte = sharedPref.getString(getString(R.string.calcul_id), calcul.getText().toString());

        calcul.setText(texte);
        resultatPartiel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        TextView calcul = findViewById(R.id.calcul);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.calcul_id), calcul.getText().toString());
        editor.apply();
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
        String texte = calcul.getText().toString() + character.getText().toString();
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

        if(texte.length() >= 3 && texte.substring(texte.length() - 3).matches("^[\\d)]([+−]{2}|[×÷]{2})$"))
        {
            texte = texte.substring(0, texte.length() - 2) + texte.charAt(texte.length() - 1);
        }

        if(texte.matches("^(−?\\(+)*−?(\\d*|\\d+(,\\d*)?)\\)*(\\d\\)*(([+−]|[×÷]−?)(\\(−?)*(\\d*|\\d+(,\\d*)?))?)*$") && nbParentheseOuvrante >= nbParentheseFermante)
        {
            calcul.setText(texte);
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

        if(texte.length() > 0) {
            calcul.setText(texte.substring(0, texte.length() - 1));
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
}
