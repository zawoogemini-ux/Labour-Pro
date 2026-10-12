package com.example.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.data.model.Site;

import java.util.List;

@Dao
public interface SiteDao {
    @Query("SELECT * FROM sites ORDER BY siteName ASC")
    List<Site> getAllSites();

    @Query("SELECT * FROM sites WHERE isActive = 1 ORDER BY siteName ASC")
    List<Site> getActiveSites();

    @Query("SELECT * FROM sites WHERE id = :id LIMIT 1")
    Site getSiteById(int id);

    @Query("SELECT COUNT(*) FROM sites")
    int count();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Site site);

    @Update
    void update(Site site);

    @Delete
    void delete(Site site);
}
