package za.co.kierendavies.pleasecall;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class PleaseCallSettings extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        if (Build.VERSION.SDK_INT >= 11) {
            addResource();
        } else {
            addResourceOldApi();
        }
    }

    @SuppressLint("NewApi")
    protected void addResourceOldApi() {
        addPreferencesFromResource(R.xml.settings);
    }

    @TargetApi(11)
    protected void addResource() {
        PreferenceFragment pf = new PreferenceFragment() {
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.settings);
            }
        };
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, pf)
                .commit();
    }
}