package com.gaswa.calculatrice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gaswa.calculatrice.donnee.BDD;
import com.gaswa.calculatrice.donnee.ItemHistorique;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;

public class Historique extends AppCompatActivity {
    private BDD bdd;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historique);

        bdd = BDD.getInstance(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String calcul_id = getString(R.string.calcul_id);
        String resultat_id = getString(R.string.resultat_id);
        String[] from = {calcul_id, resultat_id};
        int[] to = {R.id.calcul, R.id.resultat};
        final LinkedList<Map<String, String>> liste = new LinkedList<>();
        final ListView listeVue = findViewById(R.id.historique);

        adapter = new SimpleAdapter(this, liste, R.layout.item_historique, from, to);

        LiveData<List<ItemHistorique>> historiqueListener =  bdd.itemHistoriqueDao().getAll();
        historiqueListener.observe(this, historique -> {
            if(historique != null) {
                liste.clear();

                for (int i = historique.size() - 1; i >= 0; i--) {
                    ItemHistorique itemHistorique = historique.get(i);
                    Map<String, String> item = new HashMap<>();
                    item.put(calcul_id, itemHistorique.calcul);
                    item.put(resultat_id, itemHistorique.resultat);
                    liste.add(item);
                }

                listeVue.setOnItemClickListener((parent, view, position, id) -> {
                    ViewGroup viewGroup = (ViewGroup)view;
                    TextView calcul = viewGroup.findViewById(R.id.calcul);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.calcul_id), calcul.getText().toString());
                    editor.apply();

                    finish();
                });

                adapter.notifyDataSetChanged();
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
                Executors.newSingleThreadExecutor().execute(() -> bdd.itemHistoriqueDao().deleteAll());

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
