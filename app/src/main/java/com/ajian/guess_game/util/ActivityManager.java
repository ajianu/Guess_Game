package com.ajian.guess_game.util;

import android.app.Activity;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Administrator on 2016/4/22.
 */
public class ActivityManager{
    private LinkedList<Activity> activityLinkedList = new LinkedList<Activity>();

    private ActivityManager() {
    }

    private static ActivityManager instance;

    public static ActivityManager getInstance(){
        if(null == instance){
            instance = new ActivityManager();
        }
        return instance;
    }

    //向list中添加Activity
    public ActivityManager addActivity(Activity activity){
        activityLinkedList.add(activity);
        return instance;
    }

    //结束特定的Activity(s)
    public ActivityManager finshActivities(Class<? extends Activity>... activityClasses){
        for (Activity activity : activityLinkedList) {
            if( Arrays.asList(activityClasses).contains( activity.getClass() ) ){
                activity.finish();
            }
        }
        return instance;
    }

    //结束所有的Activities
    private void finshAllActivities() {
        for (Activity activity : activityLinkedList) {
            activity.finish();
        }
    }
}
