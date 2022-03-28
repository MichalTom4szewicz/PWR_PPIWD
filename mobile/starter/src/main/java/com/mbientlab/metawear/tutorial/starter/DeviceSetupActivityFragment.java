/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.metawear.tutorial.starter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.Gyro;
import com.mbientlab.metawear.module.Led;

import bolts.Continuation;
import bolts.Task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    private MetaWearBoard metawear = null;
    private FragmentSettings settings;

    private Gyro gyro;
    private Led ledModule;

    Context ctx;

    LocalDateTime time;

    double gyro_raw_x;
    double gyro_raw_y;
    double gyro_raw_z;

    String gyro_string_x;
    String gyro_string_y;
    String gyro_string_z;

    String activityType="";
    String repetitions="";

    String csv_entry = "activity_type" + "," +
                        "repetitions" + "," +
                        "time(sec)" + "," +
                        "gyroscope_x(deg/sec)" + "," +
                        "gyroscope_y(deg/sec)" + "," +
                        "gyroscope_z(deg/sec)" + "\n";

    long timeWhenPaused = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity owner= getActivity();
        if (!(owner instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings= (FragmentSettings) owner;
        owner.getApplicationContext().bindService(new Intent(owner, BtleService.class), this, Context.BIND_AUTO_CREATE);
        ctx = owner.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_device_setup, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Switch jumping_jacks_switch = (Switch) view.findViewById(R.id.jumpingJacksSwitch);
        Switch squats_switch = (Switch) view.findViewById(R.id.squatsSwitch);
        Switch running_switch = (Switch) view.findViewById(R.id.runningSwitch);
        Switch boxing_switch = (Switch) view.findViewById(R.id.boxingSwitch);

        Switch repeat5_switch = (Switch) view.findViewById(R.id.repeat5Switch);
        Switch repeat10_switch = (Switch) view.findViewById(R.id.repeat10Switch);
        Switch repeat15_switch = (Switch) view.findViewById(R.id.repeat15Switch);
        Switch repeat20_switch = (Switch) view.findViewById(R.id.repeat20Switch);

        Chronometer simpleChronometer = (Chronometer) view.findViewById(R.id.simpleChronometer);

        jumping_jacks_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "jumping jacks switch " + isChecked);
                if (isChecked) {
                    squats_switch.setChecked(false);
                    running_switch.setChecked(false);
                    boxing_switch.setChecked(false);
                    activityType="jumping_jacks";
                    if (repetitions != ""){
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    activityType="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        squats_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "squats switch " + isChecked);
                if (isChecked) {
                    jumping_jacks_switch.setChecked(false);
                    running_switch.setChecked(false);
                    boxing_switch.setChecked(false);
                    activityType="squats";
                    if (repetitions != ""){
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    activityType="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        running_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "running switch" + isChecked);
                if (isChecked) {
                    jumping_jacks_switch.setChecked(false);
                    squats_switch.setChecked(false);
                    boxing_switch.setChecked(false);
                    activityType="running";
                    if (repetitions != ""){
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    activityType="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        boxing_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "boxing switch" + isChecked);
                if (isChecked) {
                    jumping_jacks_switch.setChecked(false);
                    squats_switch.setChecked(false);
                    running_switch.setChecked(false);
                    activityType="boxing";
                    if (repetitions != ""){
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    activityType="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        repeat5_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "jumping jacks switch " + isChecked);
                if (isChecked) {
                    repeat10_switch.setChecked(false);
                    repeat15_switch.setChecked(false);
                    repeat20_switch.setChecked(false);
                    repetitions="5";
                    if (activityType != "") {
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    repetitions="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        repeat10_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "jumping jacks switch " + isChecked);
                if (isChecked) {
                    repeat5_switch.setChecked(false);
                    repeat15_switch.setChecked(false);
                    repeat20_switch.setChecked(false);
                    repetitions="10";
                    if (activityType != "") {
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    repetitions="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        repeat15_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "jumping jacks switch " + isChecked);
                if (isChecked) {
                    repeat5_switch.setChecked(false);
                    repeat10_switch.setChecked(false);
                    repeat20_switch.setChecked(false);
                    repetitions="15";
                    if (activityType != "") {
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    repetitions="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        repeat20_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i("Switch State=", "jumping jacks switch " + isChecked);
                if (isChecked) {
                    repeat5_switch.setChecked(false);
                    repeat10_switch.setChecked(false);
                    repeat15_switch.setChecked(false);
                    repetitions="20";
                    if (activityType != "") {
                        view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.geo_stop).setVisibility(View.VISIBLE);
                    }
                } else {
                    repetitions="";
                    view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.geo_stop).setVisibility(View.INVISIBLE);
                }
            }
        });

        view.findViewById(R.id.geo_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.geo_start).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.geo_pause).setVisibility(View.VISIBLE);

                view.findViewById(R.id.geo_stop).setEnabled(true);

                simpleChronometer.setBase(SystemClock.elapsedRealtime());

                Log.i("MetaWear", "Connected");
                ledModule = metawear.getModule(Led.class);

                ledModule.stop(true);
                Log.i("MetaWear", "Starting activity");
                ledModule.editPattern(Led.Color.GREEN)
                        .riseTime((short) 0)
                        .pulseDuration((short) 1000)
                        .repeatCount((byte) 2)
                        .highTime((short) 500)
                        .highIntensity((byte) 16)
                        .lowIntensity((byte) 16)
                        .commit();
                ledModule.play();

                view.findViewById(R.id.timeText).setVisibility(View.VISIBLE);
                view.findViewById(R.id.simpleChronometer).setVisibility(View.VISIBLE);

                jumping_jacks_switch.setClickable(false);
                squats_switch.setClickable(false);
                running_switch.setClickable(false);
                boxing_switch.setClickable(false);

                repeat5_switch.setClickable(false);
                repeat10_switch.setClickable(false);
                repeat15_switch.setClickable(false);
                repeat20_switch.setClickable(false);


                simpleChronometer.start(); // start a chronometer

                gyro.angularVelocity().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("MainActivity", data.value(AngularVelocity.class).toString());

                                // Read Gyro and Prepare for writing to CSV
                                gyro_raw_x = data.value(AngularVelocity.class).x();
                                gyro_raw_y = data.value(AngularVelocity.class).y();
                                gyro_raw_z = data.value(AngularVelocity.class).z();
                                gyro_raw_x = Math.toRadians(gyro_raw_x);
                                gyro_raw_y = Math.toRadians(gyro_raw_y);
                                gyro_raw_z = Math.toRadians(gyro_raw_z);

                                gyro_string_x = Double.toString(gyro_raw_x);
                                gyro_string_y = Double.toString(gyro_raw_y);
                                gyro_string_z = Double.toString(gyro_raw_z);

                                csv_entry = csv_entry +
                                            activityType + "," +
                                            repetitions + "," +
                                            time.now().toString() + "," +
                                            gyro_string_x + "," +
                                            gyro_string_y + "," +
                                            gyro_string_z + "\n";
                            }

                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        gyro.angularVelocity().start();
                        gyro.start();

                        return null;
                    }
                });
            }
        });

        view.findViewById(R.id.geo_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.geo_resume).setVisibility(View.VISIBLE);
                view.findViewById(R.id.geo_pause).setVisibility(View.INVISIBLE);

                gyro.stop();
                gyro.angularVelocity().stop();
                Log.i("MetaWear", "Pause activity");

                timeWhenPaused = simpleChronometer.getBase() - SystemClock.elapsedRealtime();
                simpleChronometer.stop();
            }
        });

        view.findViewById(R.id.geo_resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.geo_pause).setVisibility(View.VISIBLE);
                view.findViewById(R.id.geo_resume).setVisibility(View.INVISIBLE);

                gyro.start();
                gyro.angularVelocity().start();
                Log.i("MetaWear", "Resume activity");

                simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
                simpleChronometer.start();
            }
        });

        view.findViewById(R.id.geo_stop).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                view.findViewById(R.id.geo_pause).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.geo_resume).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.geo_start).setVisibility(View.VISIBLE);
                view.findViewById(R.id.timeText).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.simpleChronometer).setVisibility(View.INVISIBLE);

                simpleChronometer.stop();
                timeWhenPaused = 0;

                Log.i("MetaWear", "Connected");
                ledModule = metawear.getModule(Led.class);
                ledModule.stop(true);
                Log.i("MetaWear", "Ending activity");
                ledModule.editPattern(Led.Color.GREEN)
                        .riseTime((short) 0)
                        .pulseDuration((short) 1000)
                        .repeatCount((byte) 2)
                        .highTime((short) 500)
                        .highIntensity((byte) 16)
                        .lowIntensity((byte) 16)
                        .commit();
                ledModule.play();

                jumping_jacks_switch.setClickable(true);
                squats_switch.setClickable(true);
                running_switch.setClickable(true);
                boxing_switch.setClickable(true);

                repeat5_switch.setClickable(true);
                repeat10_switch.setClickable(true);
                repeat15_switch.setClickable(true);
                repeat20_switch.setClickable(true);

                view.findViewById(R.id.geo_start).setClickable(true);

                String baseDir = "/storage/emulated/0/Download";
                String uniqueString = UUID.randomUUID().toString();
                String fileName = "AnalysisData" + uniqueString + ".csv";
                String filePath = baseDir + File.separator + fileName;
                File f = new File(filePath);

                gyro.stop();
                gyro.angularVelocity().stop();

                try {
                    OutputStream os = new FileOutputStream(f);
                    os.write(csv_entry.getBytes());
                    os.close();
                    Log.i("MainActivity", "File is created!");
                    sendFile(filePath);
                } catch (IOException e) {
                    Log.i("MainActivity", "File NOT created ...!");
                    e.printStackTrace();
                }

                csv_entry = "activity_type" + "," +
                        "repetitions" + "," +
                        "time(sec)" + "," +
                        "gyroscope_x(deg/sec)" + "," +
                        "gyroscope_y(deg/sec)" + "," +
                        "gyroscope_z(deg/sec)" + "\n";

                metawear.tearDown();
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        metawear = ((BtleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());

        Log.i("MetaWear", "Connected");
        ledModule = metawear.getModule(Led.class);
        ledModule.stop(true);
        Log.i("MetaWear", "Turn on BLUE LED");
        ledModule.editPattern(Led.Color.BLUE)
                .riseTime((short) 0)
                .pulseDuration((short) 1000)
                .repeatCount((byte) 2)
                .highTime((short) 500)
                .highIntensity((byte) 16)
                .lowIntensity((byte) 16)
                .commit();
        ledModule.play();


        gyro = metawear.getModule(Gyro.class);
        gyro.configure()
                .odr(Gyro.OutputDataRate.ODR_50_HZ)
                .range(Gyro.Range.FSR_2000)
                .commit();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    public void sendFile(String filePath) {
        File F = new File(filePath);
        if (F == null) {
            Log.i("MetaWear", "File not found");
            return;
        }
        FileUploadParams params = new FileUploadParams(F,activityType);
        new FileUpload().execute(params);
    }

    public void reconnected() { }
}
class FileUpload extends AsyncTask<FileUploadParams, Void, Void> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("MetaWear", "Starting Background Task");
    }


    @Override
    protected Void doInBackground(FileUploadParams... fileUploadParams) {
        String path = "http://ppiwd.arturb.xyz:5000/training/measurement/" + fileUploadParams[0].activityType;
        File file = fileUploadParams[0].file;

        String parameterName = "measurements";
        String attachmentFileName = file.getName();

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String[] q = attachmentFileName.split("/");
        int idx = q.length - 1;
        String fileMimeType = "text/csv";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(path);

            connection = (HttpURLConnection) url.openConnection();

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + parameterName + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            if (200 != connection.getResponseCode()) {
                Log.i("Error", "Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            Log.i("Server response", "Code:" + connection.getResponseCode() + " " + connection.getResponseMessage());

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.i("error", e.toString());
        }
        return null;
    }
}
class FileUploadParams {
    File file;
    String activityType;

    FileUploadParams(File file, String activityType) {
        this.file = file;
        this.activityType = activityType;
    }
}