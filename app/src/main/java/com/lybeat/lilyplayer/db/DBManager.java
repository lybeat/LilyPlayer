package com.lybeat.lilyplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lybeat.lilyplayer.entity.PlayRecord;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * Author: lybeat
 * Date: 2016/7/18
 */
public class DBManager {

    private static final String DB_NAME = "lily_payer_db";

    private static DBManager dbManager;
    private DaoMaster.DevOpenHelper devOpenHelper;
    private Context context;

    public DBManager(Context context) {
        this.context = context;
        this.devOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
    }

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            synchronized (DBManager.class) {
                if (dbManager == null) {
                    dbManager= new DBManager(context);
                }
            }
        }
        return dbManager;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (devOpenHelper == null) {
            devOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        }
        return devOpenHelper.getReadableDatabase();
    }

    private SQLiteDatabase getWritableDatabase() {
        if (devOpenHelper == null) {
            devOpenHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        }
        return devOpenHelper.getWritableDatabase();
    }

    public void insertPlayRecord(PlayRecord playRecord) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        PlayRecordDao playRecordDao = daoSession.getPlayRecordDao();
        playRecordDao.insert(playRecord);
    }

    public void insertPlayRecordList(List<PlayRecord> playRecords) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        PlayRecordDao playRecordDao = daoSession.getPlayRecordDao();
        playRecordDao.insertInTx(playRecords);
    }

    public void deletePlayRecord(PlayRecord playRecord) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        PlayRecordDao playRecordDao = daoSession.getPlayRecordDao();
        playRecordDao.delete(playRecord);
    }

    public List<PlayRecord> queryPlayRecordList() {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        PlayRecordDao playRecordDao = daoSession.getPlayRecordDao();
        QueryBuilder<PlayRecord> queryBuilder = playRecordDao.queryBuilder();
        return queryBuilder.list();
    }

    public List<PlayRecord> queryPlayRecordList(String name) {
        DaoMaster daoMaster = new DaoMaster(getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        PlayRecordDao playRecordDao = daoSession.getPlayRecordDao();
        QueryBuilder<PlayRecord> queryBuilder = playRecordDao.queryBuilder();
        queryBuilder.where(PlayRecordDao.Properties.Name.gt(name)).orderAsc(PlayRecordDao.Properties.Name);
        return queryBuilder.list();
    }
}
