package com.example.antiprocrastination.data

import androidx.room.*
import com.example.antiprocrastination.domain.model.DistractionApp
import kotlinx.coroutines.flow.Flow

@Dao
interface DistractionDao {
    @Query("SELECT * FROM distraction_apps")
    fun getAllDistractions(): Flow<List<DistractionApp>>

    @Query("SELECT * FROM distraction_apps")
    suspend fun getAllDistractionsList(): List<DistractionApp>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDistraction(app: DistractionApp)

    @Delete
    suspend fun deleteDistraction(app: DistractionApp)

    @Query("SELECT EXISTS(SELECT 1 FROM distraction_apps WHERE packageName = :packageName)")
    suspend fun isLearnedDistraction(packageName: String): Boolean
}
