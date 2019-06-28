package com.basit.sweetshowcaselibrary;

public interface SweetShowcaseLayoutListener {

    void onSlideRight(float position);

    void onSlideLeft(float position);

    void onSlideUp(float position);

    void onSlideDown(float position);

    void onPress();

    void onRelease();

    void onScrollCompleted();

}
