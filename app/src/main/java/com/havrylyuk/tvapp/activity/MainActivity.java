package com.havrylyuk.tvapp.activity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.data.local.TvContract;
import com.havrylyuk.tvapp.dialog.SortChannelDialog;
import com.havrylyuk.tvapp.fragment.CategoryFragment;
import com.havrylyuk.tvapp.fragment.ChannelFragment;
import com.havrylyuk.tvapp.fragment.FavoriteFragment;
import com.havrylyuk.tvapp.fragment.ProgramsTabsFragment;
import com.havrylyuk.tvapp.sync.TvGuideSyncAdapter;
import com.havrylyuk.tvapp.util.ImageHelper;
import com.havrylyuk.tvapp.util.PreferencesHelper;
import com.havrylyuk.tvapp.util.Utility;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements
        NavigationView.OnNavigationItemSelectedListener,
        SyncStatusObserver,
        SortChannelDialog.onSortApplyListener,
        DatePickerDialog.OnDateSetListener,
        CategoryFragment.OnSelectCategoryListener,
        ChannelFragment.OnChangeFavoriteListener{

    private static final String DATE_PICKER_DIALOG_TAG = "com.havrylyuk.tvapp.DATE_PICKER_DIALOG_TAG";
    private static final String STATE_SELECTED_POSITION = "com.havrylyuk.tvapp.STATE_SELECTED_POSITION";
    private static final String STATE_SYNC_ACTIVE = "com.havrylyuk.tvapp.STATE_SYNC_ACTIVE";;

    private ProgressBar progressBar;
    private Object syncMonitor;//check sync status
    private PreferencesHelper preferencesHelper;
    private DatePickerDialog datePickerDialog;
    private FloatingActionButton selectDateButton;
    private int currentItem = R.id.nav_home;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView lastSyncDateView;
    private boolean isSyncActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferencesHelper = PreferencesHelper.getInstance();
        setupToolBar();
        setupCalendarFab();
        setupDrawerLayout();
        if (savedInstanceState != null) {
            currentItem = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            isSyncActive = savedInstanceState.getBoolean(STATE_SYNC_ACTIVE);
        }
        navigationView.getMenu().performIdentifierAction(currentItem, 0);//default main fragment
        TvGuideSyncAdapter.initializeSyncAdapter(this);//sync data now
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_POSITION, currentItem);
        outState.putBoolean(STATE_SYNC_ACTIVE, isSyncActive);
        super.onSaveInstanceState(outState);
    }

    private void setupDrawerLayout() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_drawer_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        lastSyncDateView = (TextView) header.findViewById(R.id.text_view_last_sync);
        NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
           if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
          }
    }

    private void setupToolBar() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        updateChannelView();
    }

    private void setupCalendarFab() {
        selectDateButton = (FloatingActionButton) findViewById(R.id.fab_date_picker);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar currentDate = Calendar.getInstance();
                final Calendar futureDate = Calendar.getInstance();
                datePickerDialog = DatePickerDialog.newInstance( MainActivity.this,
                        currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setFirstDayOfWeek(Calendar.MONDAY);
                datePickerDialog.showYearPickerFirst(false);
                datePickerDialog.setMinDate(currentDate);
                int countOfDays = Integer.parseInt(preferencesHelper
                        .getScheduleDaysCount(getString(R.string.pref_schedule_days_count_key)));
                futureDate.add(Calendar.DAY_OF_YEAR, countOfDays - 1);
                datePickerDialog.setMaxDate(futureDate);
                datePickerDialog.setTitle(getString(R.string.date_picker_dialog_title));
                datePickerDialog.show(getFragmentManager(), DATE_PICKER_DIALOG_TAG);
            }
        });

    }

    private void updateChannelView() {
        long date = preferencesHelper.getCurProgramsDate(getString(R.string.pref_program_date_key));
        SimpleDateFormat format = new SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault());
        updateChannelView(format.format(date));
    }

    public void updateChannelView(String title) {
        if (title != null) {
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(title);
            } else if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
        }
    }

    private void setSelectDateButtonVisible(boolean isVisible) {
        if (selectDateButton != null) {
            selectDateButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
    }

    @Override
    protected void onPause() {
        ContentResolver.removeStatusChangeListener(syncMonitor);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncMonitor = ContentResolver.addStatusChangeListener(
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE | ContentResolver.SYNC_OBSERVER_TYPE_PENDING, this);
        onStatusChanged(1);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_category:
                switchToFragment(id, new CategoryFragment(), CategoryFragment.CATEGORIES_FRAGMENT_TAG);
                break;
            case R.id.nav_chanel:
                switchToFragment(id, new ChannelFragment(), ChannelFragment.CHANNELS_FRAGMENT_TAG);
                break;
            case R.id.nav_prefered:
                switchToFragment(id, new FavoriteFragment(), FavoriteFragment.FAVORITES_FRAGMENT_TAG);
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_sort:
                DialogFragment sortDialog = new SortChannelDialog();
                sortDialog.show(getSupportFragmentManager().beginTransaction(), SortChannelDialog.SORT_DIALOG_TAG);
                break;
            case R.id.nav_sync:
                TvGuideSyncAdapter.syncImmediately(this);
                break;
            case R.id.nav_home:
                switchToFragment(id, new ProgramsTabsFragment(), ProgramsTabsFragment.MAIN_FRAGMENT_TAG);
                updateChannelView();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchToFragment(int itemIid, Fragment fragment, String tag) {
        currentItem = itemIid;
        setSelectDateButtonVisible(fragment instanceof ProgramsTabsFragment);
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack();
        fm.beginTransaction().replace(R.id.frame_container, fragment, tag).commit();
    }

    @Override
    public void onStatusChanged(int which) {
        onSyncStatusChanged(ContentResolver.isSyncActive(TvGuideSyncAdapter.getSyncAccount(this),
                getResources().getString(R.string.content_authority)));
    }

    private void onSyncStatusChanged(final boolean isSyncing) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null) {
                    progressBar.setVisibility(isSyncing ? View.VISIBLE : View.GONE);
                }
                if (lastSyncDateView != null) {
                    long lsatSyncDate =  Utility.getLastSyncTime(MainActivity.this);
                    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                    lastSyncDateView.setText(getString(R.string.format_last_sync, format.format(lsatSyncDate)));
                }
               if (!isSyncActive && isSyncing) {
                   if (Utility.isNotDuplicateSync(MainActivity.this)) blueToast(getString(R.string.notify_sync_start));
               } else if (isSyncActive && !isSyncing){
                   if (Utility.isNotDuplicateSync(MainActivity.this)) blueToast(getString(R.string.sync_success));
               }
               isSyncActive = isSyncing;
               getContentResolver().notifyChange(TvContract.ChannelEntry.CONTENT_URI, null);
            }
        });
    }

    @Override
    public void changeSortType(int position, boolean isDesc) {
        String[] values = getResources().getStringArray(Utility.getChannelSortValues(isDesc));
        ProgramsTabsFragment mainFragment = (ProgramsTabsFragment) getSupportFragmentManager()
                .findFragmentByTag(ProgramsTabsFragment.MAIN_FRAGMENT_TAG);
        if (mainFragment != null) {
            mainFragment.sortChannels(values[position]);
        }
        ChannelFragment channelFragment = (ChannelFragment) getSupportFragmentManager()
                .findFragmentByTag(ChannelFragment.CHANNELS_FRAGMENT_TAG);
        if (channelFragment != null) {
            channelFragment.sortChannels(values[position]);
        }
        preferencesHelper.setChannelSortType(getString(R.string.pref_sort_channel_key), values[position] );
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear,
                          int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date date = calendar.getTime();
        preferencesHelper.setCurProgramsDate(getString(R.string.pref_program_date_key), date.getTime());
        getContentResolver().notifyChange(TvContract.ChannelEntry.CONTENT_URI, null);
        updateChannelView();
    }

    @Override
    public void onCategoryClick(long id) {
        setSelectDateButtonVisible(false);
        Bundle arguments = new Bundle();
        arguments.putLong(ChannelFragment.EXTRA_CATEGORY_ID, id);
        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setArguments(arguments);
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, channelFragment, ChannelFragment.CHANNELS_FRAGMENT_TAG)
                .commit();
    }

    private void blueToast(String message) {
        final SpannableString spannableString = new SpannableString(message);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)),
                0, spannableString.length(), 0);
        Toast.makeText(this,spannableString,Toast.LENGTH_LONG).show();
    }

    public void setChannelLogo(String url) {
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        if (imageView != null) {
            ImageHelper.load(url, imageView);
        }
    }

    public void setChannelLogo(int resId) {
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        if (imageView != null) {
            ImageHelper.load(resId, imageView);
        }
    }

    @Override
    public void onChangeFavoriteState(long id, boolean value) {
        ContentValues cv = new ContentValues();
        cv.put(TvContract.ChannelEntry.COLUMN_CHANNEL_FAVORITE, value ? 0 : 1);
        getContentResolver().update(TvContract.ChannelEntry.CONTENT_URI, cv,
                TvContract.ChannelEntry.TABLE_NAME + "." + TvContract.ChannelEntry._ID + "=? ",
                new String[]{String.valueOf(id)});
    }

}
