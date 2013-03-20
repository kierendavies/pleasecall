package za.co.kierendavies.pleasecall;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class PleaseCall extends Activity {
    private static String providerPrefix;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        providerPrefix = sharedPref.getString("providerPrefix", "");
        if (providerPrefix.equals("")) {
            String[] mncs = getResources().getStringArray(R.array.provider_mnc);
            String[] prefixes = getResources().getStringArray(R.array.provider_prefixes);
            String mnc = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator().substring(3);
            //TODO check mcc is 655
            int i;
            for (i = 0; i < mncs.length && !mnc.equals(mncs[i]); ++i);
            if (i < mncs.length) {
                providerPrefix = prefixes[i];
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("providerPrefix", providerPrefix);
                editor.commit();
            } else {
                startActivity(new Intent(this, PleaseCallSettings.class));
            }
        }

        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
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