package ml.dcxo.x.obwei.db.queue

import androidx.room.Entity
import androidx.room.PrimaryKey
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 23/02/2019 for ObweiX
 */

@Entity(tableName = "queue")
data class Queue(
	@PrimaryKey var primaryKey: Int = 205,
	var queue: ArrayList<Song>,
	var sortedQueue: ArrayList<Song>,
	var queuePosition: Int,
	var songDurationPosition: Int = 0
)