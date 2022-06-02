package xyz.lns103.bookkeeping;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import xyz.lns103.bookkeeping.bean.Bill;

public class CloudSync {
    String userID;
    String password;

    public CloudSync(String userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public static boolean syncNeeded(){
        List<Bill> bills = LitePal.where("state != 1").find(Bill.class);
        if(bills == null || bills.isEmpty()) return false;
        else return true;
    }

    public static List<Bill> getLocalData(){
        return LitePal.where("state != 3").order("date desc").find(Bill.class);
    }

    public static List<Bill> getLocalData(Calendar date1, Calendar date2){
        if(date1==null||date2==null) return LitePal.where("state != 3").order("date").find(Bill.class);
        long dateLong1 = date1.getTimeInMillis();
        long dateLong2 = date2.getTimeInMillis();
        if(dateLong1>=dateLong2){
            dateLong1 = dateLong1 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ?",String.valueOf(dateLong2),String.valueOf(dateLong1)).order("date desc").find(Bill.class);
        } else {
            dateLong2 = dateLong2 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ?", String.valueOf(dateLong1), String.valueOf(dateLong2)).order("date").find(Bill.class);
        }
    }

    public static List<Bill> getLocalDataIncome(Calendar date1, Calendar date2){
        if(date1==null||date2==null) return LitePal.where("state != 3 and charge > 0").order("date").find(Bill.class);
        long dateLong1 = date1.getTimeInMillis();
        long dateLong2 = date2.getTimeInMillis();
        if(dateLong1>=dateLong2){
            dateLong1 = dateLong1 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ? and charge > 0",String.valueOf(dateLong2),String.valueOf(dateLong1)).order("date desc").find(Bill.class);
        } else {
            dateLong2 = dateLong2 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ? and charge > 0", String.valueOf(dateLong1), String.valueOf(dateLong2)).order("date").find(Bill.class);
        }
    }

    public static List<Bill> getLocalDataOutcome(Calendar date1, Calendar date2){
        if(date1==null||date2==null) return LitePal.where("state != 3 and charge < 0").order("date").find(Bill.class);
        long dateLong1 = date1.getTimeInMillis();
        long dateLong2 = date2.getTimeInMillis();
        if(dateLong1>=dateLong2){
            dateLong1 = dateLong1 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ? and charge < 0",String.valueOf(dateLong2),String.valueOf(dateLong1)).order("date desc").find(Bill.class);
        } else {
            dateLong2 = dateLong2 + 86400*1000;
            return LitePal.where("state != 3 and date >= ? and date < ? and charge < 0", String.valueOf(dateLong1), String.valueOf(dateLong2)).order("date").find(Bill.class);
        }
    }

    public void syncAll(){
        Log.e(TAG, "syncAll: " );
        String url = "https://lns103.xyz/bill/list/user="+userID+"/password="+password;
        final Handler syncAllHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                String receive = (String) msg.obj;
                if(receive.equals("network failure")){
                    if (getSyncListener!=null){
                        getSyncListener.onBackMsg("网络错误，全量同步失败");
                    }
                    return;
                }
                try {
                    finishSyncAll(receive);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            };
        };
        NetworkTool.httpGet(url,syncAllHandler);
    }

    private void finishSyncAll( String receive) throws JSONException {
        JSONArray jsonArray = new JSONArray(receive);
        List<Bill> bills = transferJsonArray(jsonArray);

        ContentValues values = new ContentValues();
        values.put("fullSyncing", true);
        LitePal.updateAll(Bill.class, values);

        for(int i=0;i<bills.size();i++){//同步云端数据
            Log.e(TAG, "sync: "+bills.get(i).getType() );
            long createDate = bills.get(i).getCreateDateLong();
            Bill bill = LitePal.where("createDate = " + createDate).findFirst(Bill.class);
            if(bill!=null) {

                if (bill.getEditDateLong() != bills.get(i).getEditDateLong() && (bill.getState()!=2 || bill.getEditDateLong() <= bills.get(i).getEditDateLong())) {
                   LitePal.delete(Bill.class,bill.getId());
                   bills.get(i).setState(1);
                   bills.get(i).save();
                } else {
                    //Log.e(TAG, "finishSyncAll: "+ bill.getState());
                    if(bill.getState()==3) continue;
                    bill.setFullSyncing(false);
                    bill.setState(1);
                    bill.save();
                }

            } else{
                bills.get(i).setState(1);
                bills.get(i).save();
            }
        }
        LitePal.deleteAll(Bill.class,"fullSyncing = true and state = 1");
        ContentValues values2 = new ContentValues();
        values2.put("fullSyncing", false);
        LitePal.updateAll(Bill.class, values);
        syncByStatus();
        getSyncListener.onBackSync();
    }

    private void syncByStatus() {//同步本地未同步的
        List<Bill> bill_upload = LitePal.where("state = 0").find(Bill.class);
        List<Bill> bill_update = LitePal.where("state = 2").find(Bill.class);
        List<Bill> bill_delete = LitePal.where("state = 3").find(Bill.class);
        for(int i=0;i<bill_upload.size();i++){
            add(bill_upload.get(i));
        }
        for(int i=0;i<bill_update.size();i++){
            edit(bill_update.get(i));
        }
        for(int i=0;i<bill_delete.size();i++){
            delete(bill_delete.get(i));
        }
    }

    public void delete(Bill bill){
        Long id = bill.getCreateDate().getTime();
        String url = "https://lns103.xyz/bill/delete/id/"+ id +"/user="+userID+"/password="+password;
        final Handler deleteHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                String receive = (String) msg.obj;
                if(receive.equals("true")) finishDelete(bill);
                else checkIfDelete(bill);
                    //getSyncListener.onBackMsg("删除操作同步失败");
            };
        };
        NetworkTool.httpDelete(url,deleteHandler);
        bill.setState(3);
        bill.save();
    }

    private void checkIfDelete(Bill bill) {
        Long id = bill.getCreateDate().getTime();
        String url = "https://lns103.xyz/bill/list/id/"+ id +"/user="+userID+"/password="+password;
        final Handler checkDeleteHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                String receive = (String) msg.obj;
                Bill billGet = null;
                try {
                    billGet = transferJsonObject(new JSONObject(receive));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(receive.equals("network failure")){
                    if (getSyncListener!=null){
                        getSyncListener.onBackMsg("网络错误，删除同步失败");
                    }
                    return;
                } else if(billGet==null) finishDelete(bill);
            };
        };
        NetworkTool.httpGet(url,checkDeleteHandler);
    }

    private void finishDelete(Bill bill) {
        Log.e(TAG, "finishDelete" );
        bill.delete();
    }

    public void add(Bill bill){
        Long id = bill.getCreateDate().getTime();
        String url = "https://lns103.xyz/bill/add/user="+userID+"/password="+password;
        final Handler addHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                String receive = (String) msg.obj;
                if(receive.equals("true")) finishAdd(bill);
                if(receive.equals("network failure")){
                    if (getSyncListener!=null){
                        getSyncListener.onBackMsg("网络错误，添加同步失败");
                    }
                    return;
                }
            }
        };
        JSONObject jsonObject = toJsonObject(bill);
        NetworkTool.httpPost(url,jsonObject.toString(),addHandler);
        bill.setState(0);
        bill.save();
    }

    private void finishAdd(Bill bill) {
        bill.setState(1);
        bill.save();
        Log.e(TAG, "finishAdd" );
    }

    public void edit(Bill bill){
        long id = bill.getId();
        String url = "https://lns103.xyz/bill/update/user="+userID+"/password="+password;
        bill.setState(2);
        //Log.e(TAG, "id2 "+id);
        bill.save();
        final Handler editHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                String receive = (String) msg.obj;
                //Log.e(TAG, "id3 "+id );
                if(receive.equals("true")) finishEdit(bill,id);
                if(receive.equals("false")) add(bill);
                if(receive.equals("network failure")){
                    if (getSyncListener!=null){
                        getSyncListener.onBackMsg("网络错误，修改同步失败");
                    }
                    return;
                }
            };
        };
        JSONObject jsonObject = toJsonObject(bill);
        NetworkTool.httpPut(url,jsonObject.toString(),editHandler);
    }

    private void finishEdit(Bill bill ,long id) {
        //Log.e(TAG, "id4 "+bill.getId() );
        LitePal.delete(Bill.class,id);
        bill.setState(1);
        bill.save();
        Log.e(TAG, "finishEdit" );
    }


    public Bill transferJsonObject(JSONObject jsonObject){
        long createDate = 0L;
        long date = 0L;
        long editDate = 0L;
        double charge = 0.0;
        String type = "";
        String note = "";
        try {
            createDate = jsonObject.getLong("createDate");
            date = jsonObject.getLong("date");
            editDate = jsonObject.getLong("editDate");
            charge = jsonObject.getDouble("charge");
            type = jsonObject.getString("type");
            note = jsonObject.getString("note");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bill bill = new Bill(charge,date,type,note,editDate,createDate);
        return bill;
    }

    public List<Bill> transferJsonArray(JSONArray jsonArray) throws JSONException {
        List<Bill> bills = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            Bill bill = transferJsonObject(jsonArray.getJSONObject(i));
            bills.add(bill);
        }
        return bills;
    }

    public JSONObject toJsonObject(Bill bill){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("createDate",bill.getCreateDateLong());
            jsonObject.put("charge",bill.getCharge());
            jsonObject.put("date",bill.getDateLong());
            jsonObject.put("editDate",bill.getEditDateLong());
            jsonObject.put("type",bill.getType());
            jsonObject.put("note",bill.getNote());
            jsonObject.put("userId",userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private static GetSyncListener getSyncListener;

    public interface GetSyncListener{
        void onBackMsg(String msg);
        void onBackSync();
    }

    public static void setGetSyncListener(GetSyncListener listener){
        getSyncListener = listener;
    }

}
