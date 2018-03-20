package com.kedacom.gradle_test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Handler handler;
    private static final int quit = 0;
    private static final int req1 = 1;
    private static final int req2 = 2;
    private static final int req3 = 3;
    private static final int rsp1 = 4;
    private static final int rsp2 = 5;
    private static final int rsp3 = 6;
    EmployeeDbHelper employeeDbHelper = new EmployeeDbHelper(this);
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PcTrace.p("~~>");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        employeeDbHelper = new EmployeeDbHelper(this);

        new Thread(){
            @SuppressLint("HandlerLeak")
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Looper.prepare();

                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what){
                            case quit:
                                Looper.myLooper().quit();
                                break;
                            case req1:
                                callback(rsp1);
                                break;
                            case req2:
                                callback(rsp2);
                                break;
                            case req3:
                                callback(rsp3);
                                break;
                        }
                    }
                };

                Looper.loop();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        PcTrace.p("~~>");
        super.onResume();
        if (null == db){
            db = employeeDbHelper.getWritableDatabase();
//            db.isInMemoryDatabase();
            handler.sendEmptyMessageDelayed(req1, 1000);
        }
    }

    @Override
    protected void onStop() {
        PcTrace.p("~~>");
        super.onStop();
        employeeDbHelper.close();
        db = null;
    }

    @Override
    protected void onDestroy() {
        PcTrace.p("~~>");
        super.onDestroy();
        handler.sendEmptyMessage(quit);
    }

    private void callback(int rsp){
        switch(rsp){
            case rsp1:
                db.beginTransaction(); // 一、开启事务。开启事务能大大提高插入效率
                saveDepartments();
                queryDepartments();
                handler.sendEmptyMessage(req2);
                break;
            case rsp2:
                saveEmployees();
                queryEmployees();
                db.setTransactionSuccessful();
                db.endTransaction();  // 二、提交事务。可见事务的“开启——提交”可以跨越消息交互流程。
                handler.sendEmptyMessage(req3);
                break;
            case rsp3:
                queryDepartments();
                queryEmployees();
                break;
        }
    }

    private void saveDepartments(){
        final List<ContentValues> depCvs = new ArrayList<>();
        // 创建一个虚拟的＂根部门＂，类似根目录"/"，以满足department表的完整性约束。
        ContentValues rootDep = new ContentValues();
        rootDep.put("id", 0);
        rootDep.put("name", "dep_"+0);
        rootDep.put("parentDepartmentId", 0);
        depCvs.add(rootDep);
        for (int i=0; i<1000; ++i){
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", i+1);
            contentValues.put("name", "dep_"+(i+1));
            contentValues.put("parentDepartmentId", i%10);
//            contentValues.put("memberNum", 0);　　// 需要更新的字段才添加进ContentValues否则update时会覆盖掉原来的值　
            depCvs.add(contentValues);
        }

        PcTrace.p("--> insert 1000 departments");
        for (ContentValues cv : depCvs){
            DbUtils.updateOrInsert(db, "department", cv,
                    "id=?", new String[]{cv.getAsString("id")});
        }
        PcTrace.p("<-- insert 1000 departments");

    }

    private void saveEmployees(){
        final List<ContentValues> empCvs = new ArrayList<>();
        for (int i=0; i<10000; ++i){
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", i+1);
            contentValues.put("name", "emp_"+(i%9900+1));
            contentValues.put("birth", 1960+i%40);
            contentValues.put("nativePlace", "nativePlace_"+i%100);
            contentValues.put("address", "address_"+i%100);
            contentValues.put("phone", ""+12345600000L+i);
            contentValues.put("email", "email_"+i+"@gmail.com");
            contentValues.put("departmentId", i%4+1);
            empCvs.add(contentValues);
        }

        PcTrace.p("--> insert 10000 employees");
        for (ContentValues cv : empCvs) {
            DbUtils.updateOrInsert(db, "employee", cv,
                    "id=?", new String[]{cv.getAsString("id")});
        }
        PcTrace.p("<-- insert 10000 employees");
    }

    private void queryDepartments(){
        long depCnt = DbUtils.count(db, "department", null, null);
        PcTrace.p("--> total department count=%s, query department", depCnt);
        Cursor cursor;
//        cursor = db.query(true, "department", /*new String[]{"id", "parentDepartmentId", ""}*/null, null, null,
//                null, null, null, "9"); // 使用query，更安全，防止sql注入。
        cursor = db.rawQuery("select * from department limit 9", null); // 使用rawQuery更灵活方便
        while (cursor.moveToNext()) {
            PcTrace.p("(%s,%s,%s,%s)", cursor.getString(0), cursor.getString(1),
                    cursor.getString(2),cursor.getString(3));
        }
        cursor.close();
    }

    private void queryEmployees(){
        long cnt = DbUtils.count(db, "employee", null, null);
        PcTrace.p("--> total employee count=%s, batch query 9 employees", cnt);
        Cursor cursor = db.query("employee", null, null, null,
                null, null, null, "9");
        PcTrace.p("count=%s", cursor.getCount());
//        int nameIdx = cursor.getColumnIndex("name");

        while (cursor.moveToNext()) {
            PcTrace.p("(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)", cursor.getString(0),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4),
                    cursor.getString(5), cursor.getString(6),
                    cursor.getString(7), cursor.getString(8),
                    cursor.getString(9));
        }
        cursor.close();
    }

}
