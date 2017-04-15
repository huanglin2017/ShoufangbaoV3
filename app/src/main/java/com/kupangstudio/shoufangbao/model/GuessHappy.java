package com.kupangstudio.shoufangbao.model;

import com.kupangstudio.shoufangbao.utils.StringUtils;

import java.io.Serializable;

/**
 * Created by Jsmi on 2015/11/24.
 * 猜猜乐列表数据结构
 */
public class GuessHappy implements Serializable{
    public final static int GUESS = 1;
    private int kid;//猜猜乐id
    private String remark;//备注
    private String day;//时间
    private String tittle;//标题
    private String[] content;//图片
    private int type;
    private String correct;//正确提示
    private String error;//错误提示
    private int answer;//答案

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getKid() {
        return kid;
    }

    public void setKid(int kid) {
        this.kid = kid;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTittle() {
        return tittle;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public String[] getContent() {
        return content;
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
