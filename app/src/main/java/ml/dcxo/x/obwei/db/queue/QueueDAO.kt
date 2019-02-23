package ml.dcxo.x.obwei.db.queue

import androidx.room.*
import ml.dcxo.x.obwei.viewModel.Tracklist

/**
 * Created by David on 23/02/2019 for ObweiX
 */
@Dao interface QueueDAO {

	@Query("SELECT * FROM queue WHERE primaryKey = 205") fun getQueue(): Queue?
	@Query("UPDATE queue SET queuePosition = :queuePosition WHERE primaryKey = 205")
	fun updateQueuePosition(queuePosition: Int)
	@Query("UPDATE queue SET queue = :queue WHERE primaryKey = 205") fun updateQueue(queue: Tracklist)
	@Query("UPDATE queue SET songDurationPosition = :trackPosition WHERE primaryKey = 205")
	fun updateInTrackPosition(trackPosition: Int)
	@Insert(onConflict = OnConflictStrategy.REPLACE) fun insertQueue(queue: Queue)

}