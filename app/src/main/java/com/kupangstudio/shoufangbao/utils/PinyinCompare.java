package com.kupangstudio.shoufangbao.utils;

import com.kupangstudio.shoufangbao.model.ContactBean;
import com.kupangstudio.shoufangbao.model.Custom;

import java.util.Comparator;

/**
 * Created by Jsmi on 2016/1/26.
 * 拼音比较器
 */
public class PinyinCompare implements Comparator<ContactBean> {
    public int compare(ContactBean o1, ContactBean o2) {
        if (o1.getSortKey().equals("@")
                || o2.getSortKey().equals("#")) {
            return -1;
        } else if (o1.getSortKey().equals("#")
                || o2.getSortKey().equals("@")) {
            return 1;
        } else {
            return o1.getSortKey().compareTo(o2.getSortKey());
        }
    }

}
