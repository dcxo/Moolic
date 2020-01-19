package ml.dcxo.x.obwei.service

import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as NotificationMediaCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.request.target.NotificationTarget
import ml.dcxo.x.obwei.R
import ml.dcxo.x.obwei.ui.UniqueActivity
import ml.dcxo.x.obwei.utils.GlideApp

/**
 * Created by David on 13/01/2019 for XOXO
 */
class MusicNotification {

	private lateinit var notificationManager: NotificationManager
	private lateinit var service: MusicService

	var bitmap: Bitmap? = null

	private val notificationId = 0xCD
	private val channelId = "X_OBWEI_NOTIFICATION_CHANNEL_X"

	private fun buildPendingIntent(action: String): PendingIntent =
		PendingIntent.getService(service, 0, Intent(action).apply { component = ComponentName(service, MusicService::class.java) }, 0)

	fun init(service: MusicService) {
		this.service = service
		this.notificationManager = service.getSystemService<NotificationManager>()!!
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChan()
	}
	fun update() {

		val notiBuilder =
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				NotificationCompat.Builder(service, channelId)
			else NotificationCompat.Builder(service)

		val bool = service.cPlaybackStateCompat.state == PlaybackStateCompat.STATE_PLAYING

		val playPendingIntent = buildPendingIntent(MusicService.PLAY)
		val previousPendingIntent = buildPendingIntent(MusicService.PREVIOUS)
		val nextPendingIntent = buildPendingIntent(MusicService.NEXT)
		val deletePendingIntent = buildPendingIntent(MusicService.DELETE)
		val action = Intent(service, UniqueActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
			action = UniqueActivity.OPEN_PLAYER
		}
		val clickPendingIntent = PendingIntent.getActivity(service, 0, action, 0)

		val playAction = NotificationCompat.Action
			.Builder(
				if (bool) R.drawable.icon_pause
				else R.drawable.icon_play, "Play", playPendingIntent).build()
		val previousAction = NotificationCompat.Action
			.Builder(R.drawable.icon_skip_previous, "Previous Song", previousPendingIntent).build()
		val nextAction = NotificationCompat.Action
			.Builder(R.drawable.icon_skip_next, "Next Song", nextPendingIntent).build()

		val noti = notiBuilder.apply {

			val mediaStyle = NotificationMediaCompat.MediaStyle()
				.setMediaSession(service.mSession.sessionToken)
				.setShowActionsInCompactView(1,2)

			setContentIntent(clickPendingIntent)
			setDeleteIntent(deletePendingIntent)

			addAction(previousAction)
			addAction(playAction)
			addAction(nextAction)

			setStyle(mediaStyle)
			setOngoing(bool)
			setContentTitle(service.cSong.title)
			setContentText(service.cSong.artistName)
			setLargeIcon(
				bitmap ?:
				service.resources.getDrawable(R.drawable.drawable_error_album_art_song, null).toBitmap()
			)
			setSmallIcon(R.mipmap.ic_notification)


		}.build()

		if (bool) service.startForeground(notificationId, noti)
		else {
			service.stopForeground(false)
			notificationManager.notify(notificationId, noti)
		}

	}
	fun stop() {

		//bitmap?.recycle()
		service.stopForeground(true)
		notificationManager.cancel(notificationId)

	}

	@RequiresApi(26)
	fun createNotificationChan() {
		if (notificationManager.getNotificationChannel(channelId) == null) {

			val notificationChannel = NotificationChannel(
				channelId,
				"X_MOOLIC_CHANNEL_X",
				NotificationManager.IMPORTANCE_DEFAULT
			).apply {
				description = "Moolic channel for player notification"
				enableLights(false)
				enableVibration(false)
			}
			notificationManager.createNotificationChannel(notificationChannel)

		}
	}

}