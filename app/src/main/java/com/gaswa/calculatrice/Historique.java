package com.gaswa.calculatrice;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gaswa.calculatrice.donnee.BDD;
import com.gaswa.calculatrice.donnee.ItemHistorique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Historique extends AppCompatActivity implements Runnable {
    private static final String CALCUL_ID = "calcul";
    private static final String RESULTAT_ID = "resultat";
    private BDD bdd;
    private List<Map<String, String>> liste;
    private SimpleAdapter adapter;
    private Executeur executeur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historique);

        executeur = new Executeur();
        bdd = BDD.getInstance(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar actionBar = getSupportActionBar();

        // Enable the Up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        executeur.execute(this);
    }

    @Override
    public void run() {
        String[] from = {CALCUL_ID, RESULTAT_ID};
        int[] to = {R.id.calcul, R.id.resultat};

        List<ItemHistorique> historique =  bdd.itemHistoriqueDao().getAll();
        liste = new ArrayList<>(historique.size());

        for(int i = historique.size() - 1; i >= 0; i--)
        {
            ItemHistorique itemHistorique = historique.get(i);
            Map<String, String> item = new HashMap<>();
            item.put(CALCUL_ID, itemHistorique.calcul);
            item.put(RESULTAT_ID, itemHistorique.resultat);
            liste.add(item);
        }

        adapter = new SimpleAdapter(this, liste, R.layout.item_historique, from, to);
        final ListView listeVue = findViewById(R.id.historique);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listeVue.setAdapter(adapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.historique, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.vider:
                liste.clear();
                adapter.notifyDataSetChanged();

                executeur.execute(new Runnable() {
                    @Override
                    public void run() {
                        bdd.itemHistoriqueDao().deleteAll();
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
