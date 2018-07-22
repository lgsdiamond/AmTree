package com.lgsdiamond.amtree.Amway

import com.lgsdiamond.amtree.TreeView.TreeView
import kotlin.math.roundToLong

enum class MemberPIN(val fullName: String) { NONE("ABO"),
    SP("Silver Producer"), GP("Gold Producer"),
    PT("Platinum"), F_PT("Founders Platinum"),
    RB("Ruby"), F_RB("Founders Ruby"),
    SA("Sapphire"), F_SA("Founders Sapphire"),
    EM("Emerald"), F_EM("Founders Emerald"),
    DIA("Diamond"), F_DIA("Founders Diamond"),
    EDC("Executive Diamond"), F_EDC("Founders Executive Diamond"),
    DDC("Double Diamond"), F_DDC("Founders Double Diamond"),
    TDC("Triple Diamond"), F_TDC("Founders Triple Diamond"),
    CR("Crown"), F_CR("Founders Crown"),
    CA("Crown Ambassador"), F_CA("Founders Crown Ambassador")
}

abstract class Member(val name: String) {
    protected var sponsor: ABO? = null
    val isTopSponsor: Boolean
        get() = (sponsor == null)

    var ownPV = 0.0f

    override fun toString(): String = when (this@Member) {
        is ABO -> "(ownPV=$ownPV) ABO[$name]"
        is OnMember -> "(ownPV=$ownPV) OnMember[$name]"
        else -> "(ownPV=$ownPV) OffMember[$name]"
    }

    fun addPV(pv: Float) {
        ownPV += pv
        sponsor?.addSubPV(pv)
    }

    fun assignSponsor(abo: ABO) {
        sponsor = abo
    }

    companion object {
        var countABO = 0
        var countOnMember = 0
        var countOffMember = 0
        var countMemberID = 0
    }
}

// OffMember does not have id
class OffMember(name: String) : Member(name) {
}

// OnMember has an unique online id
open class OnMember(name: String, private val id: Int) : Member(name) {
    override fun toString(): String {
        return "${super.toString()} with ID=$id"
    }
}

// ABO is an OnMember who can have partner(ABO), OnMembers, OffMembers
class ABO(name: String, id: Int, abo: ABO? = null) : OnMember(name, id) {
    var subPV: Float = 0.0f
    val subMembers = ArrayList<Member>()
    val totalPV: Float
        get() = (ownPV + subPV)

    override fun toString(): String {
        return "[totalPV = $totalPV] (subPV=$subPV) ${super.toString()} has ${subMembers.size} subMembers, firstBonus = ${computeFirstBonus()}"
    }

    fun addSubPV(pv: Float) {
        subPV += pv
        sponsor?.addSubPV(pv)
    }

    fun addSubMember(member: Member, pv: Float = 0.0f) {
        member.assignSponsor(this)
        subMembers.add(member)
        member.addPV(pv)
    }

    fun addDummyOffMember(pv: Float = 0.0f): ABO {
        addSubMember(OffMember("OffMember-${++countOffMember}"), pv)
        return this
    }

    fun addDummyOnMember(pv: Float = 0.0f): ABO {
        addSubMember(OnMember("OnMember-${++countOnMember}", ++countMemberID), pv)
        return this
    }

    fun addDummyABO(pv: Float = 0.0f): ABO {
        val abo = ABO("ABO-${++countABO}", ++countMemberID)
        addSubMember(abo, pv)
        return this
    }

    fun newDummyABO(pv: Float = 0.0f): ABO {
        val abo = ABO("ABO-${++countABO}", ++countMemberID)
        addSubMember(abo, pv)
        return abo
    }

    fun computeFirstBonus(): Float {
        var totalBonus: Float = FirstBonus.computeFirstBonus(totalPV)
        val ABOs = subMembers.filter { it is ABO }
        for (abo in ABOs) {
            totalBonus -= FirstBonus.computeFirstBonus((abo as ABO).totalPV)
        }
        totalBonus = ((totalBonus * 1_000.0f).roundToLong().toFloat()) / 1_000.0f
        return totalBonus
    }
}