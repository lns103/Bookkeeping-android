package xyz.lns103.bookkeeping.fragment;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.activity.EditPasswordActivity;
import xyz.lns103.bookkeeping.activity.LoginActivity;
import xyz.lns103.bookkeeping.activity.MainActivity;
import xyz.lns103.bookkeeping.databinding.FragmentSettingBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters


    FragmentSettingBinding binding;
    public SettingFragment() {
        // Required empty public constructor
    }


    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingBinding.inflate(inflater,container,false);
        sharedPreferences =  getContext().getSharedPreferences("login_information",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        binding.userId.setText(sharedPreferences.getString("user_id",""));

        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: " );
                editor.putBoolean("login",false);
                editor.putString("user_id","");
                editor.putString("password","");
                editor.commit();
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        binding.editPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getContext(), EditPasswordActivity.class);
                startActivity(intent);
            }
        });

        return binding.getRoot();

        //return inflater.inflate(R.layout.fragment_setting, container, false);
    }

}