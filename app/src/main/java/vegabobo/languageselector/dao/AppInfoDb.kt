package vegabobo.languageselector.dao

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppInfoEntity::class], version = 1, exportSchema = true)
abstract class AppInfoDb : RoomDatabase() {
    abstract fun appInfoDao(): AppInfoDao
}
