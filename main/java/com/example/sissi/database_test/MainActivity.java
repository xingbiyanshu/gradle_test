package com.example.sissi.database_test;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        employeeDbHelper = new EmployeeDbHelper(this);

//        new Thread(){
//            @Override
//            public void run() {
//                dbOp();
//            }
//        }.start();

        new Thread(){
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                Looper.prepare();

                handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what){
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
        super.onResume();
        if (null == db){
            db = employeeDbHelper.getWritableDatabase();
//            db.isInMemoryDatabase();
            handler.sendEmptyMessage(req1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        employeeDbHelper.close();
        db = null;
    }

    private void callback(int rsp){
        switch(rsp){
            case rsp1:
//                db.beginTransaction();
                saveDepartments();
                queryDepartments();
                handler.sendEmptyMessage(req2);
                break;
            case rsp2:
                saveEmployees();
                queryEmployees();
//                db.setTransactionSuccessful();
//                db.endTransaction();
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


    private void dbOp(){
        EmployeeDbHelper employeeDbHelper = new EmployeeDbHelper(this);
        final SQLiteDatabase db = employeeDbHelper.getWritableDatabase();

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


//        db.setMaximumSize(1024*1024); // 设置数据库大小上限

        // 插入部门(部门的父部门字段存在完整性约束,必须已存在于数据库中,所以插入部门的顺序需要注意先插父部门再插子部门).
        PcTrace.p("--> insert 1000 departments");
        db.beginTransaction();
        try{
            for (ContentValues cv : depCvs){
                DbUtils.updateOrInsert(db, "department", cv,
                        "id=?", new String[]{cv.getAsString("id")});
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }

        // 插入员工(插入的员工的部门id必须在部门表中,这有外键约束,所以先插入部门再插入员工)
        PcTrace.p("--> insert 10000 employees");
        db.beginTransaction();
        try {
            db.beginTransaction();
            try {
                int i = 0;
                for (ContentValues cv : empCvs) {
                if (++i==5000){
//                    throw new RuntimeException("just for test"); // 测试事务回滚（没有一个人被插入）以及触发器回滚（部门下人数为0）
                    PcTrace.p("employee count=%s", DbUtils.count(db, "employee", null, null)); // 此处打印出已插入的人数，后面事务回滚后再次查询对比
                }

                    DbUtils.updateOrInsert(db, "employee", cv,
                            "id=?", new String[]{cv.getAsString("id")});
                }
                db.setTransactionSuccessful();
            }catch (Exception e){

            }finally {
                db.endTransaction();
            }

//            if (true) {
//                throw new RuntimeException("for test savepoint"); // 测试嵌套事务回滚。期望的行为是此处抛出异常后内部嵌套的事务亦会被回滚。
//            }

            db.setTransactionSuccessful();
        }catch (Exception e){
            PcTrace.p(e.getMessage());
        }
        finally {
            db.endTransaction();
        }

        // 删除员工(注意观察部门人数的变化)
        db.delete("employee", "id=1", null);

        // 更新员工(注意观察部门人数的变化)
        ContentValues tmpCv = new ContentValues();
        tmpCv.put("departmentId", 2);
        db.update("employee", tmpCv, "id=3", null); // 第二次执行时由于departmentId未变updateEmpTrig不会触发。

        // 删除部门(注意观察部门下子部门以及人员也随之删除了,外键以及触发器综合作用的结果)
        db.delete("department", "id=2", null);

        // 查询部门
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

        // 批量查询员工
        long cnt = DbUtils.count(db, "employee", null, null);
        PcTrace.p("--> total employee count=%s, batch query 9 employees", cnt);
        cursor = db.query("employee", null, null, null,
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

        for (int k=0; k<3; ++k) { // 批量查询,第一次返回“满足条件的结果集里的”1到3号记录,第二次返回4到6，第三次返回7到9
            cursor = db.query("employee", null, null, null,
                    null, null, null, k*3+","+"3"/*"3 offset "+k*3*/);
            PcTrace.p("count=%s", cursor.getCount());

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


//        // 插入记录
//        PcTrace.p("--> insert 10000 record");
//
//        db.beginTransaction();  // 批量插入时开启事务能显著提高效率　
//        try {
////            for (ContentValues values : empCvs) {
//            for (int i=0; i<empCvs.size(); ++i) {
//                if (0==i%1000){
//                    final int finalI = i;
//                    new Thread(){ // 多线程方式插入，考察数据库对多线程的适应性以及多线程和事务的关系
//                        @Override
//                        public void run() {
//                            db.beginTransaction();  // 批量插入时开启事务能显著提高效率　
//                            try {
//                                int j;
//                                for (j = finalI; j < finalI + 1000; ++j) {
//                                    DbUtils.updateOrInsert(db, "employee", empCvs.get(j),
//                                            "name=?", new String[]{empCvs.get(j).getAsString("name")});
//                                }
//                                if (10000==j) {
//                                    Cursor cursor = db.query("employee", null, null, null,
//                                            null, null, null, "9");
//                                    PcTrace.p("count=%s", cursor.getCount());
//                                    int nameIdx = cursor.getColumnIndex("name");
//
//                                    while (cursor.moveToNext()) {
//                                        PcTrace.p("record name=%s", cursor.getString(nameIdx));
//                                    }
//                                    cursor.close();
//
//                                    for (int k=0; k<3; ++k) { // 批量查询,第一次返回“满足条件的结果集里的”1到3号记录,第二次返回4到6，第三次返回7到9
//                                        cursor = db.query("employee", null, null, null,
//                                                null, null, null, k*3+","+"3"/*"3 offset "+k*3*/);
//                                        PcTrace.p("count=%s", cursor.getCount());
//
//                                        while (cursor.moveToNext()) {
//                                            PcTrace.p("record name=%s", cursor.getString(nameIdx));
//                                        }
//                                        cursor.close();
//                                    }
//                                }
//
//                                db.setTransactionSuccessful();
//                            }finally {
//                                db.endTransaction();
//                            }
//                        }
//                    }.start();
//                }
////                DbUtils.updateOrInsert(db, "employee", values, "name=?", new String[]{values.getAsString("name")});
//            }
//            db.setTransactionSuccessful();
//        }finally { // try finally的方式是事务的标准写法　
//            db.endTransaction();
//        }
//
//
//        PcTrace.p("<-- insert 10000 record");

//        cursor.close();
//        employeeDbHelper.close();
    }

}
