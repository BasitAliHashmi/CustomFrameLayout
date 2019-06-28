package com.basit.sweetshowcase;

public interface CustomFrameLayoutListener {

    void onSlideRight(float position) ;

    void onSlideLeft(float position) ;

    void onSlideUp(float position) ;

    void onSlideDown(float position) ;

    void onPress();

    void onRelease();

    void onScrollCompleted();
}
