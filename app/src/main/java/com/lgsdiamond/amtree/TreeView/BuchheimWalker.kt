package com.lgsdiamond.amtree.TreeView

import android.content.Context
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.graphics.Point
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*

object AlgorithmFactory {

    fun createBuchheimWalker(configuration: BuchheimWalkerConfiguration): Algorithm {
        return BuchheimWalkerAlgorithm(configuration)
    }

    fun createDefaultBuchheimWalker(): Algorithm {
        return BuchheimWalkerAlgorithm()
    }
}

class BuchheimWalkerConfiguration(val siblingSeparation: Int, val subtreeSeparation: Int)


class BuchheimWalkerAlgorithm @JvmOverloads constructor(private val mConfiguration: BuchheimWalkerConfiguration = BuchheimWalkerConfiguration(DEFAULT_SIBLING_SEPARATION, DEFAULT_SUBTREE_SEPARATION)) : Algorithm {
    private val mNodeData = HashMap<TreeNode, BuchheimWalkerNodeData>()

    private fun createNodeData(node: TreeNode): BuchheimWalkerNodeData {
        val nodeData = BuchheimWalkerNodeData()
        nodeData.ancestor = node
        mNodeData[node] = nodeData

        return nodeData
    }

    private fun getNodeData(node: TreeNode?): BuchheimWalkerNodeData {
        return mNodeData[node]!!
    }

    private fun firstWalk(node: TreeNode, depth: Int, number: Int) {
        val nodeData = createNodeData(node)
        nodeData.depth = depth
        nodeData.number = number

        if (isLeaf(node)) {
            // if the node has no left sibling, prelim(node) should be set to 0, but we don't have to set it
            // here, because it's already initialized with 0
            if (hasLeftSibling(node)) {
                val leftSibling = getLeftSibling(node)
                nodeData.prelim = getPrelim(leftSibling) + getSpacing(leftSibling!!, node)
            }
        } else {
            val leftMost = getLeftMostChild(node)
            val rightMost = getRightMostChild(node)
            var defaultAncestor = leftMost

            var next: TreeNode? = leftMost
            var i = 1
            while (next != null) {
                firstWalk(next, depth + 1, i++)
                defaultAncestor = apportion(next, defaultAncestor)

                next = getRightSibling(next)
            }

            executeShifts(node)

            val midPoint = 0.5 * (getPrelim(leftMost) + getPrelim(rightMost) + rightMost!!.width - node.width)

            if (hasLeftSibling(node)) {
                val leftSibling = getLeftSibling(node)
                nodeData.prelim = getPrelim(leftSibling) + getSpacing(leftSibling!!, node)
                nodeData.modifier = nodeData.prelim - midPoint
            } else {
                nodeData.prelim = midPoint
            }
        }
    }

    private fun secondWalk(node: TreeNode, modifier: Double) {
        val nodeData = getNodeData(node)
        node.x = (nodeData.prelim.toInt() + modifier.toInt())
        node.y = nodeData.depth
        node.level = nodeData.depth

        for (w in node.children) {
            secondWalk(w, modifier + nodeData.modifier)
        }
    }

    private fun executeShifts(node: TreeNode) {
        var shift = 0.0
        var change = 0.0
        var w = getRightMostChild(node)
        while (w != null) {
            val nodeData = getNodeData(w)

            nodeData.prelim = nodeData.prelim + shift
            nodeData.modifier = nodeData.modifier + shift
            change += nodeData.change
            shift += nodeData.shift + change

            w = getLeftSibling(w)
        }
    }

    private fun apportion(node: TreeNode, ancestor: TreeNode): TreeNode {
        var defaultAncestor = ancestor
        if (hasLeftSibling(node)) {
            val leftSibling = getLeftSibling(node)

            var vip = node
            var vop: TreeNode? = node
            var vim = leftSibling
            var vom: TreeNode? = getLeftMostChild(vip.parent!!)

            var sip = getModifier(vip)
            var sop = getModifier(vop)
            var sim = getModifier(vim)
            var som = getModifier(vom)

            var nextRight = nextRight(vim!!)
            var nextLeft = nextLeft(vip)

            while (nextRight != null && nextLeft != null) {
                vim = nextRight
                vip = nextLeft
                vom = nextLeft(vom!!)
                vop = nextRight(vop!!)

                setAncestor(vop, node)

                val shift = getPrelim(vim) + sim - (getPrelim(vip) + sip) + getSpacing(vim, vip)
                if (shift > 0) {
                    moveSubtree(ancestor(vim, node, defaultAncestor), node, shift)
                    sip += shift
                    sop += shift
                }

                sim += getModifier(vim)
                sip += getModifier(vip)
                som += getModifier(vom)
                sop += getModifier(vop)

                nextRight = nextRight(vim)
                nextLeft = nextLeft(vip)
            }

            if (nextRight != null && nextRight(vop!!) == null) {
                setThread(vop, nextRight)
                setModifier(vop, getModifier(vop) + sim - sop)
            }

            if (nextLeft != null && nextLeft(vom!!) == null) {
                setThread(vom, nextLeft)
                setModifier(vom, getModifier(vom) + sip - som)
                defaultAncestor = node
            }
        }

        return defaultAncestor
    }

    private fun setAncestor(v: TreeNode?, ancestor: TreeNode) {
        getNodeData(v).ancestor = ancestor
    }

    private fun setModifier(v: TreeNode, modifier: Double) {
        getNodeData(v).modifier = modifier
    }

    private fun setThread(v: TreeNode, thread: TreeNode) {
        getNodeData(v).thread = thread
    }

    private fun getPrelim(v: TreeNode?): Double {
        return getNodeData(v).prelim
    }

    private fun getModifier(vip: TreeNode?): Double {
        return getNodeData(vip).modifier
    }

    private fun moveSubtree(wm: TreeNode, wp: TreeNode, shift: Double) {
        val wpNodeData = getNodeData(wp)
        val wmNodeData = getNodeData(wm)

        val subtrees = wpNodeData.number - wmNodeData.number
        wpNodeData.change = wpNodeData.change - shift / subtrees
        wpNodeData.shift = wpNodeData.shift + shift
        wmNodeData.change = wmNodeData.change + shift / subtrees
        wpNodeData.prelim = wpNodeData.prelim + shift
        wpNodeData.modifier = wpNodeData.modifier + shift
    }

    private fun ancestor(vim: TreeNode, node: TreeNode, defaultAncestor: TreeNode): TreeNode {
        val vipNodeData = getNodeData(vim)

        return if (vipNodeData.ancestor!!.parent === node.parent) {
            vipNodeData.ancestor!!
        } else defaultAncestor

    }

    private fun nextRight(node: TreeNode): TreeNode? {
        return if (node.hasChildren) {
            getRightMostChild(node)
        } else getNodeData(node).thread

    }

    private fun nextLeft(node: TreeNode): TreeNode? {
        return if (node.hasChildren) {
            getLeftMostChild(node)
        } else getNodeData(node).thread

    }

    private fun getSpacing(leftNode: TreeNode, rightNode: TreeNode): Int {
        return mConfiguration.siblingSeparation + leftNode.width
    }

    private fun isLeaf(node: TreeNode): Boolean {
        return node.children.isEmpty()
    }

    private fun getLeftSibling(node: TreeNode): TreeNode? {
        if (!hasLeftSibling(node)) {
            return null
        }

        val parent = node.parent
        val children = parent!!.children
        val nodeIndex = children.indexOf(node)
        return children[nodeIndex - 1]
    }

    private fun hasLeftSibling(node: TreeNode): Boolean {
        val parent = node.parent ?: return false

        val nodeIndex = parent!!.children.indexOf(node)
        return nodeIndex > 0
    }

    private fun getRightSibling(node: TreeNode): TreeNode? {
        if (!hasRightSibling(node)) {
            return null
        }

        val parent = node.parent
        val children = parent!!.children
        val nodeIndex = children.indexOf(node)
        return children[nodeIndex + 1]
    }

    private fun hasRightSibling(node: TreeNode): Boolean {
        val parent = node.parent ?: return false

        val children = parent.children
        val nodeIndex = children.indexOf(node)
        return nodeIndex < children.size - 1
    }

    private fun getLeftMostChild(node: TreeNode): TreeNode {
        return node.children[0]
    }

    private fun getRightMostChild(node: TreeNode): TreeNode? {
        val children = node.children
        return if (children.isEmpty()) {
            null
        } else children.get(children.size - 1)

    }

    override fun run(root: TreeNode) {
        mNodeData.clear()

        firstWalk(root, 0, 0)
        secondWalk(root, -getPrelim(root))
    }

    companion object {

        private const val DEFAULT_SIBLING_SEPARATION = 100
        private const val DEFAULT_SUBTREE_SEPARATION = 100
    }
}

internal class BuchheimWalkerNodeData {
    var ancestor: TreeNode? = null
    var thread: TreeNode? = null
    var number: Int = 0
    var depth: Int = 0
    var prelim: Double = 0.toDouble()
    var modifier: Double = 0.toDouble()
    var shift: Double = 0.toDouble()
    var change: Double = 0.toDouble()
}

internal object Conditions {

    fun <T> isNonNull(`object`: T?, message: String): T {
        if (`object` == null) {
            throw IllegalArgumentException(message)
        }

        return `object`
    }
}

abstract class BaseTreeAdapter<VH>(context: Context, @param:LayoutRes private val mLayoutRes: Int) : TreeAdapter<VH> {
    private var mRootNode: TreeNode? = null

    private var mAlgorithm: Algorithm? = null

    private val mLayoutInflater: LayoutInflater

    private val mDataSetObservable = DataSetObservable()

    override var algorithm: Algorithm
        get() {
            if (mAlgorithm == null) {
                mAlgorithm = AlgorithmFactory.createDefaultBuchheimWalker()
            }

            return mAlgorithm!!
        }
        set(algorithm) {
            Conditions.isNonNull(algorithm, "algorithm can't be null")

            mAlgorithm = algorithm
        }


    override fun getCount(): Int = if (mRootNode != null) mRootNode!!.nodeCount else 0

    override fun getViewTypeCount(): Int = 0

    override fun isEmpty(): Boolean = false

    init {
        mLayoutInflater = LayoutInflater.from(context)
    }

    override fun notifySizeChanged() {
        if (mRootNode != null) {
            algorithm.run(mRootNode!!)
        }
    }

    override fun setRootNode(rootNode: TreeNode) {
        Conditions.isNonNull(rootNode, "rootNode can't be null")

        if (mRootNode != null) {
            mRootNode!!.removeTreeNodeObserver(this)
        }

        mRootNode = rootNode
        mRootNode!!.addTreeNodeObserver(this)
        notifyDataChanged(mRootNode!!)
    }

    override fun getNode(position: Int): TreeNode? {
        val list = ArrayList<TreeNode>()
        list.add(mRootNode!!)

        return if (mRootNode != null) breadthSearch(list, position) else null
    }

    protected fun breadthSearch(nodes: List<TreeNode>, position: Int): TreeNode {
        if (nodes.size > position) {
            return nodes[position]
        }

        val childNodes = ArrayList<TreeNode>()
        for (n in nodes) {
            for (child in n.children)
                childNodes.add(child)
        }

        return breadthSearch(childNodes, position - nodes.size)
    }

    override fun getScreenPosition(position: Int): Point {
        val node = getNode(position)

        return Point(node!!.x, node.y)
    }

    override fun notifyDataChanged(node: TreeNode) {
        mDataSetObservable.notifyChanged()
    }

    override fun notifyNodeAdded(node: TreeNode, parent: TreeNode) {
        mDataSetObservable.notifyInvalidated()
    }

    override fun notifyNodeRemoved(node: TreeNode, parent: TreeNode) {
        mDataSetObservable.notifyInvalidated()
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.registerObserver(observer)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        mDataSetObservable.unregisterObserver(observer)
    }

    override fun getItem(position: Int): Any {
        return getNode(position)!!.data!!
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: VH

        if (convertView == null) {
            view = mLayoutInflater.inflate(mLayoutRes, parent, false)
            viewHolder = onCreateViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as VH
        }

        val node = getNode(position)
        onBindViewHolder(viewHolder, node!!.data!!, position)

        return view
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }
}
