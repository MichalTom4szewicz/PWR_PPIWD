package com.mbientlab.metawear.tutorial.starter;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegistrationActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText firstName;
    EditText lastName;
    Button register;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        firstName = findViewById(R.id.nameInput);
        lastName = findViewById(R.id.surnameInput);
        register = findViewById(R.id.registerButton);
        cancel = findViewById(R.id.cancelButton);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEnteredData()){
                    RegistrationParams registrationParams = new RegistrationParams(email,password, firstName, lastName);
                    new RegisterUser().execute(registrationParams);
                    finish();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    boolean checkEnteredData(){
        if (!isEmail(email)) {
            email.setError("Enter valid email!");
            showToast("You must enter correct email to register!");
            return false;
        }
        if (isEmpty(password)) {
            password.setError("Password is required!");
            showToast("You must enter password to register!");
            return false;
        }
        if (isEmpty(firstName)) {
            firstName.setError("Name is required!");
            showToast("You must enter name to register!");
            return false;
        }

        if (isEmpty(lastName)) {
            lastName.setError("Last name is required!");
            showToast("You must enter surname to register!");
            return false;
        }
        return true;

    }
    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
    void showToast(String toastText){
        Toast t = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
        t.show();
    }
}

class RegisterUser extends AsyncTask<RegistrationParams, Void, Void> {

    @Override
    protected Void doInBackground(RegistrationParams... registrationParams) {
        String path = "http://ppiwd.arturb.xyz:5000/auth/register" ;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject cred = new JSONObject();
            cred.put("email",registrationParams[0].email);
            cred.put("password",registrationParams[0].password);
            cred.put("firstName",registrationParams[0].firstName);
            cred.put("lastName",registrationParams[0].lastName);

            DataOutputStream localDataOutputStream = new DataOutputStream(connection.getOutputStream());
            localDataOutputStream.writeBytes(cred.toString());
            localDataOutputStream.flush();
            localDataOutputStream.close();

            Log.i("Registration status", String.valueOf(connection.getResponseCode()));
            Log.i("Registration message", connection.getRequestMethod());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}

class RegistrationParams {
    String email;
    String password;
    String firstName;
    String lastName;


    RegistrationParams(EditText email,EditText password,EditText firstName,EditText lastName) {
        this.email = email.toString();
        this.password = password.toString();
        this.firstName = firstName.toString();
        this.lastName = lastName.toString();
    }
}