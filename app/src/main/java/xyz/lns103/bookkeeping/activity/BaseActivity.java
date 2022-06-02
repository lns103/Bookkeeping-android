package xyz.lns103.bookkeeping.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import rikka.insets.WindowInsetsHelper;
import rikka.layoutinflater.view.LayoutInflaterFactory;

public class BaseActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        this.getLayoutInflater().setFactory2((new LayoutInflaterFactory(this.getDelegate())).addOnViewCreatedListener(WindowInsetsHelper.Companion.getLISTENER()));
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
    }
}
