package com.gaswa.calculatrice;

public class Calcul {
    private static class Resultat
    {
        private int indice;
        private double valeur;

        Resultat()
        {
            this(0, 0);
        }
        
        Resultat(int indice, double valeur)
        {
            this.indice = indice;
            this.valeur = valeur;
        }
    }

    public static double solution(String equation)
    {
       return calculPrioritaire(equation).valeur;
    }

    private static Resultat calculPrioritaire(String equation)
    {
        int offset = 0;
        double valeur = 0;
        String equationTemporaire = equation;
        Resultat resultat = new Resultat();

        while (equationTemporaire.length() > 0 && equationTemporaire.charAt(0) != ')')
        {
            resultat = addition(equationTemporaire);

            equationTemporaire = equationTemporaire.substring(resultat.indice);
            offset += resultat.indice;
            valeur += resultat.valeur;
        }

        if(equationTemporaire.length() > 0 && equationTemporaire.charAt(0) == ')')
        {
            offset++;
        }

        resultat.indice = offset;
        resultat.valeur = valeur;

        return resultat;
    }

    private static Resultat addition(String equation)
    {
        Resultat resultat;
        int offset = 0;
        String equationTemporaire = equation;

        if(equationTemporaire.length() > 0) {
            if (equationTemporaire.charAt(0) == '+') {
                equationTemporaire = equationTemporaire.substring(1);
                offset++;
            }
        }

        if(equationTemporaire.length() > 0) {
            if (equationTemporaire.charAt(0) == '−') {
                resultat = addition(equation.substring(1));
                resultat.valeur = -resultat.valeur;
                offset++;
            } else if (equationTemporaire.charAt(0) == '(') {
                resultat = calculPrioritaire(equationTemporaire.substring(1));
                offset++;
            } else {
                resultat = valeur(equationTemporaire);
            }
        }
        else
        {
            resultat = new Resultat();
        }

        resultat.indice += offset;

        return priorite(equation.substring(resultat.indice), resultat);
    }

    private static Resultat priorite(String equation, Resultat resultat)
    {
        Resultat resultat2;

        if(equation.length() > 0) {
            switch (equation.charAt(0)) {
                case '×':
                    resultat2 = addition(equation.substring(1));
                    resultat.indice += resultat2.indice + 1;

                    if(resultat2.indice > 0) {
                        resultat.valeur *= resultat2.valeur;
                    }
                    break;

                case '÷':
                    resultat2 = addition(equation.substring(1));
                    resultat.indice += resultat2.indice + 1;

                    if(resultat2.indice > 0) {
                        resultat.valeur /= resultat2.valeur;
                    }
                    break;
            }
        }

        return resultat;
    }

    private static Resultat valeur(String equation)
    {
        Resultat resultat = new Resultat();

        while (resultat.indice < equation.length() && equation.substring(0, resultat.indice + 1).matches("^([0-9]+(\\.[0-9]*)?)?$")) {
            resultat.indice++;
        }

        if(resultat.indice > 0) {
            String temp = equation.substring(0, resultat.indice);

            if (temp.charAt(temp.length() - 1) == '.') {
                temp += '0';
            }

            resultat.valeur = Double.parseDouble(temp);
        }

        return resultat;
    }
}
