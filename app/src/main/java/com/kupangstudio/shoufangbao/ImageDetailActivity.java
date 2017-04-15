package com.kupangstudio.shoufangbao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bigkoo.convenientbanner.CBPageAdapter;
import com.bigkoo.convenientbanner.CBViewHolderCreator;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageDetailActivity extends Activity {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ConvenientBanner turnImg;
    private List<String> bannerList;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.empty_building_list)
                .showImageForEmptyUri(R.drawable.empty_building_list)
                .showImageOnFail(R.drawable.empty_building_list)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
                .build();
        bannerList = new ArrayList<String>();
        Intent intent = getIntent();
        bannerList = intent.getStringArrayListExtra("url");
        position = intent.getIntExtra("position", 0);
        turnImg = (ConvenientBanner) findViewById(R.id.scene_image_detail);
        if (bannerList != null && bannerList.size() > 0) {
            turnImg.setPages(new CBViewHolderCreator<ImageHolderView>() {
                @Override
                public ImageHolderView createHolder() {
                    return new ImageHolderView();
                }
            }, bannerList).setPageIndicator(new int[]{R.drawable.banner_unselected, R.drawable.banner_selected});
        }
        turnImg.setcurrentitem(position);
    }

    private class ImageHolderView implements CBPageAdapter.Holder<String> {
        private PhotoView imageView;

        @Override
        public View createView(Context context) {
            imageView = new PhotoView(context);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, final int position, final String data) {
            /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.
                    MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);*/
            imageView.setImageResource(R.drawable.empty_building_list);
            imageLoader.displayImage(data, imageView);
            imageView.setOnPhotoTapListener(
                    new PhotoViewAttacher.OnPhotoTapListener() {
                        @Override
                        public void onPhotoTap(View view, float x, float y) {
                            ImageDetailActivity.this.finish();
                        }
                    }
            );
        }
    }

}
