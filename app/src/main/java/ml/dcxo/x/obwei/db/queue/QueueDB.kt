package ml.dcxo.x.obwei.db.queue

import androidx.room.*

/**
 * Created by David on 23/02/2019 for ObweiX
 */
@Database(entities = [Queue::class], version = 1)
@TypeConverters(TracklistConverter::class)
abstract class QueueDB: RoomDatabase() {
	abstract fun queueDAO(): QueueDAO
}