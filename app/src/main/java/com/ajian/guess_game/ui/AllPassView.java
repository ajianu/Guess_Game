package com.ajian.guess_game.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.ajian.guess_game.R;

/**
 * 通关界面
 * @author zhang jian on 2016/4/21.
 *
 */
public class AllPassView extends Activity{
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.all_pass_view);

        //隐藏右上角的金币按钮
        FrameLayout view= (FrameLayout) findViewById(R.id.layout_bar_coin);
        view.setVisibility(View.INVISIBLE);
    }

}
