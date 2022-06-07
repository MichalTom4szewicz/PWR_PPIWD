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

import com.google.gson.Gson;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.data.MagneticField;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Gyro;
import com.mbientlab.metawear.module.Led;
import com.mbientlab.metawear.module.MagnetometerBmm150;
import com.mbientlab.metawear.tutorial.starter.charts.BarChartActivity;
import com.mbientlab.metawear.tutorial.starter.charts.PieChartActivity;

import bolts.Continuation;
import bolts.Task;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.lang.Math;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


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
    private Accelerometer accelerometer;
    private MagnetometerBmm150 magnetometer;
    private Led ledModule;

    Context ctx;

    LocalDateTime time;

    double gyro_raw_x;
    double gyro_raw_y;
    double gyro_raw_z;

    double accel_raw_x;
    double accel_raw_y;
    double accel_raw_z;

    double magneto_raw_x;
    double magneto_raw_y;
    double magneto_raw_z;

    String gyro_string_x;
    String gyro_string_y;
    String gyro_string_z;

    String accel_string_x;
    String accel_string_y;
    String accel_string_z;

    String magneto_string_x;
    String magneto_string_y;
    String magneto_string_z;

    Integer initial_accel_loop = 1;
    Integer initial_gyro_loop = 1;


//    String activityType="none";
//    Integer repetitions=null;

    String csv_entry =
                        "time(sec)" + "," +
                        "gyroscope_x(deg/sec)" + "," +
                        "gyroscope_y(deg/sec)" + "," +
                        "gyroscope_z(deg/sec)" + "," +
                        "accelerometer_x(g)" + "," +
                        "accelerometer_y(g)" + "," +
                        "accelerometer_z(g)" + "," +
                        "magnetometer_x(T)" + "," +
                        "magnetometer_y(T)" + "," +
                        "magnetometer_z(T)" + "\n";

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

        Chronometer simpleChronometer = (Chronometer) view.findViewById(R.id.simpleChronometer);


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

                simpleChronometer.start(); // start a chronometer

                gyro.angularVelocity().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("Gyro", data.value(AngularVelocity.class).toString());

                                // Read Gyro and Prepare for writing to CSV
                                gyro_raw_x = data.value(AngularVelocity.class).x();
                                gyro_raw_y = data.value(AngularVelocity.class).y();
                                gyro_raw_z = data.value(AngularVelocity.class).z();

                                gyro_string_x = Double.toString(gyro_raw_x);
                                gyro_string_y = Double.toString(gyro_raw_y);
                                gyro_string_z = Double.toString(gyro_raw_z);

                                initial_gyro_loop = 0;
                            }

                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        gyro.angularVelocity().start();
                        gyro.start();

                        initial_gyro_loop = 1;

                        return null;
                    }
                });
                accelerometer.acceleration().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void apply(Data data, Object... env) {
                                Log.i("Accel", data.value(Acceleration.class).toString());

                                // Read Accel and Prepare for writing to CSV
                                accel_raw_x = data.value(Acceleration.class).x();
                                accel_raw_y = data.value(Acceleration.class).y();
                                accel_raw_z = data.value(Acceleration.class).z();
                                accel_string_x = Double.toString(accel_raw_x);
                                accel_string_y = Double.toString(accel_raw_y);
                                accel_string_z = Double.toString(accel_raw_z);

                                initial_accel_loop = 0;
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {

                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        accelerometer.acceleration().start();
                        accelerometer.start();

                        initial_accel_loop = 1;

                        return null;
                    }
                });
                magnetometer.magneticField().addRouteAsync(new RouteBuilder() {
                    @Override
                    public void configure(RouteComponent source) {
                        source.stream(new Subscriber() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void apply(Data data, Object ... env) {
                                Log.i("Magneto", data.value(MagneticField.class).toString());

                                // Read Magneto and Prepare for writing to CSV
                                magneto_raw_x = data.value(MagneticField.class).x();
                                magneto_raw_y = data.value(MagneticField.class).y();
                                magneto_raw_z = data.value(MagneticField.class).z();
                                magneto_string_x = Double.toString(magneto_raw_x);
                                magneto_string_y = Double.toString(magneto_raw_y);
                                magneto_string_z = Double.toString(magneto_raw_z);

                                if(initial_accel_loop == 0 && initial_gyro_loop == 0){
                                    csv_entry = csv_entry +
                                            time.now().toString() + "," +
                                            gyro_string_x + "," +
                                            gyro_string_y + "," +
                                            gyro_string_z + "," +
                                            accel_string_x + "," +
                                            accel_string_y + "," +
                                            accel_string_z + "," +
                                            magneto_string_x + "," +
                                            magneto_string_y + "," +
                                            magneto_string_z + "\n";
                                }
                            }
                        });
                    }
                }).continueWith(new Continuation<Route, Void>() {
                    @Override
                    public Void then(Task<Route> task) throws Exception {
                        magnetometer.magneticField().start();
                        magnetometer.start();
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
                accelerometer.acceleration().stop();
                accelerometer.stop();
                magnetometer.magneticField().stop();
                magnetometer.stop();
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
                accelerometer.acceleration().start();
                accelerometer.start();
                magnetometer.magneticField().start();
                magnetometer.start();

                Log.i("MetaWear", "Resume activity");

                simpleChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
                simpleChronometer.start();
            }
        });

        view.findViewById(R.id.geo_stop).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                view.findViewById(R.id.geo_stop).setEnabled(false);
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

                view.findViewById(R.id.geo_start).setClickable(true);

                String baseDir = "/storage/emulated/0/Download";
                String uniqueString = UUID.randomUUID().toString();
                String fileName = "AnalysisData" + uniqueString + ".csv";
                String filePath = baseDir + File.separator + fileName;
                File f = new File(filePath);

                gyro.stop();
                gyro.angularVelocity().stop();
                accelerometer.acceleration().stop();
                accelerometer.stop();
                magnetometer.magneticField().stop();
                magnetometer.stop();

                try {
                    OutputStream os = new FileOutputStream(f);
                    os.write(csv_entry.getBytes());
                    os.close();
                    Log.i("MainActivity", "File is created!");
                    sendFile(filePath);
                    new getTrainingSummary().execute();

//                    Intent intent = new Intent(v.getContext(), BarChartActivity.class);
//                    Intent intent = new Intent(v.getContext(), PieChartActivity.class);
//                    startActivity(intent);

                } catch (IOException e) {
                    Log.i("MainActivity", "File NOT created ...!");
                    e.printStackTrace();
                }


                csv_entry =
                        "time(sec)" + "," +
                        "gyroscope_x(deg/sec)" + "," +
                        "gyroscope_y(deg/sec)" + "," +
                        "gyroscope_z(deg/sec)" + "," +
                        "accelerometer_x(g)" + "," +
                        "accelerometer_y(g)" + "," +
                        "accelerometer_z(g)" + "," +
                        "magnetometer_x(T)" + "," +
                        "magnetometer_y(T)" + "," +
                        "magnetometer_z(T)" + "\n";

                initial_accel_loop = 1;
                initial_gyro_loop = 1;

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
                .odr(Gyro.OutputDataRate.ODR_25_HZ)
                .commit();

        accelerometer = metawear.getModule(Accelerometer.class);
        accelerometer.configure()
                .odr(25f)
                .commit();

        magnetometer = metawear.getModule(MagnetometerBmm150.class);
        magnetometer.configure()
                .outputDataRate(MagnetometerBmm150.OutputDataRate.ODR_25_HZ)
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
        FileUploadParams params = new FileUploadParams(F);
        new FileUpload().execute(params);
    }

    public void reconnected() { }

    class FileUpload extends AsyncTask <FileUploadParams, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("MetaWear", "Starting Background Task");
        }


        @Override
        protected Void doInBackground(FileUploadParams... fileUploadParams) {
            String path = "http://ppiwd.arturb.xyz:5000/measurements";
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
                connection.setRequestProperty("Authorization", "Bearer " + ((MyApplication) getActivity().getApplication()).getToken());

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

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();

                OneTrainingSummaryId trainingId = new Gson().fromJson(sb.toString(), OneTrainingSummaryId.class);
//                Log.i("IDDDDDD: ", trainingId.id);
                ((MyApplication) getActivity().getApplication()).setTrainingId(trainingId.id);

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();


            } catch (Exception e) {
                Log.i("error", e.toString());
            }
            return null;
        }
    }

    class getTrainingSummary extends AsyncTask <FileUploadParams, Void, Boolean> {

            String path = "http://ppiwd.arturb.xyz:5000/measurements/";

            HttpURLConnection connection = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((MyApplication) getActivity().getApplication()).setJumpingJacksCounter(0);
            ((MyApplication) getActivity().getApplication()).setSquatsCounter(0);
            ((MyApplication) getActivity().getApplication()).setRunningCounter(0);
            ((MyApplication) getActivity().getApplication()).setBoxingCounter(0);
        }


        @Override
        protected Boolean doInBackground(FileUploadParams... fileUploadParams) {

            try {
                URL url = new URL(path + ((MyApplication) getActivity().getApplication()).getTrainingId());

                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + ((MyApplication) getActivity().getApplication()).getToken());

                if (200 != connection.getResponseCode()) {
                    Log.i("Error", "Failed to get training summary:" + connection.getResponseCode() + " " + connection.getResponseMessage());
                }

                Log.i("Get Summary Server response", "Code:" + connection.getResponseCode() + " " + connection.getResponseMessage());

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();
                Log.i("Whole Training Summary: ", sb.toString());
                Training training = new Gson().fromJson(sb.toString(), Training.class);

                for (TrainingParams trainingParams  : training.classifications) {
                    if (trainingParams.getActivity_name().equalsIgnoreCase("jumping_jacks")) {
                        Integer jjNow = ((MyApplication) getActivity().getApplication()).getJumpingJacksCounter();
                        ((MyApplication) getActivity().getApplication()).setJumpingJacksCounter( jjNow + trainingParams.getCount());
                    }
                    if (trainingParams.getActivity_name().equalsIgnoreCase("squats")) {
                        Integer squatsNow = ((MyApplication) getActivity().getApplication()).getSquatsCounter();
                        ((MyApplication) getActivity().getApplication()).setSquatsCounter( squatsNow + trainingParams.getCount());
                    }
                    if (trainingParams.getActivity_name().equalsIgnoreCase("running")) {
                        Integer runningNow = ((MyApplication) getActivity().getApplication()).getRunningCounter();
                        ((MyApplication) getActivity().getApplication()).setRunningCounter( runningNow + trainingParams.getCount());
                    }
                    if (trainingParams.getActivity_name().equalsIgnoreCase("boxing")) {
                        Integer boxingNow = ((MyApplication) getActivity().getApplication()).getBoxingCounter();
                        ((MyApplication) getActivity().getApplication()).setBoxingCounter( boxingNow + trainingParams.getCount());
                    }
                    Log.i("JUMPING JACKS COUNT: ", ((MyApplication) getActivity().getApplication()).getJumpingJacksCounter().toString());
                    Log.i("SQUATS COUNT: ", ((MyApplication) getActivity().getApplication()).getSquatsCounter().toString());
                }


            } catch (Exception e) {
                Log.i("error", e.toString());
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean isReady) {
            try {
                if(isReady) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), PieChartActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.i("error", e.toString());
            }
            return;
        }

    }

}

class FileUploadParams {
    File file;

    FileUploadParams(File file) {
        this.file = file;
    }
}

class OneTrainingSummaryId {
    String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

class TrainingParams {
    String activity_name;
    Integer count;
    Double end;
    Double start;

    public String getActivity_name() { return activity_name; }
    public void setActivity_name(String activity_name) { this.activity_name = activity_name; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Double getEnd() { return end; }
    public void setEnd(Double end) { this.end = end; }
    public Double getStart() { return start; }
    public void setStart(Double start) { this.start = start; }
}

class Training {
    List<TrainingParams> classifications = null;

    String id;
    String processed_at;
    String sent_at;
    String user;

    public List<TrainingParams> getClassifications() { return classifications; }
    public void setClassifications(List<TrainingParams> classifications) { this.classifications = classifications; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProcessed_at() { return processed_at; }
    public void setProcessed_at(String processed_at) { this.processed_at = processed_at; }
    public String getSent_at() { return sent_at; }
    public void setSent_at(String sent_att) { this.sent_at = sent_at; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}