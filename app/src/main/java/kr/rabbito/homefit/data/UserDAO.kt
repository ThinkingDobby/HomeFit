package kr.rabbito.homefit.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDAO {
    @Query("SELECT * FROM User ORDER BY id")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM User WHERE id = :id")
    fun getUserById(id: Long): List<User>

    @Query("UPDATE User SET userName = :userName, height = :height, weight = :weight, mealCount = :mealCount, favWorkout = :favWorkout, basicDiet = :basicDiet WHERE id = :id")
    fun updateUserById(id: Long, userName: String, height: Int, weight: Int, mealCount: Int, favWorkout: String, basicDiet: String)

    @Query("SELECT weight FROM User WHERE id = :id")
    fun getWeightById(id: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM User")
    fun deleteAll()
}