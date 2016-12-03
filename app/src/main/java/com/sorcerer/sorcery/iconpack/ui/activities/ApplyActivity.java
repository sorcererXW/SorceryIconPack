package com.sorcerer.sorcery.iconpack.ui.activities;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;

import com.sorcerer.sorcery.iconpack.R;
import com.sorcerer.sorcery.iconpack.ui.activities.base.SlideInAndOutAppCompatActivity;
import com.sorcerer.sorcery.iconpack.ui.adapters.recyclerviewAdapter.ApplyAdapter;
import com.sorcerer.sorcery.iconpack.utils.AppInfoUtil;
import com.sorcerer.sorcery.iconpack.utils.LauncherIntents;

import butterknife.BindView;

public class ApplyActivity extends SlideInAndOutAppCompatActivity {
    @BindView(R.id.recyclerView_apply)
    RecyclerView mRecyclerView;

    private GridLayoutManager mGridLayoutManager;

    private ApplyAdapter mAdapter;

    @Override
    protected int provideLayoutId() {
        return R.layout.activity_apply;
    }

    @Override
    protected void init() {
        super.init();

        setToolbarBackIndicator();

        mGridLayoutManager = new GridLayoutManager(this, 2);
        mGridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        mGridLayoutManager.scrollToPosition(0);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(false);

        mAdapter =
                new ApplyAdapter(this, AppInfoUtil.generateLauncherInfo(this), calcNumOfRows());
        mAdapter.setOnItemClickListener(new ApplyAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mAdapter.getItem(position).isInstalled()) {
                    new LauncherIntents(mContext, mAdapter.getItem(position).getLabel());
                } else {
                    final String appPackageName =
                            mAdapter.getItem(position).getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id="
                                        + appPackageName)));
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            super.onBackPressed();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resize();
    }

    private void resize() {
        mGridLayoutManager.setSpanCount(calcNumOfRows());
        mGridLayoutManager.requestLayout();
        mAdapter.changeSpan(calcNumOfRows());
    }

    private int calcNumOfRows() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float s = getResources().getDimension(R.dimen.apply_item_size);
        return Math.max(1, (int) (size.x / s));
    }

}
