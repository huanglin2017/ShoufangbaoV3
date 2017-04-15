package com.kupangstudio.shoufangbao.utils;


import com.kupangstudio.shoufangbao.model.Custom;

import java.util.ArrayList;

/**
 * Java汉字转换为拼音
 * Created by Jsmi on 2015/11/9.
 *
 */
public class CharacterParser {
    private static CharacterParser characterParser = new CharacterParser();

    public static CharacterParser getInstance() {
        return characterParser;
    }

    public static ArrayList<Custom> filledData(ArrayList<Custom> obj) {
        ArrayList<Custom> mSortList = new ArrayList<Custom>();
        for (int i = 0; i < obj.size(); i++) {
            Custom sortCustom = obj.get(i);
            // 汉字转换成拼音
            if (CommonUtils.getFirstSpell(obj.get(i).getName()).length() == 0) {
                sortCustom.setSortLetters("#");
            } else {
                String pinyin = CommonUtils.getFirstSpell(obj.get(i).getName());
                String sortString = pinyin.substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    sortCustom.setSortLetters(sortString.toUpperCase());
                } else {
                    sortCustom.setSortLetters("#");
                }
            }

            mSortList.add(sortCustom);
        }
        return mSortList;
    }

    public static Custom filledData(Custom obj) {
        // 汉字转换成拼音
        if (CommonUtils.getFirstSpell(obj.getName()).length() == 0) {
            obj.setSortLetters("#");
        } else {
            String pinyin = CommonUtils.getFirstSpell(obj.getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                obj.setSortLetters(sortString.toUpperCase());
            } else {
                obj.setSortLetters("#");
            }
        }

        return obj;
    }

    /*public static ArrayList<CenterCity> filledDataCity(ArrayList<CenterCity> obj) {
        ArrayList<CenterCity> mSortList = new ArrayList<CenterCity>();
        for (int i = 0; i < obj.size(); i++) {
            CenterCity sortCustom = obj.get(i);
            // 汉字转换成拼音
            if(PinyinUtils.getFirstSpell(obj.get(i).getName()).length() == 0){
                sortCustom.setSortLetter("#");
            }else{

                String pinyin = PinyinUtils.getFirstSpell(obj.get(i).getName());
                String sortString = pinyin.substring(0, 1).toUpperCase();
                // 正则表达式，判断首字母是否是英文字母
                if (sortString.matches("[A-Z]")) {
                    sortCustom.setSortLetter(sortString.toUpperCase());
                } else {
                    sortCustom.setSortLetter("#");
                }
            }

            mSortList.add(sortCustom);
        }
        return mSortList;
    }

    public static CenterCity filledDataCity(CenterCity obj) {
        if(PinyinUtils.getFirstSpell(obj.getName()).length() == 0){
            obj.setSortLetter("#");
        }else{
            // 汉字转换成拼音
            String pinyin = PinyinUtils.getFirstSpell(obj.getName());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                obj.setSortLetter(sortString.toUpperCase());
            } else {
                obj.setSortLetter("#");
            }
        }

        return obj;
    }*/

}

