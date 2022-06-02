package xyz.lns103.bookkeeping.bean;

import static android.content.ContentValues.TAG;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.util.Log;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class Bill extends LitePalSupport implements Serializable {
    /*
    state:
    0.need to upload
    1.synced
    2.need to update
    3.need to delete
     */
    private double charge;
    private Date date;
    private Date editDate;
    private String type;
    private String note;
    private long id;
    private int state;
    private Date createDate;
    private Boolean fullSyncing = false;

    public Bill(double charge, Date date, Date editDate, String type, String note, long id, int state, Date createDate, Boolean fullSyncing) {
        this.charge = charge;
        this.date = date;
        this.editDate = editDate;
        this.type = type;
        this.note = note;
        this.id = id;
        this.state = state;
        this.createDate = createDate;
        this.fullSyncing = fullSyncing;
    }

    public Bill(double charge, Date date, String type, String note, Date createDate) {
        this.charge = charge;
        this.date = date;
        this.type = type;
        this.note = note;
        this.editDate = new Date();
        this.createDate = createDate;
    }

    public Bill(double charge, Date date, String type, String note, Date editDate, Date createDate) {
        this.charge = charge;
        this.date = date;
        this.type = type;
        this.note = note;
        this.editDate = editDate;
        this.createDate = createDate;
    }

    public Bill(double charge, long date, String type, String note, long editDate, long createDate) {
        this.charge = charge;
        this.date = new Date(date);
        this.type = type;
        this.note = note;
        this.editDate = new Date(editDate);
        this.createDate = new Date(createDate);
    }



    public static Bill copyBill(Bill bill1){
        Bill bill2 = new Bill(bill1.getCharge(),bill1.getDate(), bill1.getType(), bill1.getNote(), bill1.getEditDate(),bill1.getCreateDate());
        return bill2;
    }

    public Date getEditDate() {
        return editDate;
    }

    public Calendar getEditDateCalendar() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public long getEditDateLong(){
        return editDate.getTime();
    }

    public String getEditDataString(){
        SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd' 'HH:mm", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        now.set(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH),0,0,0);
        long time = editDate.getTime() - now.getTimeInMillis();
        Calendar old = Calendar.getInstance();
        old.setTime(editDate);
        if(old.get(Calendar.YEAR)==now.get(Calendar.YEAR)) format= new SimpleDateFormat("MM.dd' 'HH:mm", Locale.getDefault());
        if(time>=0 && time<86400*1000) format= new SimpleDateFormat("今天' 'HH:mm", Locale.getDefault());
        if(time<0 && time>-86400*1000) format= new SimpleDateFormat("昨天' 'HH:mm", Locale.getDefault());
        String myDate = format.format(editDate);
        return myDate;
    }

    public void setEditDate(Date editDate) {
        this.editDate = editDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getCharge() {
        return charge;
    }

    public String getChargeString(){
        String charge = String.format("%.2f",getCharge());
        if (getCharge()<0) return charge;
        else return "+" + charge;
    }

    public void setCharge(double charge) {
        this.charge = charge;
    }

    public Date getDate() {
        return date;
    }

    public Calendar getDateCalendar() {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    public long getDateLong(){
        return date.getTime();
    }

    public String getDataString(){
        SimpleDateFormat format= new SimpleDateFormat("yyyy.MM.dd' 'HH:mm", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        now.set(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH),0,0,0);
        long time = date.getTime() - now.getTimeInMillis();
        Calendar old = Calendar.getInstance();
        old.setTime(date);
        if(old.get(Calendar.YEAR)==now.get(Calendar.YEAR)) format= new SimpleDateFormat("MM.dd' 'HH:mm", Locale.getDefault());
        if(time>=0 && time<86400*1000) format= new SimpleDateFormat("今天' 'HH:mm", Locale.getDefault());
        if(time<0 && time>-86400*1000) format= new SimpleDateFormat("昨天' 'HH:mm", Locale.getDefault());
        String myDate = format.format(date);
        return myDate;
    }

    public void setDateCalendar(Calendar date) {
        this.date = date.getTime();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Calendar getCreateDateInCalendar() {
        Calendar c = Calendar.getInstance();
        c.setTime(createDate);
        return c;
    }

    public long getCreateDateLong(){
        return createDate.getTime();
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setCreateDateInCalendar(Calendar createDate) {
        this.createDate = createDate.getTime();
    }

    public boolean isFullSyncing() {
        return fullSyncing;
    }

    public void setFullSyncing(boolean fullSyncing) {
        this.fullSyncing = fullSyncing;
    }
}
