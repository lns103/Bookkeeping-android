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
import java.util.Objects;

import xyz.lns103.bookkeeping.CloudSync;
import xyz.lns103.bookkeeping.activity.AddItemActivity;
import xyz.lns103.bookkeeping.bean.Bill;
import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.adapter.ItemListAdapter;
import xyz.lns103.bookkeeping.databinding.FragmentItemListBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ItemListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ItemListFragment extends Fragment {

    public List<Bill> mBills = new ArrayList<>();
    //private View rootView;
    FragmentItemListBinding binding;
    private ItemListAdapter itemListAdapter;
    private RecyclerView recyclerView;
    private CloudSync cloudSync;
    boolean firstOpen = true;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Activity parentActivity;


    public ItemListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ItemListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ItemListFragment newInstance() {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: " );
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        sharedPreferences =  getContext().getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        String userID = sharedPreferences.getString("user_id","");
        String password = sharedPreferences.getString("password","");
        cloudSync = new CloudSync(userID,password);
        if(savedInstanceState!=null) onBackFragment();
    }

    @Override
    public void onResume() {
        String userID = sharedPreferences.getString("user_id","");
        String password = sharedPreferences.getString("password","");
        cloudSync = new CloudSync(userID,password);
        super.onResume();
    }

    private void onBackFragment() {
        getItemListFragmentListener.onBack(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItemListBinding.inflate(inflater,container,false);
        if(savedInstanceState!=null){
            firstOpen = false;
            mBills = (List<Bill>) savedInstanceState.getSerializable("bills");
        }
        setListener();
        initBills();
        initRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();//滚动到顶部
        parentActivity = getActivity();
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

    private void initBills() {
        mBills = CloudSync.getLocalData();
    }

    private void initRecyclerView() {
        recyclerView = binding.recyclerView;
        int spanCount = getSpanCount();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),spanCount);
        //if(isLandscape()) gridLayoutManager.setSpanCount(2);
        recyclerView.setLayoutManager(gridLayoutManager);
        itemListAdapter = new ItemListAdapter(mBills,getContext());
        recyclerView.setAdapter(itemListAdapter);
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


    public void addBillForResult(){
        Intent intent = new Intent();
        intent.setClass(getActivity(),AddItemActivity.class);
        AddBill.launch(intent);
    }

    public void addBill(Bill bill){
        if(itemListAdapter==null) {
            itemListAdapter = new ItemListAdapter(mBills,getContext());
        }
        cloudSync.add(bill);
        itemListAdapter.add(bill);
        recyclerView.scrollToPosition(0);
    }

    private void setListener() {
        ItemListAdapter.setGetEditListener(new ItemListAdapter.GetEditListener() {
            @Override
            public void onBack(Bill bill, int position) {
                editBill(bill,position);
            }

            @Override
            public void onBackRestore(Bill bill, int position) {
                cloudSync.delete(bill);
                Snackbar.make(binding.getRoot(),"已删除项目",Snackbar.LENGTH_SHORT).setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bill copyBill = Bill.copyBill(bill);
                        cloudSync.add(bill);
                        itemListAdapter.add(copyBill, position);
                    }
                }).show();
            }
        });

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

    public void editBill(Bill bill,int position){
        Intent intent = new Intent();
        intent.setClass(getActivity(), AddItemActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("bill",(Serializable) bill);
        someActivityResultLauncher.launch(intent);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList(){
        //mBills = LitePal.order("date desc").find(Bill.class);
        mBills = CloudSync.getLocalData();
        itemListAdapter.setBills(mBills);
        itemListAdapter.notifyDataSetChanged();
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();

                    if(data.getBooleanExtra("delete",false)){
                        int position = data.getIntExtra("position",0);
                        itemListAdapter.remove(position);
                    }

                    if(data.getBooleanExtra("edit",false)){
                        Bill bill = (Bill)data.getSerializableExtra("bill");
                        int position = data.getIntExtra("position",0);
                        cloudSync.edit(bill);
                        itemListAdapter.edit(bill,position);
                    }

                    refreshListListener.onBack();
                }
            });

    ActivityResultLauncher<Intent> AddBill = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Bill bill = (Bill)data.getSerializableExtra("bill");
                    addBill(bill);
                }

                refreshListListener.onBack();
            });

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bills",(Serializable) itemListAdapter.getBills());
    }

    private static GetItemListFragmentListener getItemListFragmentListener;

    public interface GetItemListFragmentListener{
        void onBack(ItemListFragment itemListFragment);
    }

    public static void setGetItemListFragmentListener(GetItemListFragmentListener listener){
        getItemListFragmentListener = listener;
    }

    private static RefreshListListener refreshListListener;

    public interface RefreshListListener{
        void onBack();
    }

    public static void setRefreshListListener(RefreshListListener listener){
        refreshListListener = listener;
    }
}