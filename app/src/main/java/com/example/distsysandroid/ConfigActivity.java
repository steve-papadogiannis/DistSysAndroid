package com.example.distsysandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.sys.dist.distsysandroid.R;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends FragmentActivity {

    private EditText inputIp, inputPort;
    private TextInputLayout inputLayoutIp, inputLayoutPort;
    private String ip, port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        inputLayoutIp = (TextInputLayout) findViewById(R.id.input_layout_ip);
        inputLayoutPort = (TextInputLayout) findViewById(R.id.input_layout_port);
        inputIp = (EditText) findViewById(R.id.input_ip);
        inputPort = (EditText) findViewById(R.id.input_port);
        Button btnConnect = (Button) findViewById(R.id.btn_connect);

        inputIp.addTextChangedListener(new MyTextWatcher(inputIp));
        inputPort.addTextChangedListener(new MyTextWatcher(inputPort));

        btnConnect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                submitForm();
            }

        });
    }

    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateIp()) {
            return;
        }

        if (!validatePort()) {
            return;
        }

        final Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("ip", ip);
        intent.putExtra("port", port);
        startActivity(intent);
    }

    private boolean validateIp() {
        ip = inputIp.getText().toString().trim();
        if (ip.isEmpty() || !isValidIp(ip)) {
            inputLayoutIp.setError(getString(R.string.err_ip));
            requestFocus(inputIp);
            return false;
        } else {
            inputLayoutIp.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePort() {
        port = inputPort.getText().toString().trim();

        if (port.isEmpty()) {
            inputLayoutPort.setError(getString(R.string.err_port));
            requestFocus(inputPort);
            return false;
        } else {
            try {
                final Long longPort = Long.parseLong(port);
            } catch (NumberFormatException e) {
                inputLayoutPort.setError(getString(R.string.err_port));
                requestFocus(inputPort);
                return false;
            }
            inputLayoutPort.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidIp(String ip) {
        return  Patterns.IP_ADDRESS.matcher(ip).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_ip:
                    validateIp();
                    break;
                case R.id.input_port:
                    validatePort();
                    break;
            }
        }
    }
}
