package com.mran.halo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;

import com.mran.haloringpulltorefresh.HaloRingRefreshLayout;

public class MainActivity extends AppCompatActivity {
    HaloRingRefreshLayout mHaloRingRefreshLayout;
    RecyclerView mRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHaloRingRefreshLayout = (HaloRingRefreshLayout) findViewById(R.id.haloring);
        mRecycleView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(new NormalRecyclerViewAdapter(this));

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
