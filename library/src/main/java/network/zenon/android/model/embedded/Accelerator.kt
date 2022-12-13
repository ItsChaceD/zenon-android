package network.zenon.android.model.embedded

import network.zenon.android.model.primitives.Address
import network.zenon.android.model.primitives.Hash
import network.zenon.android.utils.AmountUtils
import network.zenon.android.utils.QSR_DECIMALS
import network.zenon.android.utils.ZNN_DECIMALS
import org.json.JSONObject

enum class AcceleratorProjectStatus {
    VOTING,
    ACTIVE,
    PAID,
    CLOSED,
    COMPLETED,
}

enum class AcceleratorProjectVote {
    YES,
    NO,
    ABSTAIN,
}

abstract class AcceleratorProject(
    open var id: Hash,
    open var name: String,
    open var description: String,
    open var url: String,
    open var znnFundsNeeded: Int,
    open var qsrFundsNeeded: Int,
    open var creationTimestamp: Int,
    open var statusInt: Int,
    open var voteBreakdown: VoteBreakdown
) {
    val status get() = AcceleratorProjectStatus.values()[statusInt]
    val znnFundsNeededWithDecimals get() = AmountUtils.addDecimals(znnFundsNeeded.toLong(), ZNN_DECIMALS.toLong())
    val qsrFundsNeededWithDecimals get() = AmountUtils.addDecimals(qsrFundsNeeded.toLong(), QSR_DECIMALS.toLong())
}

data class Phase(
    override var id: Hash,
    var projectId: Hash,
    override var name: String,
    override var description: String,
    override var url: String,
    override var znnFundsNeeded: Int,
    override var qsrFundsNeeded: Int,
    override var creationTimestamp: Int,
    var acceptedTimestamp: Int,
    override var statusInt: Int,
    override var voteBreakdown: VoteBreakdown,
): AcceleratorProject(
    id = id,
    name = name,
    description = description,
    url = url,
    znnFundsNeeded = znnFundsNeeded,
    qsrFundsNeeded = qsrFundsNeeded,
    creationTimestamp = creationTimestamp,
    statusInt = statusInt,
    voteBreakdown = voteBreakdown
) {
    companion object {
        fun fromJson(json: JSONObject): Phase {
            val phaseJson = json.getJSONObject("phase")
            return Phase(
                id = Hash.parse(phaseJson.getString("id")),
                projectId = Hash.parse(phaseJson.getString("projectId")),
                name = phaseJson.getString("name"),
                description = phaseJson.getString("description"),
                url = phaseJson.getString("url"),
                znnFundsNeeded = phaseJson.getInt("znnFundsNeeded"),
                qsrFundsNeeded = phaseJson.getInt("qsrFundsNeeded"),
                creationTimestamp = phaseJson.getInt("creationTimestamp"),
                acceptedTimestamp = phaseJson.getInt("acceptedTimestamp"),
                statusInt = phaseJson.getInt("statusInt"),
                voteBreakdown = VoteBreakdown.fromJson(json.getJSONObject("votes"))
            )
        }
    }
    
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id.toString())
        json.put("projectId", projectId.toString())
        json.put("name", name)
        json.put("description", description)
        json.put("url", url)
        json.put("znnFundsNeeded", znnFundsNeeded)
        json.put("qsrFundsNeeded", qsrFundsNeeded)
        json.put("creationTimestamp", creationTimestamp)
        json.put("acceptedTimestamp", acceptedTimestamp)
        json.put("status", statusInt)
        json.put("votes", voteBreakdown.toString())
        return json
    }
}

data class Project(
    override var id: Hash,
    override var name: String,
    var owner: Address,
    override var description: String,
    override var url: String,
    override var znnFundsNeeded: Int,
    override var qsrFundsNeeded: Int,
    override var creationTimestamp: Int,
    var lastUpdateTimestamp: Int,
    override var statusInt: Int,
    var phaseIds: List<Hash>,
    override var voteBreakdown: VoteBreakdown,
    var phases: List<Phase>
): AcceleratorProject(
    id = id,
    name = name,
    description = description,
    url = url,
    znnFundsNeeded = znnFundsNeeded,
    qsrFundsNeeded = qsrFundsNeeded,
    creationTimestamp = creationTimestamp,
    statusInt = statusInt,
    voteBreakdown = voteBreakdown
) {
    companion object {
        fun fromJson(json: JSONObject): Project {
            val phasesJson = json.getJSONArray("phases")
            val phaseIdsJson = json.getJSONArray("phaseIds")

            val phases = mutableListOf<Phase>()
            val phaseIds = mutableListOf<Hash>()

            for(i in 0 until phasesJson.length()) {
                phases.add(Phase.fromJson(phasesJson.getJSONObject(i)))
            }

            for(i in 0 until phaseIdsJson.length()) {
                phaseIds.add(Hash.parse(phasesJson.getString(i)))
            }

            return Project(
                id = Hash.parse(json.getString("id")),
                owner = Address.parse(json.getString("owner")),
                name = json.getString("name"),
                description = json.getString("description"),
                url = json.getString("url"),
                znnFundsNeeded = json.getInt("znnFundsNeeded"),
                qsrFundsNeeded = json.getInt("qsrFundsNeeded"),
                creationTimestamp = json.getInt("creationTimestamp"),
                lastUpdateTimestamp = json.getInt("lastUpdateTimestamp"),
                statusInt = json.getInt("statusInt"),
                voteBreakdown = VoteBreakdown.fromJson(json.getJSONObject("votes")),
                phases = phases,
                phaseIds = phaseIds
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("id", id.toString())
        json.put("owner", owner.toString())
        json.put("name", name)
        json.put("description", description)
        json.put("url", url)
        json.put("znnFundsNeeded", znnFundsNeeded)
        json.put("qsrFundsNeeded", qsrFundsNeeded)
        json.put("creationTimestamp", creationTimestamp)
        json.put("lastUpdateTimestamp", lastUpdateTimestamp)
        json.put("status", statusInt)
        json.put("phaseIds", phaseIds.toString())
        return json
    }

    fun getPaidZnnFunds(): Int {
        var amount = 0
        phases.forEach {
            if (it.status == AcceleratorProjectStatus.PAID) {
                amount += it.znnFundsNeeded
            }
        }
        return amount
    }

    fun getPendingZnnFunds(): Int {
        if(phases.isEmpty())    return 0
        val lastPhase = phases.lastOrNull()
        return if(null != lastPhase && lastPhase.status == AcceleratorProjectStatus.ACTIVE)
            lastPhase.znnFundsNeeded else 0
    }

    fun getRemainingZnnFunds(): Int {
        return if(phases.isEmpty()) znnFundsNeeded else znnFundsNeeded - getPaidZnnFunds()
    }

    fun getPaidQsrFunds(): Int {
        var amount = 0

        phases.forEach {
            if (it.status == AcceleratorProjectStatus.PAID) {
                amount += it.qsrFundsNeeded
            }
        }

        return amount
    }

    fun getPendingQsrFunds(): Int {
        if(phases.isEmpty())    return 0
        val lastPhase = phases.lastOrNull()
        return if(null != lastPhase && lastPhase.status == AcceleratorProjectStatus.ACTIVE)
            lastPhase.qsrFundsNeeded else 0
    }

    fun getRemainingQsrFunds(): Int {
        return if(phases.isEmpty()) qsrFundsNeeded else qsrFundsNeeded - getPaidQsrFunds()
    }
}

data class ProjectList(
    var count: Int,
    var list: List<Project>
) {
    companion object {
        fun fromJson(json: JSONObject): ProjectList {
            val listJson = json.getJSONArray("list")
            val list = mutableListOf<Project>()
            for(i in 0 until listJson.length()) {
                list.add(Project.fromJson(listJson.getJSONObject(i)))
            }

            return ProjectList(
                count = json.getInt("count"),
                list = list
            )
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        val mappedList = list.map { it.toJson() }
        json.put("count", count)
        json.put("list", mappedList)
        return json
    }
}