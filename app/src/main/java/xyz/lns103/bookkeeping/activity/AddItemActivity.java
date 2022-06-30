package xyz.lns103.bookkeeping.activity;


import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.util.Date;

import xyz.lns103.bookkeeping.R;
import xyz.lns103.bookkeeping.bean.Bill;
import xyz.lns103.bookkeeping.databinding.ActivityAddItemBinding;


public class AddItemActivity extends BaseActivity {
    private ActivityAddItemBinding binding;
    private Bill bill;
    private int position;
    private Calendar date;
    private Date createDate;
    private boolean isChargeNegative = true;
    private long id;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        view = binding.getRoot();
        position = getIntent().getIntExtra("position",-1);
        date = Calendar.getInstance();
        if(position!=-1){
            bill = (Bill)getIntent().getSerializableExtra("bill");
            isChargeNegative = bill.isChargeNegative();
            binding.charge.setText(bill.getChargeStringUnsigned());
            binding.type.setText(bill.getType());
            binding.note.setText(bill.getNote());
            date = bill.getDateCalendar();
            id = bill.getId();
            //Log.e(TAG, "id1 "+id );
            createDate = bill.getCreateDate();
            binding.historyView.setVisibility(View.VISIBLE);
            binding.historyTime.setText(bill.getEditDataString());
        }
        freshSymbol();
        refreshTime();
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.symbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChargeNegative) isChargeNegative = false;
                else isChargeNegative = true;
                freshSymbol();
            }
        });

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddItemActivity.this,
                        DatePickerListener,
                        date.get(Calendar.YEAR),
                        date.get(Calendar.MONTH),
                        date.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();

            }
        });

        binding.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddItemActivity.this,
                        TimePickerListener,
                        date.get(Calendar.HOUR_OF_DAY),
                        date.get(Calendar.MINUTE),
                        true);
                timePickerDialog.show();
            }
        });
    }

    private void freshSymbol() {
        if(isChargeNegative) {
            binding.symbol.setText("-");
            binding.symbol.setTextColor(this.getColor(R.color.text_green));
            binding.charge.setTextColor(this.getColor(R.color.text_green));
        } else {
            binding.symbol.setText("+");
            binding.symbol.setTextColor(this.getColor(R.color.text_red));
            binding.charge.setTextColor(this.getColor(R.color.text_red));
        }
    }


    private void returnData(){
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(binding.getRoot().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        if(String.valueOf(binding.charge.getText()).equals("")){
            Snackbar.make(view,"数额不能为空",Snackbar.LENGTH_SHORT).show();
            return;
        }
        double charge = Double.parseDouble(String.valueOf(binding.charge.getText()));
        if(isChargeNegative) charge = 0 - charge;
        //Calendar date = new Calendar.g;//需要改进
        String type = String.valueOf(binding.type.getText());
        String note = String.valueOf(binding.note.getText());
        Intent intent = new Intent();
        if(position!=-1) {
            if (charge == bill.getCharge() && type.equals(bill.getType()) && note.equals(bill.getNote()) && date == bill.getDateCalendar()) {
                intent.putExtra("edit", false);
            }
            else {
                bill = new Bill(charge, date.getTime(), type, note, createDate);//edit
                bill.setId(id);
                //Log.e(TAG, "id1 "+bill.getId() );
                intent.putExtra("position", position);
                intent.putExtra("id_before", id);
                intent.putExtra("bill", (Serializable) bill);
                intent.putExtra("edit", true);
            }
        }else{
            bill = new Bill(charge, date.getTime(), type, note,new Date());//new
            intent.putExtra("position", position);
            intent.putExtra("bill", (Serializable) bill);
            intent.putExtra("edit", true);
        }
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    private void deleteItem(){
        Intent intent = new Intent();
        intent.putExtra("position", position);
        intent.putExtra("delete", true);
        if(position!=-1)setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    private void refreshTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        binding.date.setText(dateFormat.format(date));
        binding.time.setText(timeFormat.format(date));
    }

    private final DatePickerDialog.OnDateSetListener DatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            date.set(i,i1,i2);
            refreshTime();
        }
    };

    private final TimePickerDialog.OnTimeSetListener TimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            date.set(date.get(Calendar.YEAR),
                    date.get(Calendar.MONTH),
                    date.get(Calendar.DAY_OF_MONTH),
                    i,i1);
            refreshTime();
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                deleteItem();
                break;
            case R.id.save:
                returnData();
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item_appbar_menu, menu);
        //返回true代表普通菜单显示
        return true;
    }

}