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
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PleaseCall extends Activity {
    private static final String LOG_TAG = "PleaseCall";
    private static final int CONTACT_PICKER_RESULT = 1001;
    private static String providerPrefix;
    private EditText editText;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        editText = (EditText) findViewById(R.id.number);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    send();
                    handled = true;
                }
                return handled;
            }
        });

        Intent intent = getIntent();
        if (intent.getAction().equals("android.intent.action.CALL_PRIVILEGED")) {
            Log.d(LOG_TAG, "intent is CALL_PRIVILEGED");
            editText.setText(intent.getData().getSchemeSpecificPart());
        }

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        providerPrefix = settings.getString("providerPrefix", null);
        Log.v(LOG_TAG, "prefix is " + providerPrefix);
        if (providerPrefix == null) {
            // prompt to choose one
            // whatever, let's default to MTN
            providerPrefix = "121";
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("providerPrefix", providerPrefix);
            editor.commit();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    public void selectProvider(MenuItem menuItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.options_select_provider);
        builder.setItems(R.array.provider_names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TypedArray providerPrefixes = getResources().obtainTypedArray(R.array.provider_prefixes);
                providerPrefix = providerPrefixes.getString(which);
                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                editor.putString("providerPrefix", providerPrefix);
                editor.commit();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void selectContact(View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI),
                CONTACT_PICKER_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // based on http://stackoverflow.com/a/6155690
        if (resultCode == RESULT_OK) {
            if (requestCode == CONTACT_PICKER_RESULT) {
                Cursor cursor = null;
                String phoneNumber = "";
                List<String> allNumbers = new ArrayList<String>();
                int phoneIdx = 0;
                try {
                    Uri result = data.getData();
                    String id = result.getLastPathSegment();
                    cursor = getContentResolver().query(Phone.CONTENT_URI, null, Phone.CONTACT_ID + "=?", new String[] { id }, null);
                    phoneIdx = cursor.getColumnIndex(Phone.DATA);
                    if (cursor.moveToFirst()) {
                        while (cursor.isAfterLast() == false) {
                            phoneNumber = cursor.getString(phoneIdx);
                            allNumbers.add(phoneNumber);
                            cursor.moveToNext();
                        }
                    } else {
                        //no results actions
                    }
                } catch (Exception e) {
                    //error actions
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }

                    final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
                    AlertDialog.Builder builder = new AlertDialog.Builder(PleaseCall.this);
                    builder.setTitle("Choose a number");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String selectedNumber = items[item].toString();
                            selectedNumber = selectedNumber.replace("-", "");
                            editText.setText(selectedNumber);
                        }
                    });
                    AlertDialog alert = builder.create();
                    if(allNumbers.size() > 1) {
                        alert.show();
                    } else {
                        String selectedNumber = phoneNumber.toString();
                        selectedNumber = selectedNumber.replace("-", "");
                        editText.setText(selectedNumber);
                    }

                    if (phoneNumber.length() == 0) {
                        //no numbers found actions
                    }
                }
            }
        } else {
            //not ok actions
        }
    }

    public void send(String number) {
        try {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:*" + providerPrefix + "*" + scrubbed(number) + Uri.encode("#"))));
        } catch (ActivityNotFoundException activityException) {
        }
    }

    public void send() {
        send(editText.getText().toString());
    }

    public void send(View view) {
        send();
    }

    public static String scrubbed(String number) {
        number = number.replaceAll("^\\+27", "0");  // replace country code
        number = number.replaceAll("[^0-9]", "");  // remove non-digits
        return number;
    }
}
