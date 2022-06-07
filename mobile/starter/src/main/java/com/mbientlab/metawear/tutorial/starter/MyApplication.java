package com.mbientlab.metawear.tutorial.starter;

import android.app.Application;

public class MyApplication extends Application {
    String token;
    String trainingId;
    Integer jumpingJacksCounter = 0;
    Integer squatsCounter = 0;
    Integer runningCounter = 0;
    Integer boxingCounter = 0;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getTrainingId() { return trainingId; }
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }
    public Integer getJumpingJacksCounter() {
        return jumpingJacksCounter;
    }
    public void setJumpingJacksCounter(Integer jumpingJacksCounter) { this.jumpingJacksCounter = jumpingJacksCounter; }
    public Integer getSquatsCounter() {
        return squatsCounter;
    }
    public void setSquatsCounter(Integer squatsCounter) {
        this.squatsCounter = squatsCounter;
    }
    public Integer getRunningCounter() {
        return runningCounter;
    }
    public void setRunningCounter(Integer runningCounter) {
        this.runningCounter = runningCounter;
    }
    public Integer getBoxingCounter() {
        return boxingCounter;
    }
    public void setBoxingCounter(Integer boxingCounter) {
        this.boxingCounter = boxingCounter;
    }
}

