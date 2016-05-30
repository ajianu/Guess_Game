package com.ajian.guess_game.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ajian.guess_game.R;
import com.ajian.guess_game.data.Const;
import com.ajian.guess_game.model.IAlertDialogButtonListener;
import com.ajian.guess_game.model.IWordButtonClickListener;
import com.ajian.guess_game.model.Song;
import com.ajian.guess_game.model.WordButton;
import com.ajian.guess_game.myui.MyGridView;
import com.ajian.guess_game.util.MyPlayer;
import com.ajian.guess_game.util.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements IWordButtonClickListener{

    //答案状态
    public final static int STATUS_ANSWER_RIGHT=1;
    public final static int STATUS_ANSWER_WRONG=2;
    public final static int STATUS_ANSWER_LACK=3;
    public final static int ID_DIALOG_DELETE_WORD=1;
    public final static int ID_DIALOG_TIP_ANSWER=2;
    public final static int ID_DIALOG_LACK_COINS=3;
    public final static int ID_DIALOG_EXIT_APP=4;

    //唱片相关动画
    private Animation mPanAnim;
    //线性动画——匀速
    private LinearInterpolator mPanLin;
    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;
    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;
    private ImageView mViewPan;
    private ImageView mViewPanBar;
    //当前关索引
    private TextView mCurrentStagePassView;
    private TextView mCurrentStageView;
    //当前歌曲名称
    private TextView mCurrentSongNamePassView;
    //Play按键事件
    private ImageButton mBtnPlayStart;

    //过关界面
    private View mPassview;
    private boolean mIsRunning=false;

    //文字框容器
    private ArrayList<WordButton> mAllWords;
    private ArrayList<WordButton> mBtnSelectWorlds;
    private MyGridView mMyGridView;
    //已选择文字框UI容器
    private LinearLayout mViewWordsContainer;
    //当前的歌曲
    private Song mCurrentSong;
    //当前关的索引
    private int mCurrentStageIndex=-1;

    //当前金币的数量
    private int mCurrentCoins =Const.TOTAL_COINS;

    //金币View
    private TextView mViewCurrentCoins;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*//读取上次结束时保存的游戏数据
        int[] datas=Util.loadData(this);
        mCurrentStageIndex=datas[Const.INDEX_LOAD_DATA_STAGE];
        mCurrentCoins=datas[Const.INDEX_LOAD_DATA_COINS];*/
        //初始化控件
        mViewPan= (ImageView) findViewById(R.id.imageView1);
        mViewPanBar= (ImageView) findViewById(R.id.imageView2);
        mMyGridView= (MyGridView) findViewById(R.id.gridview);

        mViewCurrentCoins= (TextView) findViewById(R.id.txt_bar_coins);
        mViewCurrentCoins.setText(mCurrentCoins + "");

        //注册监听
        mMyGridView.registOnWordButtonClick(this);

        mViewWordsContainer= (LinearLayout) findViewById(R.id.word_select_container);
        //初始化动画
        mPanAnim= AnimationUtils.loadAnimation(this,R.anim.rotate);
        mPanLin=new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPanBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarInAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_45);
        mBarInLin=new LinearInterpolator();
        //动画完成保持状态，直到被迫停止。
        mBarInAnim.setFillAfter(true);
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarOutAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_d_45);
        mBarOutLin=new LinearInterpolator();
        //完成动画保持状态，直到被迫停止。
        mBarOutAnim.setFillAfter(true);
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRunning = false;
                mBtnPlayStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBtnPlayStart= (ImageButton) findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"Hello",Toast.LENGTH_LONG).show();
                handlePlayButton();
            }
        });
        //初始化游戏数据
        initCurrentStageData();
        //处理删除按键事件
        handleDeleteWord();
        //处理提示按键事件
        handleTipAnswer();
    }

    private void handlePlayButton() {
        if (mViewPanBar!=null) {
            if (!mIsRunning) {
                mIsRunning = true;
                mViewPanBar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.INVISIBLE);
                //播放音乐
                MyPlayer.playSong(MainActivity.this,mCurrentSong.getSongFileName());
            }
        }
    }

    public void onPause(){
        //保存游戏数据
        Util.saveData(MainActivity.this,mCurrentStageIndex-1,mCurrentCoins);
        mViewPan.clearAnimation();
        //暂停音乐
        MyPlayer.stopTheSong(MainActivity.this);
        super.onPause();
    }

    private Song loadStageSongInfo(int stageIndex){
        Song song=new Song();

        String[] stage= Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Const.INDEX_FILE_NAME]);
        song.setSongName(stage[Const.INDEX_SONG_NAME]);
        return song;
    }
    //加载当前关的数据
    private void initCurrentStageData(){
        //读取当前关的歌曲信息
        mCurrentSong=loadStageSongInfo(++mCurrentStageIndex);
        //初始化已选择框
        mBtnSelectWorlds=initWordSelect();

        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(40,40);
        //清空原来的答案
        mViewWordsContainer.removeAllViews();
        //增加新的答案框
        for(int i=0;i<mBtnSelectWorlds.size();i++){
            mViewWordsContainer.addView(mBtnSelectWorlds.get(i).mViewButton,params);
        }
        //显示当前关的索引
        mCurrentStageView= (TextView) findViewById(R.id.text_current_stage);
        if (mCurrentStageView!=null) {
            mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
        }
        //获得数据
        mAllWords=initAllWord();
        //更新数据——MyGridView
        mMyGridView.updateData(mAllWords);
        //一开始播放音乐
        handlePlayButton();
    }
    //初始化待选文字框
    private ArrayList<WordButton> initAllWord(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();

        //获得所有待选文字
        String [] words=generateWords();

        for(int i=0;i< MyGridView.COUNTS_WORDS;i++){
            WordButton button=new WordButton();
            button.mWordString=words[i];
            data.add(button);
        }
        return data;
    }
    //初始化已选择文字框
    private ArrayList<WordButton> initWordSelect(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();

        for (int i=0;i<mCurrentSong.getNameLength(); i++) {
            View view = Util.getView(MainActivity.this, R.layout.self_ui_gridview_item);
            final WordButton holder=new WordButton();
            holder.mViewButton= (Button) view.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable=false;
            holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTheAnswer(holder);
                }
            });
            data.add(holder);
        }
        return data;
    }

    //生成所有的待选文字
    private String[] generateWords(){
        Random random=new Random();
        String[] words=new String[MyGridView.COUNTS_WORDS];

        //存入歌名
        for (int i=0;i<mCurrentSong.getNameLength();i++){
            words[i]=mCurrentSong.getNameCharacters()[i] + "";
        }
        //获取随机文字并存入数组
        for (int i=mCurrentSong.getNameLength();
             i<MyGridView.COUNTS_WORDS;i++){
            words[i]=getRandomChar()+"";
        }

        //打乱文字顺序首先从所有元素中随机选取一个与第一个元素交换，
        //然后在第二个之后选择一个元素与第二个交换，直到最后一个元素。
        //这样能够确保每个元素在每个位置的概率都是1/n。
        for (int i=MyGridView.COUNTS_WORDS-1;i>=0;i--){
            int index=random.nextInt(i + 1);

            String buf=words[index];
            words[index]=words[i];
            words[i]=buf;
        }

        return words;
    }
    //生成随机汉字
    private char getRandomChar(){
        String str="";
        int hightPos;
        int lowPos;

        Random random=new Random();
        hightPos = (176+Math.abs(random.nextInt(39)));
        lowPos=(161+Math.abs(random.nextInt(93)));

        byte[] b=new byte[2];
        b[0]=(Integer.valueOf(hightPos)).byteValue();
        b[1]=(Integer.valueOf(lowPos)).byteValue();

        try {
            str =new String(b,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);
    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        //Toast.makeText(this,wordButton.mIndex+"",Toast.LENGTH_SHORT).show();
        setSelectWorlds(wordButton);

        //获得答案状态
        int checkResult=checkTheAnswer();
        //检查答案
        if (checkResult==STATUS_ANSWER_RIGHT) {
            //获得相应的奖励
            handlePassEvent();
        }else if(checkResult==STATUS_ANSWER_WRONG){
            //闪烁文字并错误提示
            sparkTheWords();
        }else if(checkTheAnswer()==STATUS_ANSWER_LACK){
            //设置文字颜色为白色
            for (int i=0;i<mBtnSelectWorlds.size();i++){
                mBtnSelectWorlds.get(i).mViewButton.setTextColor(Color.WHITE);
            }
        }
    }
    //处理过关界面及事件
    private void handlePassEvent(){
        //显示过关界面
        mPassview=(LinearLayout)this.findViewById(R.id.pass_view);
        mPassview.setVisibility(View.VISIBLE);
        //停止未完成的动画
        mViewPan.clearAnimation();
        //停止正在播放的声音
        MyPlayer.stopTheSong(MainActivity.this);
        //播放音效
        MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_STONE_COIN);
        //当前关的索引
        mCurrentStagePassView= (TextView) findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView!=null) {
            mCurrentStagePassView.setText((mCurrentStageIndex+1) + "");
        }
        //显示歌曲的名称
        mCurrentStagePassView= (TextView) findViewById(R.id.text_current_song_name_pass);
        if (mCurrentStagePassView!=null) {
            mCurrentStagePassView.setText(mCurrentSong.getSongName());
        }
        //下一关按键处理
        ImageButton btnPass= (ImageButton) findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeAppPassed()) {
                    //进入到通关界面
                    Util.startActivity(MainActivity.this,AllPassView.class);
                }else{
                    //开始新一关
                    mPassview.setVisibility(View.GONE);
                    //加载管关卡数据
                    initCurrentStageData();
                }
            }
        });
    }
    //  判断是否通关
    private boolean judgeAppPassed(){
        return (mCurrentStageIndex==Const.SONG_INFO.length-1);
    }

    private void clearTheAnswer(WordButton wordButton){
        wordButton.mViewButton.setText("");
        wordButton.mWordString="";
        wordButton.mIsVisiable=false;
        //设置待选框可见性
        setButtonVisiable(mAllWords.get(wordButton.mIndex), View.VISIBLE);
    }
    //设置答案
    private void setSelectWorlds(WordButton wordButton){
        for (int i=0;i<mBtnSelectWorlds.size();i++){
            if (mBtnSelectWorlds.get(i).mWordString.length()==0){
                mBtnSelectWorlds.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectWorlds.get(i).mIsVisiable=true;
                mBtnSelectWorlds.get(i).mWordString=wordButton.mWordString;
                //记录索引
                mBtnSelectWorlds.get(i).mIndex=wordButton.mIndex;

                setButtonVisiable(wordButton,View.INVISIBLE);

                break;
            }
        }
    }
    //设置文字框是否可见
    private void setButtonVisiable(WordButton button, int visibility){
        button.mViewButton.setVisibility(visibility);
        button.mIsVisiable=(visibility==View.VISIBLE)? true:false;
        //
    }

    //检查答案
    private int checkTheAnswer(){
        //先检查长度
        for (int i=0;i<mBtnSelectWorlds.size();i++){
            //如果有空，答案不完整
            if(mBtnSelectWorlds.get(i).mWordString.length()==0){
                return STATUS_ANSWER_LACK;
            }
        }
        //答案完整，继续检查正确性
        StringBuffer sb=new StringBuffer();
        for (int i=0;i<mBtnSelectWorlds.size();i++){
            sb.append(mBtnSelectWorlds.get(i).mWordString);
        }

        return (sb.toString().equals(mCurrentSong.getSongName())) ?
                STATUS_ANSWER_RIGHT : STATUS_ANSWER_WRONG;
    }

    //文字闪烁
    private void sparkTheWords(){
        //定时器相关
        TimerTask task=new TimerTask() {
            boolean mChange=false;
            int mSpardTimes=0;
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++mSpardTimes>6) {
                            return;
                        }
                        //执行闪烁的逻辑：交替显示红色和白色
                        for (int i=0;i<mBtnSelectWorlds.size();i++){
                            mBtnSelectWorlds.get(i).mViewButton.setTextColor(
                                    mChange ? Color.RED : Color.WHITE);
                        }
                        mChange=!mChange;
                    }
                });
            }
        };

        Timer timer=new Timer();
        timer.schedule(task, 1, 150);
    }
    //提示自动选择一个答案
    private void tipAnswer() {
        boolean tipWord=false;
        for (int i=0;i<mBtnSelectWorlds.size();i++){
            if(mBtnSelectWorlds.get(i).mWordString.length()==0){
                //根据当前的答案框条件选择对应的文字并填入
                onWordButtonClick(findIsAnswerWord(i));
                tipWord=true;
                //减少金币数量
                if (!handleCoins(-getTipWordCoins())) {
                    //金币数量不够，显示对话框
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                    return;
                }
                break;
            }
        }
        //没有找到可以填充的答案
        if (!tipWord) {
            //闪烁文字提示用户
            sparkTheWords();
        }
    }

    //删除文字
    private void deleteOneWord() {
        //减少金币
        if (!handleCoins(-getDeleteWordCoins())) {
            //金币不够，显示提示对话框
            showConfirmDialog(ID_DIALOG_LACK_COINS);
            return;
        }
        //将这个索引对应的WordButton设置为不可见
        setButtonVisiable(findNotAnswerWord(),View.INVISIBLE);
    }
    //找到一个索引对应的WordButton设置为不可见
    private WordButton findNotAnswerWord(){
        Random random=new Random();
        WordButton buf=null;
        while (true){
            int index=random.nextInt(MyGridView.COUNTS_WORDS);
            buf=mAllWords.get(index);
            if (buf.mIsVisiable&&!isTheAnswerWord(buf)) {
                return buf;
            }
        }
    }
    //找到一个答案文字
    private WordButton findIsAnswerWord(int index){
        WordButton buf=null;
        for (int i=0;i<MyGridView.COUNTS_WORDS;i++){
            buf=mAllWords.get(i);
            if (buf.mWordString.equals("" + mCurrentSong.getNameCharacters()[index])) {
                return buf;
            }
        }
        return null;
    }
    //判断某个文字是否是答案
    private boolean isTheAnswerWord(WordButton word) {
        boolean result = false;
        for (int i = 0; i < mCurrentSong.getNameLength();i++) {
            if (word.mWordString.equals("" + mCurrentSong.getNameCharacters()[i])) {
                result=true;
                break;
            }
        }
        return result;
    }
    //增加或者减少指定数量的金币
    private boolean handleCoins(int data) {
        //判断当前总的金币数量是否可别减少
        if (mCurrentCoins + data>=0) {
            mCurrentCoins+=data;
            mViewCurrentCoins.setText(mCurrentCoins+"");
            return true;
        }else {
            //金币不够
            return false;
        }
    }
    //从配置文件里读取删除操作所要用的金币
    private int getDeleteWordCoins(){
        return this.getResources().getInteger(R.integer.pay_delete_word);
    }
    //从配置文件里读取提示操作所要用的金币
    private int getTipWordCoins(){
        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }
    //处理删除待选的文字的事件
    private void handleDeleteWord(){
        ImageButton button= (ImageButton) findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleteOneWord();
                showConfirmDialog(ID_DIALOG_DELETE_WORD);
            }
        });
    }
    //处理提示按键事件
    private void handleTipAnswer(){
        ImageButton button= (ImageButton) findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tipAnswer();
                showConfirmDialog(ID_DIALOG_TIP_ANSWER);
            }
        });
    }
    //自定义AlertDialog事件响应
    //退出程序
    private IAlertDialogButtonListener mBtnOkExitAppListener
            =new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            //System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };
    //删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener
            =new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            deleteOneWord();
        }
    };
    //答案提示
    private IAlertDialogButtonListener mBtnOkTipAnswerListener
            =new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            tipAnswer();
        }
    };
    //金币不足
    private IAlertDialogButtonListener mBtnOkLackCoinsListener
            =new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
        }
    };
    //显示对话框
    private void showConfirmDialog(int id){
        switch (id){
            case ID_DIALOG_EXIT_APP:
                Util.showDialog(MainActivity.this,"亲！真的要残忍的离开吗？",mBtnOkExitAppListener);
                break;
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this,"确认花掉"+ getDeleteWordCoins()+ "个金币去掉一个错误答案？",mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this,"确认花掉"+ getTipWordCoins()+ "个金币获得一个文字提示？",mBtnOkTipAnswerListener);
                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this,"金币不足，去商店补充？",mBtnOkLackCoinsListener);
                break;
        }
    }

    public boolean onKeyDown(int keyCode,KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
           /* AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示信息");
            builder.setMessage("亲，真的要残忍的离开吗？");
            builder.setPositiveButton("真的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setNegativeButton("骗你的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
            */

            showConfirmDialog(ID_DIALOG_EXIT_APP);
        }
        return super.onKeyDown(keyCode, event);
    }

}
