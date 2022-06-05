package xyz.lns103.bookkeeping.activity;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Locale;

import xyz.lns103.bookkeeping.CloudSync;
import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.adapter.ViewPagerAdapter;
import xyz.lns103.bookkeeping.databinding.ActivityMainBinding;
import xyz.lns103.bookkeeping.fragment.FilterFragment;
import xyz.lns103.bookkeeping.fragment.ItemListFragment;
import xyz.lns103.bookkeeping.fragment.SettingFragment;
import xyz.lns103.bookkeeping.fragment.StatisticsFragment;


public class MainActivity extends BaseActivity {

    ViewPager2 viewPager;
    MenuItem menuItem;
    private ActivityMainBinding binding;
    ItemListFragment itemListFragment;
    FilterFragment filterFragment;
    StatisticsFragment statisticsFragment;
    long time;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CloudSync cloudSync;
    private Calendar date1;
    private Calendar date2;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setOnBackListener();
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBar);
        setOnBackCloudSyncListener();
        sharedPreferences =  getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        //checkSync();
        syncAll();
        initFragment();
        initViewPager();
        binding.appBarTransparentBackground.bringToFront();
        binding.appBarLayout.bringToFront();
        //binding.appBar.bringToFront();
        time = System.currentTimeMillis();
        FloatingActionButton floatingActionButton = findViewById(R.id.floatButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemListFragment.addBillForResult();
            }
        });
    }

    private void checkSync() {
        if(CloudSync.syncNeeded()) syncAll();
    }

    private void initFragment() {
        if(itemListFragment==null)
            itemListFragment = ItemListFragment.newInstance();
        if(filterFragment==null)
            filterFragment = FilterFragment.newInstance();
        if(statisticsFragment==null)
            statisticsFragment = StatisticsFragment.newInstance();
    }

    @SuppressLint("RestrictedApi")
    public void initViewPager(){
        viewPager = findViewById(R.id.viewpager);
        ArrayList<Fragment> fragments = new ArrayList<>();
        //Log.e(TAG, "initViewPager: " );
        fragments.add(itemListFragment);
        fragments.add(filterFragment);
        fragments.add(statisticsFragment);
        fragments.add(SettingFragment.newInstance());
//        fragments.add(BlankFragment.newInstance("pager5"));
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),getLifecycle(),fragments);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        BottomNavigationView navView;
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) binding.nav.getLayoutParams();
        HideBottomViewOnScrollBehavior hideBottomViewOnScrollBehavior = (HideBottomViewOnScrollBehavior) layoutParams.getBehavior();
        binding.nav.setItemHorizontalTranslationEnabled(true);
        binding.nav.setOnItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.nav_item_1:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.nav_item_2:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.nav_item_3:
                            viewPager.setCurrentItem(2);
                            break;
                        case R.id.nav_item_4:
                            viewPager.setCurrentItem(3);
                            break;
//                        case R.id.nav_item_5:
//                            viewPager.setCurrentItem(4);
//                            break;
                    }
                    return false;
                });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();//控制菜单显隐
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    binding.nav.getMenu().getItem(0).setChecked(false);
                }
                menuItem = binding.nav.getMenu().getItem(position);
                menuItem.setChecked(true);
                if(position==0) {
                    binding.floatButton.show();
                }
                else {
                    binding.floatButton.hide();
                    binding.appBar.hideOverflowMenu();
                }
                if(position==0||position==3){
                    binding.dateSelectBar.setVisibility(View.GONE);
                }else{
                    binding.dateSelectBar.setVisibility(View.VISIBLE);
                }
                hideBottomViewOnScrollBehavior.slideUp(binding.nav);//显示导航栏
            }
        });

        binding.dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date1 = null;
                date2 = null;
                binding.date1.setText("选择日期");
                binding.date2.setText("选择日期");
                binding.dateButton.setImageResource(R.drawable.ic_calendar_month_24);
                refreshTime();
            }
        });

        binding.date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date1==null)date1 = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        DatePicker1Listener,
                        date1.get(Calendar.YEAR),
                        date1.get(Calendar.MONTH),
                        date1.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        binding.date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date2==null) date2 = Calendar.getInstance();

                MaterialStyledDatePickerDialog datePickerDialog = new MaterialStyledDatePickerDialog(MainActivity.this,
                        DatePicker2Listener,
                        date2.get(Calendar.YEAR),
                        date2.get(Calendar.MONTH),
                        date2.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener DatePicker1Listener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            date1.set(i,i1,i2,0,0,0);
            SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            binding.date1.setText(format.format(date1));
            refreshTime();
        }
    };

    private MaterialStyledDatePickerDialog.OnDateSetListener DatePicker2Listener = new MaterialStyledDatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            date2.set(i,i1,i2,0,0,0);
            SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
            binding.date2.setText(format.format(date2));
            refreshTime();
        }
    };

    private void refreshTime() {
        if(date1!=null && date2!=null){
            binding.dateButton.setImageResource(R.drawable.ic_clear_all_24);
        }
        filterFragment.setDate(date1,date2);
        filterFragment.refreshList();
        statisticsFragment.setDate(date1,date2);
        statisticsFragment.fresh();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        switch (viewPager.getCurrentItem()){
            case 0 :
                menu.findItem(R.id.cloudSync).setVisible(true);
                break;
            default:
                menu.findItem(R.id.cloudSync).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.cloudSync:
                syncAll();
                break;
        }
        return true;
    }

    public void syncAll(){
        String userID = sharedPreferences.getString("user_id","");
        String password = sharedPreferences.getString("password","");
        cloudSync = new CloudSync(userID,password);
        cloudSync.syncAll();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_appbar_menu, menu);
        //返回true代表普通菜单显示
        return true;
    }

    public void setOnBackListener() {
        ItemListFragment.setGetItemListFragmentListener(new ItemListFragment.GetItemListFragmentListener() {
            @Override
            public void onBack(ItemListFragment frag) {
                itemListFragment = frag;
            }
        });
        FilterFragment.setGetFilterFragmentListener(new FilterFragment.GetFilterFragmentListener() {
            @Override
            public void onBack(FilterFragment frag) {
               filterFragment = frag;
            }
        });
        StatisticsFragment.setGetStatisticsFragmentListener(new StatisticsFragment.GetStatisticsFragmentListener() {
            @Override
            public void onBack(StatisticsFragment frag) {
                statisticsFragment = frag;
            }
        });
    }

    private void setOnBackCloudSyncListener(){
        CloudSync.setGetSyncListener(new CloudSync.GetSyncListener() {
            @Override
            public void onBackMsg(String msg) {
                Snackbar.make(binding.getRoot(),msg,Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onBackSync() {
                itemListFragment.refreshList();
                filterFragment.refreshList();
                statisticsFragment.fresh();
            }
        });
    }
}