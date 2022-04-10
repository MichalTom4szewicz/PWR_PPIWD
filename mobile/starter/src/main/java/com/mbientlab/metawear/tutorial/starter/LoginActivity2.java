package com.mbientlab.metawear.tutorial.starter;

        import android.content.Context;
        import android.content.Intent;
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
        import java.lang.ref.WeakReference;
        import java.net.HttpURLConnection;
        import java.net.URL;

public class LoginActivity2 extends AppCompatActivity {
    EditText email;
    EditText password;
    Button login;
    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        signUp = findViewById(R.id.signup_button);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkEnteredData()){
                    LoginParams loginParams = new LoginParams(email ,password);
                    new LoginUser(v.getContext()).execute(loginParams);
//                    finish();
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                Intent intent = new Intent(v.getContext(), RegistrationActivity.class);
                startActivity(intent);
            }
        });
    }

    boolean checkEnteredData(){
        if (!isEmail(email)) {
            email.setError("Enter valid email!");
            showToast("You must enter correct email to login!");
            return false;
        }
        if (isEmpty(password)) {
            password.setError("Password is required!");
            showToast("You must enter password to login!");
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

class LoginUser extends AsyncTask<LoginParams, Void, String> {
   Context contextRef;

    public LoginUser(Context context) {
        contextRef = context;
    }

    @Override
    protected String doInBackground(LoginParams... loginParams) {
        String path = "http://ppiwd.arturb.xyz:5000/auth/login" ;

        HttpURLConnection connection = null;

        try {
            URL url = new URL(path);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            JSONObject cred = new JSONObject();
            cred.put("email", loginParams[0].email);
            cred.put("password", loginParams[0].password);
//            cred.put("email", "asd@email.com");
//            cred.put("password", "Haslo1234");

            DataOutputStream localDataOutputStream = new DataOutputStream(connection.getOutputStream());
            localDataOutputStream.writeBytes(cred.toString());
            localDataOutputStream.flush();
            localDataOutputStream.close();

            Log.i("Registration status", String.valueOf(connection.getResponseCode()));
            Log.i("Registration message", connection.getRequestMethod());
//            if (connection.getResponseMessage())
            if (connection.getResponseMessage().equals("OK")) {
                return "OK";
            } else {
                return "Exception Caught";
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return "";
    }


    protected void onPostExecute(String result) {
        if (result.equalsIgnoreCase("Exception Caught")){
            Toast t = Toast.makeText(contextRef, "Wrong email or password!", Toast.LENGTH_SHORT);
            t.show();
        } else {
            Intent i = new Intent(contextRef, MainActivity.class);
            contextRef.startActivity(i);
        }
    }

}

class LoginParams {
    String email;
    String password;


    LoginParams(EditText email,EditText password) {
        this.email = email.getText().toString();
        this.password = password.getText().toString();
    }
}