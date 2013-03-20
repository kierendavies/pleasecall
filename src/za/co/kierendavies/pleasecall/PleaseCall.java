package za.co.kierendavies.pleasecall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PleaseCall extends Activity {
    private static final String LOG_TAG = "PleaseCall";
    private static String providerPrefix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        providerPrefix = sharedPref.getString("providerPrefix", null);
        Log.v(LOG_TAG, "prefix is " + providerPrefix);
        if (providerPrefix == null) {
            startActivity(new Intent(this, PleaseCallSettings.class));
        }

        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
            Log.d(LOG_TAG, "intent is CALL_PRIVILEGED");
            Toast.makeText(getApplicationContext(), R.string.sending, Toast.LENGTH_SHORT).show();
            send(intent.getData().getSchemeSpecificPart());
            finish();
        }
    }

    public void send(String number) {
        number = number.replaceAll("^\\+27", "0");  // replace country code
        number = number.replaceAll("[^0-9]", "");  // remove non-digits
        try {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:*" + providerPrefix + "*" + number + Uri.encode("#"))));
        } catch (ActivityNotFoundException activityException) {
        }
    }
}
