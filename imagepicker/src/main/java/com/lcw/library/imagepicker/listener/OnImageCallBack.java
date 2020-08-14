package com.lcw.library.imagepicker.listener;

import android.support.annotation.NonNull;

import java.util.ArrayList;

public interface OnImageCallBack {

    /**
     * When the action responds.
     *
     * @param result the result of the action.
     */
    void onCallBack(@NonNull ArrayList<String> result, long totalSize);

}