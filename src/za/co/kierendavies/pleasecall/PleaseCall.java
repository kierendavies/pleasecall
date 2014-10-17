package za.co.kierendavies.pleasecall;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
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

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // get saved provider prefix, or request it to be set
        providerPrefix = sharedPref.getString("providerPrefix", "");
        if (providerPrefix.equals("")) {
            String[] mncs = getResources().getStringArray(R.array.provider_mnc);
            String[] prefixes = getResources().getStringArray(R.array.provider_prefixes);
            String mnc = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getSimOperator().substring(3);
            // TODO check mcc is 655 (South Africa)
            int i = 0;
            while (i < mncs.length && !mnc.equals(mncs[i])) ++i;
            if (i < mncs.length) {
                providerPrefix = prefixes[i];
                sharedPref.edit()
                        .putString("providerPrefix", providerPrefix)
                        .commit();
            } else {
                startActivity(new Intent(this, PleaseCallSettings.class));
            }
        }

        // get and increment number of times launched
        int timesLaunched = sharedPref.getInt("timesLaunched", 0);
        if (timesLaunched != -1) {
            ++timesLaunched;
            sharedPref.edit()
                    .putInt("timesLaunched", timesLaunched)
                    .commit();
        }

        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
            Toast.makeText(getApplicationContext(), R.string.sending, Toast.LENGTH_SHORT).show();
            send(intent.getData().getSchemeSpecificPart());

            // check number of times launched, show rating dialog
            if (timesLaunched >= 5) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.rate_title)
                        .setMessage(R.string.rate_message)
                        .setPositiveButton(R.string.rate_yes,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sharedPref.edit()
                                                .putInt("timesLaunched", -1)
                                                .commit();
                                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                Uri.parse("market://details?id=" + getPackageName())));
                                    }
                                })
                        .setNeutralButton(R.string.rate_later,  // ask next time
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        return;
                                    }
                                })
                        .setNegativeButton(R.string.rate_no,  // don't ask again
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sharedPref.edit()
                                                .putInt("timesLaunched", -1)
                                                .commit();
                                    }
                                })
                        .create()
                        .show();
            }
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