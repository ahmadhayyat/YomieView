package com.signage.yomie.CMSPlayer.network.translation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signage.yomie.commons.AppConstants

@Dao
interface TranslationDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setTranslation(en: En)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setTranslation(fr: Fr)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setTranslation(nl: Nl)

    @Query("SELECT * FROM ${AppConstants.English_table_name}")
    fun getEnTranslation(): List<En?>

    @Query("SELECT * FROM ${AppConstants.French_table_name}")
    fun getFrTranslation(): List<Fr?>

    @Query("SELECT * FROM ${AppConstants.Dutch_table_name}")
    fun getNlTranslation(): List<Nl?>

    @Query("DELETE FROM ${AppConstants.English_table_name}")
    fun deleteEnTranslation()

    @Query("DELETE FROM ${AppConstants.French_table_name}")
    fun deleteFrTranslation()

    @Query("DELETE FROM ${AppConstants.Dutch_table_name}")
    fun deleteNlTranslation()
}