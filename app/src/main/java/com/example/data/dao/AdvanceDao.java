package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.data.model.Advance;

import java.util.List;

@Dao
public interface AdvanceDao {
    @Query("SELECT * FROM advances ORDER BY date DESC, id DESC")
    List<Advance> getAllAdvances();

    @Query("SELECT * FROM advances WHERE siteId = :siteId ORDER BY date DESC, id DESC")
    List<Advance> getAdvancesForSite(int siteId);

    @Query("SELECT * FROM advances WHERE workerId = :workerId ORDER BY date DESC, id DESC")
    List<Advance> getAdvancesForWorker(int workerId);

    @Query("SELECT * FROM advances WHERE workerId = :workerId AND date BETWEEN :startDate AND :endDate")
    List<Advance> getAdvancesForWorkerBetweenDates(int workerId, String startDate, String endDate);

    @Query("SELECT * FROM advances WHERE date BETWEEN :startDate AND :endDate")
    List<Advance> getAdvancesBetweenDates(String startDate, String endDate);

    @Query("SELECT SUM(amount) FROM advances WHERE siteId = :siteId")
    Double getTotalAdvancesForSite(int siteId);

    @Query("SELECT SUM(amount) FROM advances")
    Double getTotalAdvancesAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Advance advance);

    @Delete
    void delete(Advance advance);

    @Query("DELETE FROM advances WHERE id = :id")
    void deleteById(int id);
}
