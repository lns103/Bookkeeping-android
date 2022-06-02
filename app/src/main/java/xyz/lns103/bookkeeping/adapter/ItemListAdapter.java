package xyz.lns103.bookkeeping.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.lns103.bookkeeping.bean.Bill;
import xyz.lns103.bookkeeping.R;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {


    List<Bill> bills;
    Context context;
    View mView;
    boolean clickable = true;

    public ItemListAdapter(List<Bill> bills, Context context) {
        this.bills = bills;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent,false);
        mView = view;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemListAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.item_type.setText(bills.get(position).getType());
        holder.item_date.setText(bills.get(position).getDataString());
        holder.item_money.setText(bills.get(position).getChargeString());
        if(bills.get(position).getCharge()>0) holder.item_money.setTextColor(ContextCompat.getColor(context, R.color.text_red));
        else holder.item_money.setTextColor(ContextCompat.getColor(context, R.color.text_green));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!clickable) return;
                if (getEditListener!=null){
                    getEditListener.onBack(bills.get(position),position);
                }
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(!clickable) return false;
                myPopupMenu(holder.view,position);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return bills == null? 0: bills.size();
    }

    public List<Bill> getBills() {
        return bills;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView item_type;
        TextView item_date;
        TextView item_money;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            item_type = itemView.findViewById(R.id.item_type);
            item_date = itemView.findViewById(R.id.item_date);
            item_money = itemView.findViewById(R.id.item_money);
        }
    }

    public void add(Bill bill){
        bills.add(0,bill);
        notifyItemInserted(0);
        notifyItemRangeChanged(0,getItemCount());
    }

    public void add(Bill bill,int position){
        bills.add(position,bill);
        notifyItemInserted(position);
        notifyItemRangeChanged(position,getItemCount());
    }

    public void edit(Bill bill,int position){
        bills.set(position,bill);
        notifyItemChanged(position);
    }

    public void remove(int position) {
        Bill bill = bills.get(position);
        bills.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, bills.size());
        if (getEditListener!=null){
            getEditListener.onBackRestore(bill,position);
        }
    }

    private void myPopupMenu(View v,int position) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.setForceShowIcon(true);
        popupMenu.getMenuInflater().inflate(R.menu.bill_long_click_pop_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()){
                    case R.id.edit:
                        if (getEditListener!=null)
                            getEditListener.onBack(bills.get(position),position);
                        break;
                    case R.id.delete:
                        remove(position);
                }
                return true;
            }
        });
        //显示菜单
        popupMenu.show();
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    private static GetEditListener getEditListener;

    public interface GetEditListener{
        void onBack(Bill bill,int position);
        void onBackRestore(Bill bill,int position);
    }

    public static void setGetEditListener(GetEditListener listener){
        getEditListener = listener;
    }
}
