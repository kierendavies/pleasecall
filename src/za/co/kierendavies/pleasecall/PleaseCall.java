package za.co.kierendavies.pleasecall;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class PleaseCall extends Activity {
    TextView title;
    EditText editText;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        title = (TextView) findViewById(R.id.title);
        editText = (EditText) findViewById(R.id.number);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    send(null);
                    handled = true;
                }
                return handled;
            }
        });
    }

    public void selectContact(View view) {

    }

    public void send(View view) {
        title.setText(editText.getText());
    }
}
