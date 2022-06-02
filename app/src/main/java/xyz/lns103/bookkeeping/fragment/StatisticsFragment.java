package xyz.lns103.bookkeeping.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;
import java.util.Locale;

import xyz.lns103.bookkeeping.CloudSync;
import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.bean.Bill;
import xyz.lns103.bookkeeping.databinding.FragmentSettingBinding;
import xyz.lns103.bookkeeping.databinding.FragmentStatisticsBinding;


public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private Calendar date1;
    private Calendar date2;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        if(savedInstanceState!=null) getStatisticsFragmentListener.onBack(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsBinding.inflate(inflater,container,false);
        fresh();
        setListener();
        return binding.getRoot();
    }

    private void setListener() {
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {//延迟消失刷新图标
                fresh();
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

    public void setDate(Calendar date1,Calendar date2) {
        this.date1 = date1;
        this.date2 = date2;
    }

    @SuppressLint("DefaultLocale")
    public void fresh() {
        List<Bill> bills = CloudSync.getLocalData(date1,date2);
        SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        if(bills!=null && !bills.isEmpty()) {
            binding.date1.setText(dateFormat.format(bills.get(0).getDate()));
            binding.date2.setText(dateFormat.format(bills.get(bills.size() - 1).getDate()));
        } else{
            binding.date1.setText("");
            binding.date2.setText("");
        }

        List<Bill> billsIncome = CloudSync.getLocalDataIncome(date1,date2);
        List<Bill> billsOutcome = CloudSync.getLocalDataOutcome(date1,date2);
        if(billsIncome!=null && !billsIncome.isEmpty()) {
            double Income = 0.0;
            for (int i = 0; i < billsIncome.size(); i++) {
                Income += billsIncome.get(i).getCharge();
            }
            binding.inCount.setText(String.valueOf(billsIncome.size()));
            binding.inCharge.setText(String.format("%.2f", Income));
        }else{
            binding.inCount.setText("0");
            binding.inCharge.setText("0");
        }
        if(billsOutcome!=null && !billsOutcome.isEmpty()){
            double Outcome = 0.0;
            for (int i = 0; i < billsOutcome.size(); i++) {
                Outcome += billsOutcome.get(i).getCharge();
            }
            binding.outCount.setText(String.valueOf(billsOutcome.size()));
            binding.outCharge.setText(String.format("%.2f", Outcome));
        }else{
            binding.outCount.setText("0");
            binding.outCharge.setText("0");
        }
    }

    private static GetStatisticsFragmentListener getStatisticsFragmentListener;

    public interface GetStatisticsFragmentListener{
        void onBack(StatisticsFragment statisticsFragment);
    }

    public static void setGetStatisticsFragmentListener(GetStatisticsFragmentListener listener){
        getStatisticsFragmentListener = listener;
    }
}