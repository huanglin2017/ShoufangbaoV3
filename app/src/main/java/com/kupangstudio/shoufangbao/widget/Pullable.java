package com.kupangstudio.shoufangbao.widget;

/**
 * Created by Jsmeli on 2016/3/3.
 * 定义下拉接口
 */
public interface Pullable
{
    /**
     * 判断是否可以下拉，如果不需要下拉功能可以直接return false
     *
     * @return true如果可以下拉否则返回false
     */
    boolean canPullDown();

    /**
     * 判断是否可以上拉，如果不需要上拉功能可以直接return false
     *
     * @return true如果可以上拉否则返回false
     */
    boolean canPullUp();

}
