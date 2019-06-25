package com.basit.customframelayout;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomFrameLayoutListener {

    private static List<Integer> AVAILABLE_FRAMES_IDS = new ArrayList<>();
    private CustomFrameLayout mLayoutRoot;

    private int mBaseWidth;
    private int mBaseHeight;
    private int mBorderSize = 2;
    private boolean isSelect;
    private final int ANIMATION_DURATION = 500;

    private List<View> frames = new ArrayList<View>();
    private List<View> currentOnScreenViews = new ArrayList<>();
    private List<View> nextScrollableViews = new ArrayList<>();
    private int mNextScrollIndex = 1;
    private View mSelectedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        AVAILABLE_FRAMES_IDS.add(R.layout.frame_eight_discover);
        AVAILABLE_FRAMES_IDS.add(R.layout.frame_nine_discover);
        //AVAILABLE_FRAMES_IDS.add(R.layout.frame_ten_discover);
        //AVAILABLE_FRAMES_IDS.add(R.layout.frame_eleven_discover);

        mLayoutRoot = findViewById(R.id.layoutRoot);

        mLayoutRoot.post(new Runnable() {
            @Override
            public void run() {

                mBaseWidth = mLayoutRoot.getWidth();
                mBaseHeight = mLayoutRoot.getHeight();

                inflateFrameViews(AVAILABLE_FRAMES_IDS.size());
                prepareLayoutResponsiveDynamic(frames);
                //BindDataDynamic(response.getResult(), frames);

                mLayoutRoot.addOnSwipeListener(MainActivity.this);
            }
        });
    }

    //region CUSTOM-FRAME-LAYOUT-LISTENER

    @Override
    public void onSlideRight(float position) {
        drag(mRight, position);
    }

    @Override
    public void onSlideLeft(float position) {
        drag(mLeft, position);
    }

    @Override
    public void onSlideUp(float position) {
        drag(mTop, position);
    }

    @Override
    public void onSlideDown(float position) {
        drag(mBottom, position);
    }

    @Override
    public void onPress() {
        //resetSlideShowTimer();
    }

    @Override
    public void onRelease() {
        //startSlideShowTimer();
    }

    @Override
    public void onScrollCompleted() {
        reArrangeDynamicViews(mNextScrollIndex, frames);

        mNextScrollIndex += 1;
        if (mNextScrollIndex >= frames.size()) {
            mNextScrollIndex = 0;
        }

        setNextScrollingDynamicViews(frames.get(mNextScrollIndex));
    }


    //endregion

    void inflateFrameViews(int count) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < count; i++) {
            View frame = inflater.inflate(AVAILABLE_FRAMES_IDS.get(i % AVAILABLE_FRAMES_IDS.size()), null);
            mLayoutRoot.addView(frame);
            frames.add(frame);
        }

        reArrangeDynamicViews(0, frames);

        if(frames.size() > 1)
            setNextScrollingDynamicViews(frames.get(1));
    }


    void moveRight(View v, float pos){
        v.setTranslationY(0);
        v.setTranslationX(pos * v.getWidth() * -1);
    }

    void moveLeft(View v, float pos){
        v.setTranslationY(0);
        v.setTranslationX(pos * v.getWidth());
    }

    void moveTop(View v, float pos){
        v.setTranslationX(0);
        v.setTranslationY(pos * v.getHeight());
    }

    void moveBottom(View v, float pos){
        v.setTranslationX(0);
        v.setTranslationY(pos * v.getHeight() * -1);
    }

    void changeAlpha(View v, float pos) {
        v.setAlpha(Math.abs(1 - pos));
    }

    void deSelect(View selectedView) {
        selectedView.setPadding(mBorderSize,mBorderSize,0,0);

        String[] viewOriginalDimension = selectedView.getTag().toString().split(":");
        int viewOriginalWidthPercent = getValuePercentageHorizontal(Float.parseFloat(viewOriginalDimension[0]));
        int viewOriginalHeightPercent = getValuePercentageVertical(Float.parseFloat(viewOriginalDimension[1]));

        int viewOriginalMarginLeft = getValuePercentageHorizontal(Float.parseFloat(viewOriginalDimension[2]));
        int viewOriginalMarginTop = getValuePercentageVertical(Float.parseFloat(viewOriginalDimension[3]));

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedView.getLayoutParams();

        animateWidth(selectedView, params, viewOriginalWidthPercent);
        animateHeight(selectedView, params, viewOriginalHeightPercent);
        if (viewOriginalMarginLeft > 0)
            animateMarginLeft(selectedView, params, viewOriginalMarginLeft);
        if (viewOriginalMarginTop > 0)
            animateMarginTop(selectedView, params, viewOriginalMarginTop);

        for (int i = 0; i < currentOnScreenViews.size(); i++) {
            View tempView = currentOnScreenViews.get(i);
            if (tempView.getId() != selectedView.getId()) {
                endAnimation(tempView, i);
            }

            tempView.setEnabled(true);
        }


        //All Category View
        if (selectedView.getTag().toString().contains("p")) {
            TextView lblAllProduct = (TextView) selectedView.findViewById(R.id.lbl_all_product);
            lblAllProduct.setAlpha(0f);
            lblAllProduct.setVisibility(View.VISIBLE);
            animateViewAlpha(lblAllProduct, 0f, 1f, false);

            TextView btnAllCategory = (TextView) selectedView.findViewById(R.id.btn_all_categories);
            animateViewAlpha(btnAllCategory, 1f, 0f, true);
        } else {
            TextView btnAllProducts = (TextView) ((ViewGroup)selectedView.getParent()).findViewById(R.id.btn_all_products);
            animateViewAlpha(btnAllProducts, 1f, 0f, true);
        }
    }

    void select(View selectedView) {

        selectedView.setPadding(0,0,0,0);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedView.getLayoutParams();

        animateWidth(selectedView, params, mBaseWidth);
        animateHeight(selectedView, params, getValuePercentageVertical(60f));
        if (params.leftMargin > 0)
            animateMarginLeft(selectedView, params, 0);
        if (params.topMargin > 0)
            animateMarginTop(selectedView, params, 0);

        int index = 0;
        for (int i = 0; i < currentOnScreenViews.size(); i++) {
            View tempView = currentOnScreenViews.get(i);
            if (tempView.getId() != selectedView.getId()) {
                startAnimation(tempView, index);
                index += 1;
            }
        }

        //All Category View
        if (selectedView.getTag().toString().contains("p")) {
            TextView lblAllProduct = (TextView) selectedView.findViewById(R.id.lbl_all_product);
            animateViewAlpha(lblAllProduct, 1f, 0f, true);

            TextView btnAllCategory = (TextView) selectedView.findViewById(R.id.btn_all_categories);
            btnAllCategory.setAlpha(0);
            btnAllCategory.setVisibility(View.VISIBLE);
            animateViewAlpha(btnAllCategory, 0f, 1f, false);
        } else {
            TextView btnAllProducts = (TextView) ((ViewGroup)selectedView.getParent()).findViewById(R.id.btn_all_products);
            btnAllProducts.setAlpha(0);
            btnAllProducts.setVisibility(View.VISIBLE);
            animateViewAlpha(btnAllProducts, 0f, 1f, false);
        }
    }

    void startAnimation(View v, int index) {

        int defaultSettleDownWidth = getValuePercentageHorizontal(33.4f);
        int defaultSettleDownHeight = getValuePercentageVertical(20);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        animateWidth(v, params, defaultSettleDownWidth);
        animateHeight(v, params, defaultSettleDownHeight);

        int settleDownMarginLeft = 0;
        int settleDownMarginTop = getValuePercentageVertical(60);
        switch (index) {
            case 0:
                break;
            case 1:
                settleDownMarginLeft = defaultSettleDownWidth;
                break;
            case 2:
                settleDownMarginLeft = defaultSettleDownWidth * 2;
                break;
            case 3:
                settleDownMarginTop += defaultSettleDownHeight;
                break;
            case 4:
                settleDownMarginLeft = defaultSettleDownWidth;
                settleDownMarginTop += defaultSettleDownHeight;
                break;
            case 5:
                settleDownMarginLeft = defaultSettleDownWidth * 2;
                settleDownMarginTop += defaultSettleDownHeight;
                break;
        }

        animateMarginLeft(v, params, settleDownMarginLeft);
        animateMarginTop(v, params, settleDownMarginTop);
    }

    void endAnimation(View v, int index) {

        String[] viewOriginalDimension = v.getTag().toString().split(":");
        int viewOriginalWidthPercent = getValuePercentageHorizontal(Integer.parseInt(viewOriginalDimension[0]));
        int viewOriginalHeightPercent = getValuePercentageVertical(Integer.parseInt(viewOriginalDimension[1]));

        int viewOriginalMarginLeft = getValuePercentageHorizontal(Integer.parseInt(viewOriginalDimension[2]));
        int viewOriginalMarginTop = getValuePercentageVertical(Integer.parseInt(viewOriginalDimension[3]));

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
        animateWidth(v, params, viewOriginalWidthPercent);
        animateHeight(v, params, viewOriginalHeightPercent);
        animateMarginLeft(v, params, viewOriginalMarginLeft);
        animateMarginTop(v, params, viewOriginalMarginTop);

    }

    void animateWidth(final View v, final FrameLayout.LayoutParams params, int to) {

        ValueAnimator anim = new ValueAnimator().ofInt(v.getWidth(), to);
        anim.setDuration(ANIMATION_DURATION);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.width = (int) valueAnimator.getAnimatedValue();
                v.setLayoutParams(params);
            }
        });
        anim.start();
    }

    void animateHeight(final View v, final FrameLayout.LayoutParams params, int to){

        ValueAnimator anim = new ValueAnimator().ofInt(v.getHeight(), to);
        anim.setDuration(ANIMATION_DURATION);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (int) valueAnimator.getAnimatedValue();
                v.setLayoutParams(params);
            }
        });
        anim.start();

    }

    void animateMarginLeft(final View v, final FrameLayout.LayoutParams params, int to) {

        ValueAnimator anim = new ValueAnimator().ofInt(params.leftMargin, to);
        anim.setDuration(ANIMATION_DURATION);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (int) valueAnimator.getAnimatedValue();
                v.setLayoutParams(params);
            }
        });
        anim.start();

    }

    void animateMarginTop(final View v, final FrameLayout.LayoutParams params, int to) {
        ValueAnimator anim = new ValueAnimator().ofInt(params.topMargin, to);
        anim.setDuration(ANIMATION_DURATION);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (int) valueAnimator.getAnimatedValue();
                v.setLayoutParams(params);
            }
        });
        anim.start();
    }

    void animateViewAlpha(final View v, float from, float to, final boolean hide) {
        ValueAnimator va = ValueAnimator.ofFloat(from, to);
        va.setDuration(ANIMATION_DURATION);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.setAlpha((float)animation.getAnimatedValue());
            }
        });

        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(hide)
                    v.setVisibility(View.GONE);
                else
                    v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        va.start();
    }

    int mLeft = 0, mRight = 1, mTop = 2, mBottom = 3, mDirection = 0;
    void drag(int direction, float position) {

        for (int i = 0; i < nextScrollableViews.size(); i++) {

            if (direction == mLeft) {
                moveLeft(nextScrollableViews.get(i), position);
            } else if (direction == mRight) {
                moveRight(nextScrollableViews.get(i), position);
            } else if (direction == mTop) {
                moveTop(nextScrollableViews.get(i), position);
            } else if (direction == mBottom) {
                moveBottom(nextScrollableViews.get(i), position);
            }

            if (nextScrollableViews.get(i).getId() == R.id.lbl_all_product) {
                changeAlpha(nextScrollableViews.get(i), position);
            }
        }

    }


    //// RESPONSIVE LAYOUT CALCULATION ////
    private void prepareLayoutResponsiveDynamic(List<View> parentViews) {

        for(int p = 0; p < parentViews.size(); p ++) {

            View singleParentView = parentViews.get(p);

            List<View> allViews = new ArrayList<View>();
            allViews.add(singleParentView.findViewById(R.id.l1));
            allViews.add(singleParentView.findViewById(R.id.l2));
            allViews.add(singleParentView.findViewById(R.id.l3));
            allViews.add(singleParentView.findViewById(R.id.l4));
            allViews.add(singleParentView.findViewById(R.id.l5));
            allViews.add(singleParentView.findViewById(R.id.l6));
            allViews.add(singleParentView.findViewById(R.id.l7));

            for (int i = 0; i < allViews.size(); i++) {

                View v = allViews.get(i);

                v.setPadding(mBorderSize, mBorderSize, 0, 0);

                String[] viewOriginalDimension = v.getTag().toString().split(":");
                int viewCalculatedWidth = getValuePercentageHorizontal(Float.parseFloat(viewOriginalDimension[0]));
                int viewCalculatedHeight = getValuePercentageVertical(Float.parseFloat(viewOriginalDimension[1]));

                int viewCalculatedMarginLeft = getValuePercentageHorizontal(Float.parseFloat(viewOriginalDimension[2]));
                int viewCalculatedMarginTop = getValuePercentageVertical(Float.parseFloat(viewOriginalDimension[3]));

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewCalculatedWidth, viewCalculatedHeight);
                params.setMargins(viewCalculatedMarginLeft, viewCalculatedMarginTop, 0, 0);

                v.setLayoutParams(params);
            }

            RelativeLayout overlay = singleParentView.findViewById(R.id.overlay);
            FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(mBaseWidth, mBaseHeight);
            overlay.setLayoutParams(overlayParams);
        }
    }

    private int getValuePercentageHorizontal(float rawValue) {
        return Math.round((rawValue * mBaseWidth) / 100);
    }

    private int getValuePercentageVertical(float rawValue) {
        return Math.round((rawValue * mBaseHeight) / 100);
    }

    /*void BindDataDynamic(final List<FeaturedItem> items, List<View> views) {

        for (int i = 0; i < items.size(); i++) {

            View parent = views.get(i);

            if (parent != null) {

                TextView btnAllCategory = (TextView) parent.findViewById(R.id.btn_all_categories);
                TextView btnAllProducts = (TextView) parent.findViewById(R.id.btn_all_products);
                btnAllProducts.setTag(items.get(i).getId());

                btnAllCategory.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((StoreActivity) getActivity()).setTabFromFragment(1);
                    }
                });

                btnAllProducts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((StoreActivity) getActivity()).setStoreTab((int) v.getTag());
                    }
                });

                List<ImageView> allImageViews = new ArrayList<>();
                allImageViews.add((ImageView) parent.findViewById(R.id.s1));
                allImageViews.add((ImageView) parent.findViewById(R.id.s2));
                allImageViews.add((ImageView) parent.findViewById(R.id.s3));
                allImageViews.add((ImageView) parent.findViewById(R.id.s4));
                allImageViews.add((ImageView) parent.findViewById(R.id.s5));
                allImageViews.add((ImageView) parent.findViewById(R.id.s6));
                allImageViews.add((ImageView) parent.findViewById(R.id.s7));

                List<Product> products = items.get(i).getProductModel();
                if(products != null && products.size() > 0) {
                    int indexer = 0;

                    for (int v = 0; v < allImageViews.size(); v++) {

                        ImageView tempImageView = allImageViews.get(v);

                        if (tempImageView.getTag() == null) {
                            if (products.size() > indexer) {

                                Product tempProduct = products.get(indexer);
                                GoMobileApp.getmCacheManager().loadImageForDiscoverTab(Uri.parse(tempProduct.getThumbUrl()), tempImageView, null);
                                indexer += 1;
                            }

                        } else if (tempImageView.getTag().toString().equalsIgnoreCase("p")) {
                            GoMobileApp.getmCacheManager().loadImageForDiscoverTab(Uri.parse(items.get(i).getThumbUrl()), tempImageView, null);
                        }
                    }
                }


            }
        }
    }*/

    void setNextScrollingDynamicViews(View scrollingView) {
        nextScrollableViews.clear();

        nextScrollableViews.add(scrollingView.findViewById(R.id.s1));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s2));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s3));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s4));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s5));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s6));
        nextScrollableViews.add(scrollingView.findViewById(R.id.s7));
        nextScrollableViews.add(scrollingView.findViewById(R.id.lbl_all_product));

    }

    void reArrangeDynamicViews(Integer selectedIndex, List<View> views) {

        //re-arrange views
        for (int i = 0; i < views.size(); i++) {
            View tempV1 = views.get(i).findViewById(R.id.s1);
            View tempV2 = views.get(i).findViewById(R.id.s2);
            View tempV3 = views.get(i).findViewById(R.id.s3);
            View tempV4 = views.get(i).findViewById(R.id.s4);
            View tempV5 = views.get(i).findViewById(R.id.s5);
            View tempV6 = views.get(i).findViewById(R.id.s6);
            View tempV7 = views.get(i).findViewById(R.id.s7);
            View tempHdAllProducts = views.get(i).findViewById(R.id.lbl_all_product);

            if (i == selectedIndex) {

                currentOnScreenViews.clear();
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l1));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l2));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l3));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l4));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l5));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l6));
                currentOnScreenViews.add(views.get(i).findViewById(R.id.l7));

                for(int k = 0; k < currentOnScreenViews.size(); k ++){
                    currentOnScreenViews.get(k).setClickable(true);
                    registerClickEvents(currentOnScreenViews.get(k));
                }

                tempV1.setTranslationX(0);
                tempV2.setTranslationX(0);
                tempV3.setTranslationX(0);
                tempV4.setTranslationX(0);
                tempV5.setTranslationX(0);
                tempV6.setTranslationX(0);
                tempV7.setTranslationX(0);
                tempHdAllProducts.setTranslationX(0);

                tempV1.setTranslationY(0);
                tempV2.setTranslationY(0);
                tempV3.setTranslationY(0);
                tempV4.setTranslationY(0);
                tempV5.setTranslationY(0);
                tempV6.setTranslationY(0);
                tempV7.setTranslationY(0);
                tempHdAllProducts.setTranslationY(0);
            } else {

                List<View> viewsNotRequireClick = new ArrayList<>();
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l1));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l2));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l3));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l4));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l5));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l6));
                viewsNotRequireClick.add(views.get(i).findViewById(R.id.l7));

                for(int k = 0; k < viewsNotRequireClick.size(); k ++) {
                    viewsNotRequireClick.get(k).setOnClickListener(null);
                    viewsNotRequireClick.get(k).setClickable(false);
                }

                views.get(i).bringToFront();

                tempV1.setTranslationX(mBaseWidth * -1);
                tempV2.setTranslationX(mBaseWidth * -1);
                tempV3.setTranslationX(mBaseWidth * -1);
                tempV4.setTranslationX(mBaseWidth * -1);
                tempV5.setTranslationX(mBaseWidth * -1);
                tempV6.setTranslationX(mBaseWidth * -1);
                tempV7.setTranslationX(mBaseWidth * -1);

                tempV1.setTranslationY(mBaseHeight * -1);
                tempV2.setTranslationY(mBaseHeight * -1);
                tempV3.setTranslationY(mBaseHeight * -1);
                tempV4.setTranslationY(mBaseHeight * -1);
                tempV5.setTranslationY(mBaseHeight * -1);
                tempV6.setTranslationY(mBaseHeight * -1);
                tempV7.setTranslationY(mBaseHeight * -1);
                tempHdAllProducts.setTranslationY(mBaseHeight * -1);
                tempHdAllProducts.setAlpha(0);
            }
        }
    }

    /// SELECT AND DE-SELECT
    void registerClickEvents(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSelect) {
                    mLayoutRoot.setEnableSwipe(false);
                    select(view);
                    isSelect = true;

                    mSelectedView = view;
                } else {
                    deSelect(mSelectedView);
                    isSelect = false;
                    mLayoutRoot.setEnableSwipe(true);
                }
            }
        });
    }

}
