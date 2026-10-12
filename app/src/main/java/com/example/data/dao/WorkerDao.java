package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.model.Worker;

import java.util.List;

@Dao
public interface WorkerDao {
    @Query("SELECT * FROM workers ORDER BY name ASC")
    List<Worker> getAllWorkers();

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    List<Worker> getActiveWorkers();

    @Query("SELECT * FROM workers WHERE isActive = 1 AND assignedSiteId = :siteId ORDER BY name ASC")
    List<Worker> getActiveWorkersBySite(int siteId);

    @Query("SELECT workerCode FROM workers")
    List<String> getAllWorkerCodes();

    @Query("SELECT * FROM workers WHERE id = :id LIMIT 1")
    Worker getWorkerById(int id);

    @Query("SELECT * FROM workers WHERE workerCode = :code LIMIT 1")
    Worker getWorkerByCode(String code);

    @Query("SELECT COUNT(*) FROM workers WHERE isActive = 1")
    int countActive();

    @Query("SELECT COUNT(*) FROM workers WHERE isActive = 1 AND assignedSiteId = :siteId")
    int countActiveBySite(int siteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Worker worker);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Worker> workers);

    @Update
    void update(Worker worker);

    @Delete
    void delete(Worker worker);
}
