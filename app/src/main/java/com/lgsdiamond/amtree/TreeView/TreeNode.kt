package com.lgsdiamond.amtree.TreeView

import java.util.*

interface TreeNodeObserver {
    fun notifyDataChanged(node: TreeNode)
    fun notifyNodeAdded(node: TreeNode, parent: TreeNode)
    fun notifyNodeRemoved(node: TreeNode, parent: TreeNode)
}

open class TreeNode @JvmOverloads constructor(private var mData: Any? = null) {
    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var level: Int = 0
    var nodeCount = 1
    var parent: TreeNode? = null
    private val mChildren = ArrayList<TreeNode>()
    private val mTreeNodeObservers = ArrayList<TreeNodeObserver>()

    var data: Any?
        get() = mData
        set(data) {
            mData = data

            for (observer in treeNodeObservers) {
                observer.notifyDataChanged(this)
            }
        }

    val children: List<TreeNode>
        get() = mChildren

    private val treeNodeObservers: ArrayList<TreeNodeObserver>
        get() {
            var observers = mTreeNodeObservers
            if (observers.isEmpty() && parent != null) {
                observers = parent!!.treeNodeObservers
            }
            return observers
        }

    internal fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    val hasData: Boolean
        get() = (mData != null)
    val hasChildren: Boolean
        get() = mChildren.isNotEmpty()
    val hasParent: Boolean
        get() = (parent != null)

    private fun notifyParentNodeCountChanged() {
        if (parent != null) {
            parent!!.notifyParentNodeCountChanged()
        } else {
            calculateNodeCount()
        }
    }

    private fun calculateNodeCount(): Int {
        var size = 1

        for (child in mChildren) {
            size += child.calculateNodeCount()
        }

        nodeCount = size
        return nodeCount
    }

    open fun addChild(child: TreeNode) {
        mChildren.add(child)
        child.parent = this

        notifyParentNodeCountChanged()

        for (observer in treeNodeObservers) {
            observer.notifyNodeAdded(child, this)
        }
    }

    fun addChildren(vararg children: TreeNode) {
        addChildren(Arrays.asList(*children))
    }

    fun addChildren(children: List<TreeNode>) {
        for (child in children) {
            addChild(child)
        }
    }

    fun removeChild(child: TreeNode) {
        child.parent = null
        mChildren.remove(child)

        notifyParentNodeCountChanged()

        for (observer in treeNodeObservers) {
            observer.notifyNodeRemoved(child, this)
        }
    }

    internal fun addTreeNodeObserver(observer: TreeNodeObserver) {
        mTreeNodeObservers.add(observer)
    }

    internal fun removeTreeNodeObserver(observer: TreeNodeObserver) {
        mTreeNodeObservers.remove(observer)
    }

    override fun toString(): String {
        var indent = "\t"
        for (i in 0 until y / 10) {
            indent += indent
        }
        return "\n" + indent + "TreeNode{" +
                " data=" + mData +
                ", mX=" + x +
                ", mY=" + y +
                ", mChildren=" + mChildren +
                '}'.toString()
    }

    fun isFirstChild(node: TreeNode): Boolean {
        return mChildren.indexOf(node) == 0
    }
}