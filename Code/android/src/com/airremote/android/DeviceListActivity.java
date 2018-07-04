package com.airremote.android;

import com.airremote.android.pagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DeviceListActivity extends BaseActivity {
    public static final String TAG = "DeviceListActivity";

    private final int CLOUMN_SPACING = 10;
    private final int ITEM_WIDTH = 90;
    private final int ITEM_HEIGHT = 90;

    private final int TotalDeviceNum = 35;

    private ViewPager mViewPager;
    private ArrayList<View> mPageViews;
    private ImageView mImageView;
    private ImageView[] mImageViews;

    private ViewGroup mainViewGroup;
    private ViewGroup indicatorViewGroup;
    private CirclePageIndicator mIndicator;

    private ArrayList<GridView> mDeviceGridViews;
    private ArrayList<HashMap<String, Object>> menuList = new ArrayList<HashMap<String, Object>>();

    private LayoutInflater mInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mInflater = getLayoutInflater();

        mainViewGroup = (ViewGroup) mInflater.inflate(R.layout.device_list, null);

        setContentView(mainViewGroup);

        mainViewGroup.post(new Runnable() {
            @Override
            public void run() {

               Rect outRect = new Rect();
               mainViewGroup.getDrawingRect(outRect);
               int viewHeight = outRect.height();
               int viewWidth = outRect.width();
               DisplayMetrics metrics = new DisplayMetrics();
               getWindowManager().getDefaultDisplay().getMetrics(metrics);
               int viewWidthDp = (int)(viewWidth / metrics.density);
               int viewHeightDp = (int)(viewHeight);

               int colNum = (viewWidthDp - CLOUMN_SPACING) / (ITEM_WIDTH + CLOUMN_SPACING);
               int gridViewItemWidth = (int) ((viewWidthDp - (colNum+1) * CLOUMN_SPACING) / colNum);

               int rowNum = (viewHeightDp - CLOUMN_SPACING) / (ITEM_HEIGHT + CLOUMN_SPACING);

               int itemNumOnePage = colNum * rowNum;
               int pageNum = (TotalDeviceNum) / itemNumOnePage;
               if (pageNum % TotalDeviceNum > 0) {
                   pageNum += 1;
               }

               for (int i = 0; i < colNum * rowNum; i++) {
                   HashMap<String, Object> map = new HashMap<String, Object>();
                   map.put("ItemImage", R.drawable.ic_launcher);
                   map.put("ItemText", "" + i);
                   menuList.add(map);
               }

               SimpleAdapter saItem = new SimpleAdapter(DeviceListActivity.this, menuList, R.layout.device_list_item,
                       new String[] {"ItemImage", "ItemText"},
                       new int[] { R.id.device_item_iv, R.id.device_item_tv });

               mPageViews = new ArrayList<View>();
               for (int i = 0; i < pageNum; i++) {
                   GridView v = (GridView) mInflater.inflate(R.layout.device_list_grid_view, null);
                   v.setColumnWidth(gridViewItemWidth);
                   v.setNumColumns(colNum);
                   v.setAdapter(saItem);
                   v.setOnItemClickListener(new OnItemClickListener() {
                      public void onItemClick(AdapterView<?> argv0, View arg1, int arg2, long arg3) {
                          int index = arg2;
                          Toast.makeText(getApplicationContext(), "You presse item " + index, Toast.LENGTH_SHORT).show();
                      }
                   });
                   mPageViews.add(v);
               }

               mImageViews = new ImageView[mPageViews.size()];

               mViewPager = (ViewPager) mainViewGroup.findViewById(R.id.viewpager);
               mViewPager.setAdapter(new DeviceListPagerAdapter());

               mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
               mIndicator.setViewPager(mViewPager);
           }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class DeviceListPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mPageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mPageViews.get(arg1));
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mPageViews.get(arg1));
            return mPageViews.get(arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {

        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {

        }

        @Override
        public void finishUpdate(View arg0) {

        }
    }
}