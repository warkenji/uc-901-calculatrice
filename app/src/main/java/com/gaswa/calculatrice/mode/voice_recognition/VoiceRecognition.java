package com.gaswa.calculatrice.mode.voice_recognition;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.widget.TextView;

import com.gaswa.calculatrice.MainActivity;
import com.gaswa.calculatrice.R;
import com.gaswa.calculatrice.mode.Recognition;

import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class VoiceRecognition extends Recognition {
    private final int VOICE_REQUEST_CODE = 20;
    private MainActivity activity;

    public VoiceRecognition(MainActivity activity)
    {
        this.activity = activity;
    }

    public void pick() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Veuillez indiquez le calcul à effectuer.");
        try {
            activity.startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (ActivityNotFoundException ignored) {
            ignored.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VOICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                TextView calcul = activity.findViewById(R.id.calcul);
                List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                if(result.size() > 0) {
                    String texte = activity.conversion(result.get(0));
                    boolean verif = texte.length() > 0 && texte.charAt(texte.length() - 1) == '=';
                    if(verif)
                    {
                        texte = texte.substring(0, texte.length() - 1);
                    }

                    if(activity.verification(texte))
                    {
                        calcul.setText(texte);

                        if(verif)
                        {
                            activity.resolution(null);
                        }
                        else
                        {
                            activity.resultatPartiel();
                        }
                    }

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(activity.getString(R.string.calcul_id), calcul.getText().toString());

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        editor.putInt(activity.getString(R.string.selection_start_id), calcul.getSelectionStart());
                        editor.putInt(activity.getString(R.string.selection_end_id), calcul.getSelectionEnd());
                    }

                    editor.apply();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {

    }
}
