package dev.orestegabo.sequo.core.catalog

import dev.orestegabo.sequo.core.network.NetworkConfig
import io.ktor.client.call.body
import io.ktor.client.request.*
import kotlinx.serialization.Serializable

@Serializable
internal data class DemoRider(val id:String,val name:String)
@Serializable
internal data class DemoJourneyState(val stage:String,val title:String,val nextAction:String?=null,val nextLabel:String?=null,
    val riderName:String?=null,val riders:List<DemoRider> = emptyList(),val note:String="")
@Serializable
internal data class DemoJourneyStep(val action:String,val riderId:String?=null)
