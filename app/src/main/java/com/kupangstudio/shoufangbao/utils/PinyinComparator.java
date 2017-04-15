package com.kupangstudio.shoufangbao.utils;


import com.kupangstudio.shoufangbao.model.Custom;

import java.util.Comparator;

/**
 * Created by Jsmi on 2015/11/6.
 * 拼音比较器
 */
public class PinyinComparator implements Comparator<Custom> {

    public int compare(Custom o1, Custom o2) {
        if (o1.getSortLetters().equals("@")
                || o2.getSortLetters().equals("#")) {
            return -1;
        } else if (o1.getSortLetters().equals("#")
                || o2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return o1.getSortLetters().compareTo(o2.getSortLetters());
        }
    }
}
