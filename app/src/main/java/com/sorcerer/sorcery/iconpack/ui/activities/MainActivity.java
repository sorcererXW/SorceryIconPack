package com.sorcerer.sorcery.iconpack.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.quinny898.library.persistentsearch.SearchResult;
import com.sorcerer.sorcery.iconpack.BuildConfig;
import com.sorcerer.sorcery.iconpack.R;
import com.sorcerer.sorcery.iconpack.models.PermissionBean;
import com.sorcerer.sorcery.iconpack.ui.activities.base.BaseActivity;
import com.sorcerer.sorcery.iconpack.ui.adapters.ViewPageAdapter;
import com.sorcerer.sorcery.iconpack.ui.adapters.recyclerviewAdapter.PermissionAdapter;
import com.sorcerer.sorcery.iconpack.ui.fragments.LazyIconFragment;
import com.sorcerer.sorcery.iconpack.ui.views.DoubleTapTabLayout;
import com.sorcerer.sorcery.iconpack.ui.views.SearchBox;
import com.sorcerer.sorcery.iconpack.util.DisplayUtil;
import com.sorcerer.sorcery.iconpack.util.PermissionsHelper;
import com.sorcerer.sorcery.iconpack.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Sorcerer on 2016/6/1 0001.
 * <p/>
 * MainActivity
 * The first activity with drawer and icon viewpager.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mMainToolbar;

    @BindView(R.id.appBarLayout_main)
    AppBarLayout mAppBarLayout;

    @BindView(R.id.viewPager_icon)
    ViewPager mViewPager;

    @BindView(R.id.searchBox_main_icon)
    SearchBox mSearchBox;

    @BindView(R.id.tabLayout_icon)
    DoubleTapTabLayout mTabLayout;

    @BindView(R.id.coordinatorLayout_main)
    CoordinatorLayout mCoordinatorLayout;

    private Drawer mDrawer;

    private static final String TAG = "TAG_MAIN_ACTIVITY";

    public static final int REQUEST_ICON_DIALOG = 100;

    public static Intent mLaunchIntent;
    private ViewPageAdapter mPageAdapter;
    private boolean mCustomPicker = false;
    private ViewPager.OnPageChangeListener mPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                private int times = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                    if (position != mViewPager.getCurrentItem()) {
                        closeSearch();
                    }

                    if (position == 0 && positionOffset == 0 && positionOffsetPixels == 0) {
                        times++;
                        if (times >= 3) {
                            openDrawer();
                        }
                    }
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        times = 0;
                    }
                }
            };

    private SearchBox.SearchListener mSearchListener = new SearchBox.SearchListener() {
        @Override
        public void onSearchOpened() {
            mAppBarLayout.setExpanded(true);
        }

        @Override
        public void onSearchCleared() {
        }

        @Override
        public void onSearchClosed() {
            mSearchBox.clearResults();
        }

        @Override
        public void onSearchTermChanged(String term) {
            ((LazyIconFragment) mPageAdapter.getItem(mViewPager.getCurrentItem()))
                    .showWithString(term.toLowerCase());
        }

        @Override
        public void onSearch(String result) {

        }

        @Override
        public void onResultClick(SearchResult result) {

        }
    };

    @Override
    protected int provideLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        Log.d(TAG, "onCreate");
        mLaunchIntent = getIntent();
        String action = getIntent().getAction();

        mCustomPicker = action.equals("com.novalauncher.THEME");

        setSupportActionBar(mMainToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initTabAndPager();

        initSearchBox();

        if (!mCustomPicker) {
            initDrawer();
            showPermissionDialog();
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(getString(R.string.select_an_icon));
        }
    }

    private void showPermissionDialog() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!PermissionsHelper.hasPermissions(this,
                    new String[]{PermissionsHelper.READ_PHONE_STATE_MANIFEST,
                            PermissionsHelper.WRITE_EXTERNAL_STORAGE_MANIFEST})) {
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
                builder.title(ResourceUtil.getString(this, R.string.permission_request_title));
                List<PermissionBean> list = new ArrayList<>();
                list.add(new PermissionBean(null,
                        ResourceUtil.getString(this, R.string.permission_request_content), 0));
                if (!PermissionsHelper
                        .hasPermission(this, PermissionsHelper.READ_PHONE_STATE_MANIFEST)) {
                    list.add(new PermissionBean(
                            ResourceUtil.getString(this, R.string.permission_read_phone_state),
                            ResourceUtil
                                    .getString(this,
                                            R.string.permission_request_read_phone_state),
                            R.drawable.ic_smartphone_black_24dp));
                }
                if (!PermissionsHelper
                        .hasPermission(this,
                                PermissionsHelper.WRITE_EXTERNAL_STORAGE_MANIFEST)) {
                    list.add(new PermissionBean(
                            ResourceUtil
                                    .getString(this,
                                            R.string.permission_write_external_storage),
                            ResourceUtil.getString(this,
                                    R.string.permission_request_describe_write_external_storage),
                            R.drawable.ic_folder_black_24dp));
                }
                builder.adapter(new PermissionAdapter(this, list),
                        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                                        @NonNull DialogAction dialogAction) {
                        PermissionsHelper.requestPermissions(mActivity,
                                new String[]{PermissionsHelper.READ_PHONE_STATE_MANIFEST,
                                        PermissionsHelper.WRITE_EXTERNAL_STORAGE_MANIFEST});
                        materialDialog.dismiss();
                    }
                });
                builder.positiveText(
                        ResourceUtil.getString(this, R.string.action_grant_permission));
                builder.negativeText(ResourceUtil.getString(this, R.string.action_refuse));
                builder.canceledOnTouchOutside(false);
                builder.build().show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initTabAndPager() {
        mViewPager.setOffscreenPageLimit(1);

        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mViewPager.setPageMargin(DisplayUtil.dip2px(mContext, 16));

        mPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        generateFragments(mPageAdapter);
        mViewPager.setAdapter(mPageAdapter);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setOnTabDoubleTapListener(new DoubleTapTabLayout.OnTabDoubleTapListener() {
            @Override
            public void onDoubleTap() {
                int index = mViewPager.getCurrentItem();
                ViewPageAdapter adapter = (ViewPageAdapter) mViewPager.getAdapter();
                LazyIconFragment fragment = (LazyIconFragment) adapter.getItem(index);
                fragment.getRecyclerView().smoothScrollToPosition(0);
            }
        });

    }

    private void initSearchBox() {
        mSearchBox.setLogoText("Sorcery Icons");
        mSearchBox.setHint(ResourceUtil.getString(mContext, R.string.search_hint));
        mSearchBox.setSearchListener(mSearchListener);
        mSearchBox.setSearchWithoutSuggestions(true);
        mSearchBox.setMenuListener(new SearchBox.MenuListener() {
            @Override
            public void onMenuClick() {
                openDrawer();
            }
        });
        Typeface typeface = Typeface.createFromAsset(getAssets(), "RockwellStd.otf");
        mSearchBox.setLogoTypeface(typeface);
        mSearchBox.setAnimateDrawerLogo(true);
    }

    private void initDrawer() {
        mDrawer = new DrawerBuilder()
                .withCloseOnClick(true)
                .withAccountHeader(new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeightDp(178)
                        .withHeaderBackground(R.drawable.sandy_shore)
                        .withHeaderBackgroundScaleType(ImageView.ScaleType.CENTER_CROP)
                        .withProfileImagesClickable(false)
                        .withResetDrawerOnProfileListClick(false)
                        .withSelectionListEnabled(false)
                        .withSelectionListEnabledForSingleProfile(false)
                        .withDividerBelowHeader(false)
                        .build())
                .withActivity(mActivity)
                .build();
        mDrawer.addItems(
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("apply")
                        .withIcon(ResourceUtil.getDrawableWithAlpha(mContext, R.drawable
                                .ic_input_black_24dp, 128))
                        .withName(R.string.nav_item_apply),
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("feedback")
                        .withIcon(ResourceUtil.getDrawableWithAlpha(mContext, R.drawable
                                .ic_mail_black_24dp, 128))
                        .withName(R.string.nav_item_feedback),
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("lab")
                        .withIcon(ResourceUtil.getDrawableWithAlpha(mContext, R.drawable
                                .ic_settings_black_24dp, 128))
                        .withName(R.string.nav_item_lab),
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("help")
                        .withIcon(ResourceUtil.getDrawableWithAlpha(mContext, R.drawable
                                .ic_help_black_24dp, 128))
                        .withName(R.string.nav_item_help),
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("donate")
                        .withIcon(ResourceUtil.getDrawableWithAlpha(mContext, R.drawable
                                .ic_attach_money_black_24dp, 128))
                        .withName(R.string.nav_item_donate),
                new DividerDrawerItem(),
                new PrimaryDrawerItem()
                        .withSetSelected(false)
                        .withSelectable(false)
                        .withTag("about")
                        .withName(R.string.nav_item_about)
        );
        if (BuildConfig.DEBUG) {
            mDrawer.addItem(new PrimaryDrawerItem().withSelectable(false).withTag("DEBUG")
                    .withName("DEBUG"));
        }

        mDrawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch ((String) drawerItem.getTag()) {
                    case "apply":
                        activityShift(ApplyActivity.class);
                        break;
                    case "feedback":
                        activityShift(FeedbackActivity.class);
                        break;
                    case "lab":
                        activityShift(LabActivity.class);
                        break;
                    case "help":
                        activityShift(HelpActivity.class);
                        break;
                    case "donate":
                        activityShift(DonateActivity.class);
                        break;
                    case "about":
                        Intent intent = new Intent(mContext, AboutDialogActivity.class);
                        mContext.startActivity(intent);
                        mActivity.overridePendingTransition(R.anim.fast_fade_in, 0);
                        break;
                }
                return false;
            }
        });
    }

    private void generateFragments(ViewPageAdapter adapter) {
        String[] name = getResources().getStringArray(R.array.tab_name);
        LazyIconFragment.Flag[] flag = LazyIconFragment.Flag.values();

        Log.d(TAG, Arrays.toString(flag));

        for (int i = 0; i < flag.length; i++) {
            adapter.addFragment(LazyIconFragment.newInstance(flag[i], mCustomPicker), name[i]);
        }
    }

    private void activityShift(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_in, android.R.anim.fade_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!closeDrawer()) {
            super.onBackPressed();
        }
    }

    private void closeSearch() {
        mSearchBox.closeSearch();
    }

    private void openDrawer() {
        if (mDrawer != null) {
            mDrawer.openDrawer();
        }
    }

    private boolean closeDrawer() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
            return true;
        }
        return false;
    }
}
