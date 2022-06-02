package xyz.lns103.bookkeeping.fragment;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.material.snackbar.Snackbar;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import xyz.lns103.bookkeeping.CloudSync;
import xyz.lns103.bookkeeping.activity.AddItemActivity;
import xyz.lns103.bookkeeping.bean.Bill;
import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.adapter.ItemListAdapter;
import xyz.lns103.bookkeeping.databinding.FragmentFilterBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterFragment extends Fragment {

    public List<Bill> mBills = new ArrayList<>();
    //private View rootView;
    FragmentFilterBinding binding;
    private ItemListAdapter itemListAdapter;
    private RecyclerView recyclerView;
    boolean firstOpen = true;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Calendar date1;
    private Calendar date2;


    public FilterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FilterFragment newInstance() {
        FilterFragment fragment = new FilterFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        sharedPreferences =  getContext().getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        String userID = sharedPreferences.getString("user_id","");
        String password = sharedPreferences.getString("password","");
        if(savedInstanceState!=null) onBackFragment();
    }

    private void onBackFragment() {
        getFilterFragmentListener.onBack(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater,container,false);
        if(savedInstanceState!=null){
            firstOpen = false;
            mBills = (List<Bill>) savedInstanceState.getSerializable("bills");
        }
        initBills();
        initRecyclerView();
        setListener();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();//滚动到顶部
        if(firstOpen) {
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.scrollToPosition(0);
                    firstOpen = false;
                }
            });
        }
    }

    private void setListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {//延迟消失刷新图标
                refreshList();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void initBills() {
        mBills = CloudSync.getLocalData(date1,date2);
    }

    private void initRecyclerView() {
        recyclerView = binding.recyclerView;
        //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        int spanCount = getSpanCount();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),spanCount);
        //if(isLandscape()) gridLayoutManager.setSpanCount(2);
        recyclerView.setLayoutManager(gridLayoutManager);
        itemListAdapter = new ItemListAdapter(mBills,getContext());
        recyclerView.setAdapter(itemListAdapter);
        itemListAdapter.setClickable(false);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void refreshList(){
        mBills = CloudSync.getLocalData(date1,date2);
        itemListAdapter.setBills(mBills);
        itemListAdapter.notifyDataSetChanged();
    }

    private int getSpanCount() {
        WindowManager manager = requireActivity().getWindowManager();

        DisplayMetrics outMetrics = new DisplayMetrics();

        manager.getDefaultDisplay().getMetrics(outMetrics);
        float density = outMetrics.density;
        int width = outMetrics.widthPixels;
        int spanCount = (int) (width / density /290);
        Log.e(TAG, "width: "+width +" "+density+" "+spanCount);
        if(spanCount==0) spanCount=1;
        return spanCount;

    }

    public boolean isLandscape(){
        Configuration mConfiguration;
        mConfiguration = getActivity().getResources().getConfiguration();
        int ori = mConfiguration.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            return true;
        } else {
            return false;
        }
    }

    public void setDate(Calendar date1,Calendar date2) {
        this.date1 = date1;
        this.date2 = date2;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bills",(Serializable) itemListAdapter.getBills());
    }

    private static GetFilterFragmentListener getFilterFragmentListener;

    public interface GetFilterFragmentListener{
        void onBack(FilterFragment filterFragment);
    }

    public static void setGetFilterFragmentListener(GetFilterFragmentListener listener){
        getFilterFragmentListener = listener;
    }
}