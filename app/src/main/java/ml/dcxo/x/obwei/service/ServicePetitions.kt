package ml.dcxo.x.obwei.service

/**
 * Created by David on 08/11/2018 for XOXO
 */
interface ServicePetitions {

	fun openPlayer()
	fun closePlayer(destroy: Boolean = true): Boolean

}