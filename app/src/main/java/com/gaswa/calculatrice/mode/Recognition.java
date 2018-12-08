package com.gaswa.calculatrice.mode;

public abstract class Recognition {
    protected String conversion(String texte)
    {
        texte = texte.replaceAll("parenthèses? ouvrantes?", "(");
        texte = texte.replaceAll("parenthèses? ferm[ea]ntes?", ")");
        texte = texte.replaceAll("[\\s\\n\\t\\r]+", "");
        texte = texte.replace('x', '×');
        texte = texte.replace('X', '×');
        texte = texte.replace('*', '×');
        texte = texte.replace('-', '−');
        texte = texte.replace('/', '÷');
        return texte;
    }

    public abstract void pick();
}
