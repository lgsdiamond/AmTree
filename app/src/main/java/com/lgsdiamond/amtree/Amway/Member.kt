package com.lgsdiamond.amtree.Amway

import com.lgsdiamond.amtree.TreeView.TreeNode
import kotlin.math.roundToLong

abstract class Member(val name: String, var ownPV: Float = 0.0f) {
    lateinit var amNode: AmNode
    open fun toTitle(): CharSequence = name
    abstract fun toDesc(): CharSequence

    val sponsor: ABO?
        get() = (if (amNode.parent == null) null else amNode.parent!!.data as ABO)
    val isTopSponsor: Boolean
        get() = (sponsor == null)
    var subPV = 0.0f
    val totalPV: Float
        get() = (ownPV + subPV)

    override fun toString(): String = when (this@Member) {
        is ABO -> "(ownPV=$ownPV) ABO[$name]"
        is OnMember -> "(ownPV=$ownPV) OnMember[$name]"
        else -> "(ownPV=$ownPV) OffMember[$name]"
    }

    fun addOwnPV(pv: Float) {
        ownPV += pv
        sponsor?.addSubPV(pv)
    }

    fun addPVtoSponsor() {
        sponsor?.addSubPV(totalPV)
    }

    companion object {
        var countABO = 0
        var countOnMember = 0
        var countOffMember = 0
        var countMemberID = 0
    }
}

// OffMember does not have id
class OffMember(name: String, ownPV: Float = 20.0f) : Member(name, ownPV) {
    override fun toDesc(): CharSequence = "PV($ownPV)"
}

// OnMember has an unique online id
open class OnMember(name: String, ownPV: Float = 20.0f, private val id: Int = 0) : Member(name, ownPV) {
    override fun toDesc(): CharSequence = "PV($ownPV)"
}

// ABO is an OnMember who can have partner(ABO), OnMembers, OffMembers
class ABO(name: String, ownPV: Float = 20.0f, id: Int = 0) : OnMember(name, ownPV, id) {
    override fun toDesc(): CharSequence = "PV($totalPV/$ownPV), B(${computeFirstBonus()})"
    override fun toString(): String {
        return "[PV=$totalPV] (sub=$subPV) ${super.toString()} has ${amNode.children.size} subMembers, firstBonus = ${computeFirstBonus()}"
    }

    fun addSubPV(pv: Float) {
        subPV += pv
        sponsor?.addSubPV(pv)
    }

    fun computeFirstBonus(): Float {
        var totalBonus: Float = FirstBonus.computeFirstBonus(totalPV)
        val aboChildren = amNode.children.filter { it.data is ABO }
        for (node in aboChildren) {
            val abo = node.data as ABO
            totalBonus -= FirstBonus.computeFirstBonus(abo.totalPV)
        }
        totalBonus = ((totalBonus * 1_000.0f).roundToLong().toFloat()) / 1_000.0f
        return totalBonus
    }
}

class AmNode(member: Member) : TreeNode(member) {
    init {
        member.amNode = this@AmNode
    }

    override fun addChild(child: TreeNode) {
        super.addChild(child)

        val member = child.data as Member
        member.addPVtoSponsor()
    }
}