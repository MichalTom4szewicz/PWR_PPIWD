package com.mbientlab.metawear.tutorial.starter.data;

import android.util.Log;

import com.mbientlab.metawear.tutorial.starter.data.model.LoggedInUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

//        try {
            // TODO: handle loggedInUser authentication
            String path = "http://ppiwd.arturb.xyz:5000/auth/login" ;

//            HttpURLConnection connection = null;

            Log.i("sadas", "zzzzzzzzz");

            try {
                URL url = new URL(path);
                Log.i("sadas", "1");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
//                connection.setRequestProperty("Connection", "Keep-Alive");
//                connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");

                Log.i("sadas", "2");

                JSONObject creds = new JSONObject();
                creds.put("email", username);
                creds.put("password", password);
                String jsonInputString = creds.toString();
                Log.i("sadas", "3");
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                Log.i("sadas", "44");
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
//                Log.i("xd", connection.getOutputStream().toString());
                Log.i("sadas", "3.4");

//                DataOutputStream localDataOutputStream = new DataOutputStream(connection.getOutputStream());
                Log.i("sadas", "3.5");

//                localDataOutputStream.writeBytes(creds.toString());
//                localDataOutputStream.flush();
//                localDataOutputStream.close();
                Log.i("sadas", "4");

                Log.i("Login status", String.valueOf(connection.getResponseCode()));
                Log.i("Login message", connection.getRequestMethod());

            } catch (IOException | JSONException e) {
                Log.i("ss", e.toString());
                e.printStackTrace();
            }
//            Log.i("Registration status", String.valueOf(connection.getResponseCode()));
//            Log.i("Registration message", connection.getRequestMethod());
//            return null;
//            return new Result.Error(new IOException("Error logging in"));
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
//        } catch (Exception e) {
//            return new Result.Error(new IOException("Error logging in", e));
//        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}