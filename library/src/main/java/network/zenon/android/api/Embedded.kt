package network.zenon.android.api

import network.zenon.android.api.embedded.AcceleratorApi
import network.zenon.android.api.embedded.PillarApi
import network.zenon.android.api.embedded.PlasmaApi
import network.zenon.android.api.embedded.SentinelApi
import network.zenon.android.api.embedded.StakeApi
import network.zenon.android.api.embedded.SwapApi
import network.zenon.android.api.embedded.TokenApi
import network.zenon.android.client.Client

class EmbeddedApi {
    var client: Client? = null
        set(value) {
            field = value!!
            pillar.client = value
            plasma.client = value
            sentinel.client = value
            stake.client = value
            swap.client = value
            token.client = value
            accelerator.client = value
        }
    
    var pillar: PillarApi = PillarApi()
    var plasma: PlasmaApi = PlasmaApi()
    var sentinel: SentinelApi = SentinelApi()
    var stake: StakeApi = StakeApi()
    var swap: SwapApi = SwapApi()
    var token: TokenApi = TokenApi()
    var accelerator: AcceleratorApi = AcceleratorApi()
}