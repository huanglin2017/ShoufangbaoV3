package com.kupangstudio.shoufangbao.model;

/**
 * Created by Jsmi on 2015/11/24.
 * 猜猜乐答案
 */
public class Answer {
    private String title;
    private String error;
    private int right;
    private int answer;
    private int integrate;

    public int getIntegrate() {
        return integrate;
    }

    public void setIntegrate(int integrate) {
        this.integrate = integrate;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
