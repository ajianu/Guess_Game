package com.ajian.guess_game.model;

import android.widget.Button;

/**
 * @author zhang jian
 * Created by Administrator on 2016/4/19.
 */
public class WordButton {
    public int mIndex;
    public boolean mIsVisiable;
    public String mWordString;

    public Button mViewButton;

    public WordButton(){
        mIsVisiable=true;
        mWordString="";
    }
}
