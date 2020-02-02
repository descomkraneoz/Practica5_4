package net.iesseveroochoa.manuelmartinez.practica5_2.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.iesseveroochoa.manuelmartinez.practica5_2.R;

public class PreferenciasUsuarioActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.opciones);
    }
}
