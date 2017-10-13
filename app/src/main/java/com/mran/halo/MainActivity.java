package com.mran.halo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.mran.haloringpulltorefresh.HaloRingRefreshLayout;

public class MainActivity extends AppCompatActivity {
    HaloRingRefreshLayout mHaloRingRefreshLayout;
    RecyclerView mRecycleView;
    NormalRecyclerViewAdapter mNormalRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHaloRingRefreshLayout = (HaloRingRefreshLayout) findViewById(R.id.haloring);
        mRecycleView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mNormalRecyclerViewAdapter = new NormalRecyclerViewAdapter(this);
        mNormalRecyclerViewAdapter.addListener(new NormalRecyclerViewAdapter.OnRecycleItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("MainActivity", "onItemClick: " + ((TextView)view).getText() + position);
                mHaloRingRefreshLayout.setEnableRefresh(!mHaloRingRefreshLayout.isEnableRefresh());
            }
        });
        mRecycleView.setAdapter(mNormalRecyclerViewAdapter);
        mHaloRingRefreshLayout.setEnableRefresh(false);//设置能否刷新
        mHaloRingRefreshLayout.stopRefresh();//取消刷新
        mHaloRingRefreshLayout.isRefreshing();//是否正在刷新
        mHaloRingRefreshLayout.isBeingDragged();//是否在拖动
        mHaloRingRefreshLayout.isPullEnd();//是否拖动到底部
        mHaloRingRefreshLayout.getPercent();//移动距离和可移动距离的百分比
        mHaloRingRefreshLayout.getMaxDragDistance();//可移动的最大距离

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("MainActivity", "onKeyDown: ");
        if (mHaloRingRefreshLayout.isRefreshing()) {
            mHaloRingRefreshLayout.stopRefresh();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
