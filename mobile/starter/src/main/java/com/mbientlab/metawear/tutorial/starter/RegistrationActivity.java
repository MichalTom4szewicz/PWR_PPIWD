package com.mbientlab.metawear.tutorial.starter;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class RegistrationActivity extends AppCompatActivity {
    EditText email;
    EditText password;
    EditText name;
    EditText surname;
    Button register;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        name = findViewById(R.id.nameInput);
        surname = findViewById(R.id.surnameInput);
        register = findViewById(R.id.registerButton);
        cancel = findViewById(R.id.cancelButton);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEnteredData()){
                    RegistrationParams registrationParams = new RegistrationParams(email,password, name, surname);
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
        if (isEmpty(name)) {
            name.setError("Name is required!");
            showToast("You must enter name to register!");
            return false;
        }

        if (isEmpty(surname)) {
            surname.setError("Last name is required!");
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
    protected Void doInBackground(RegistrationParams ...registrationParams) {
        String path = "http://ppiwd.arturb.xyz:5000/auth/register" ;

        String parameterName = "";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";


        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;
        return null;
    }
}

class RegistrationParams {
    String email;
    String password;
    String name;
    String surname;


    RegistrationParams(EditText email,EditText password,EditText name,EditText surname) {
        this.email = email.toString();
        this.password = password.toString();
        this.name = name.toString();
        this.surname = surname.toString();
    }
}