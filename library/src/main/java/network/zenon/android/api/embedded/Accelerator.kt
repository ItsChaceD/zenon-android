package network.zenon.android.api.embedded

import network.zenon.android.client.Client
import network.zenon.android.client.RPC_MAX_PAGE_SIZE
import network.zenon.android.embedded.Definitions
import network.zenon.android.embedded.PROJECT_CREATION_FEE_IN_ZNN
import network.zenon.android.model.embedded.*
import network.zenon.android.model.nom.AccountBlockTemplate
import network.zenon.android.model.primitives.ACCELERATOR_ADDRESS
import network.zenon.android.model.primitives.Hash
import network.zenon.android.model.primitives.TokenStandard
import network.zenon.android.model.primitives.ZNN_ZTS
import network.zenon.android.utils.AmountUtils
import network.zenon.android.utils.ZNN_DECIMALS
import org.json.JSONObject

class AcceleratorApi {
    lateinit var client: Client

    // RPC
    fun getAll(pageIndex: Int = 0, pageSize: Int = RPC_MAX_PAGE_SIZE): ProjectList {
        val response = client.sendRequest("embedded.accelerator.getAll", listOf(pageIndex, pageSize))
        return ProjectList.fromJson(response as JSONObject)
    }

    fun getProjectById(id: String): Project {
        val response = client.sendRequest("embedded.accelerator.getProjectById", listOf(id))
        return Project.fromJson(response as JSONObject)
    }

    fun getPhaseById(id: Hash): Phase {
        val response = client.sendRequest("embedded.accelerator.getPhaseById", listOf(id))
        return Phase.fromJson(response as JSONObject)
    }

    fun getPillarVotes(name: String, hashes: List<String>): List<PillarVote?> {
        val response = client.sendRequest("embedded.accelerator.getPillarVotes", listOf(name, hashes)) as List<*>
        return response
            .map { if (it == null) null else PillarVote.fromJson(it as JSONObject) }
            .toList()
    }

    fun getVoteBreakdown(id: Hash): VoteBreakdown {
        val response = client.sendRequest("embedded.accelerator.getVoteBreakdown", listOf(id.toString()))
        return VoteBreakdown.fromJson(response as JSONObject)
    }

    // Contract methods
    fun createProject(name: String, description: String, url: String, znnFundsNeeded: Int, qsrFundsNeeded: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS,
            ZNN_ZTS,
            AmountUtils.extractDecimals(PROJECT_CREATION_FEE_IN_ZNN.toDouble(), ZNN_DECIMALS.toLong()).toInt(),
            Definitions.accelerator.encodeFunction("CreateProject",
                listOf(name, description, url, znnFundsNeeded, qsrFundsNeeded))
        )
    }

    fun addPhase(id: Hash, name: String, description: String, url: String, znnFundsNeeded: Int, qsrFundsNeeded: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.accelerator.encodeFunction("AddPhase", listOf(id.hash, name, description, url, znnFundsNeeded, qsrFundsNeeded))
        )
    }

    fun updatePhase(id: Hash, name: String, description: String, url: String, znnFundsNeeded: Int, qsrFundsNeeded: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.accelerator.encodeFunction("UpdatePhase", listOf(id.hash, name, description, url, znnFundsNeeded, qsrFundsNeeded))
        )
    }

    fun donate(amount: Int, zts: TokenStandard): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS, zts, amount,
            Definitions.accelerator.encodeFunction("Donate", listOf<Any>()))
    }

    fun voteByName(id: Hash, pillarName: String, vote: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.accelerator
                .encodeFunction("VoteByName", listOf(id.hash, pillarName, vote))
        )
    }

    fun voteByProdAddress(id: Hash, vote: Int): AccountBlockTemplate {
        return AccountBlockTemplate.callContract(
            ACCELERATOR_ADDRESS,
            ZNN_ZTS,
            0,
            Definitions.accelerator
                .encodeFunction("VoteByProdAddress", listOf(id.hash, vote))
        )
    }
}