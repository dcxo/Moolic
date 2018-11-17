package ml.dcxo.x.obwei.adapters

import android.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.item_song.view.*
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.base.BaseAdapter
import ml.dcxo.x.obwei.base.BaseViewHolder
import ml.dcxo.x.obwei.viewModel.Song

/**
 * Created by David on 02/11/2018 for ObweiX
 */
class SongsAdapter: BaseAdapter<Song, SongsAdapter.SongViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder =
		SongViewHolder( LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false) )
	override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean =
		oldItem.filePath == newItem.filePath
	override fun areItemTheSame(oldItem: Song, newItem: Song): Boolean = oldItem.id == newItem.id

	inner class SongViewHolder(itemView: View): BaseViewHolder<Song>(itemView) {

		var t: Target<*>? = null

		override fun bind(i: Song, position: Int, click: ((Song, Int) -> Unit)?, longClick: ((Song, Int) -> Unit)?) {
			super.bind(i, position, click, longClick)

			itemView.songTitle.text = i.title
			itemView.artistName.text = i.artistName

			t = Glide.with(itemView.context).load(i.getAlbumArtURI)
				.crossFade(440).into(itemView.thumbnailAlbumArt)

		}
		override fun recycle() {
		}

	}

}