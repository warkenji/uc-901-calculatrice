package com.gaswa.calculatrice;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
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

        if(texte.length() >= 3 && texte.substring(texte.length() - 3).matches("^[0-9)]([+−]{2}|[×÷]{2})$"))
        {
            texte = texte.substring(0, texte.length() - 2) + texte.charAt(texte.length() - 1);
        }

        if(texte.matches("^(−?\\(+)*−?([0-9]*|[0-9]+(,[0-9]*)?)\\)*([0-9]\\)*(([+−]|[×÷]−?)(\\(−?)*([0-9]*|[0-9]+(,[0-9]*)?))?)*$") && nbParentheseOuvrante >= nbParentheseFermante)
        {
            calcul.setText(texte);
        }

        resultatPartiel();
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

        resultat();

        String texte = resultat.getText().toString();

        if(texte.matches("^−?[0-9]+(,[0-9]*)?$")) {
            calcul.setText(texte);
            resultat.setText("");
        }
    }

    public void resultatPartiel()
    {
        TextView resultat = findViewById(R.id.resultat);
        TextView calcul = findViewById(R.id.calcul);
        String texte = calcul.getText().toString();
        if(texte.matches("^−?(\\(|[0-9]+(,[0-9]*)?[+−×÷]).*$"))
        {
            resultat();
        }
        else
        {
            resultat.setText("");
        }
    }

    public void resultat()
    {
        TextView resultat = findViewById(R.id.resultat);
        TextView calcul = findViewById(R.id.calcul);
        String texte = calcul.getText().toString().replace(',', '.');

        if(texte.length() > 0)
        {
            try
            {
                while(texte.matches("^.*[^0-9]$"))
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

        resultat.setText(texte);
    }
}
