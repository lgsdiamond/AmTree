package com.lgsdiamond.amtree

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import com.lgsdiamond.amtree.Amway.ABO
import com.lgsdiamond.amtree.TreeView.BaseTreeAdapter
import com.lgsdiamond.amtree.TreeView.TreeNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //
        initTrees()
        //
        initMembers()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    //
    fun initTrees() {

        val adapter = object : BaseTreeAdapter<ViewHolder>(this, R.layout.node) {
            override fun onCreateViewHolder(view: View): ViewHolder {
                return ViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ViewHolder, data: Any, position: Int) {
                viewHolder.mTextView.text = data.toString()
            }
        }

        tvMain.setAdapter(adapter)
        btnAddNode.setOnClickListener {
            if (mCurrentNode != null) {
                mCurrentNode!!.addChild(TreeNode(getNodeText()))
            } else {
                adapter.setRootNode(TreeNode(getNodeText()))
            }
        }

        // example tree
        mCurrentNode = TreeNode(getNodeText())
        mCurrentNode!!.addChild(TreeNode(getNodeText()))
        val child3 = TreeNode(getNodeText())
        child3.addChild(TreeNode(getNodeText()))
        val child6 = TreeNode(getNodeText())
        child6.addChild(TreeNode(getNodeText()))
        child6.addChild(TreeNode(getNodeText()))
        child3.addChild(child6)
        mCurrentNode!!.addChild(child3)
        val child4 = TreeNode(getNodeText())
        child4.addChild(TreeNode(getNodeText()))
        child4.addChild(TreeNode(getNodeText()))
        mCurrentNode!!.addChild(child4)

        adapter.setRootNode(mCurrentNode!!)
        tvMain.setOnItemClickListener { parent, view, position, id ->
            mCurrentNode = adapter.getNode(position)
            Snackbar.make(tvMain, "Clicked on " + mCurrentNode!!.data!!.toString(), LENGTH_SHORT).show()
        }

    }

    fun initMembers() {
        val mySelf = ABO("TopABO-A", 0)
        mySelf.addDummyABO(10f).addDummyABO(10f).addDummyOnMember(10f).addDummyOnMember(10f).addDummyOffMember(10f).addDummyOnMember(10f).addDummyABO(10f)
                .newDummyABO(10f).addDummyOnMember(10f).addDummyOnMember(10f).addDummyOffMember(10f).addDummyOnMember(10f).addDummyOnMember(10f)
        val yourSelf = ABO("TopABO-B", 1)
        yourSelf.addDummyABO(20f).addDummyABO(20f).addDummyOnMember(20f).addDummyOnMember(20f).addDummyOffMember(20f).addDummyOnMember(20f).addDummyABO(20f)
                .newDummyABO(20f).addDummyOnMember(20f).addDummyOnMember(20f).addDummyOffMember(20f).addDummyOnMember(20f).addDummyOnMember(20f)

        // basic network

        val basicSponsor = ABO("Sponsor", 100)
        val basicYou = ABO("You", 101)
        val basicA = ABO("A", 102)
        val basicB = ABO("B", 103)

        basicSponsor.addSubMember(basicYou, 20.0f)
        basicYou.addSubMember(basicA, 20.0f)
        basicYou.addSubMember(basicB, 20.0f)

        // 6-4-2 Model. step-1
        val you = ABO("You", 1000)
        you.addPV(20.0f)
        for (i in 1..6) {
            you.addDummyABO(20.0f)
        }
        var bonus = you.computeFirstBonus()

        // 6-4-2 Model. step-2
        for (abo in you.subMembers) {
            for (i in 1..4) {
                (abo as ABO).addDummyABO(20.0f)
            }
        }
        bonus = you.computeFirstBonus()

        // 6-4-2 Model. step-3
        for (abo in you.subMembers) {
            for (subABO in (abo as ABO).subMembers) {
                for (i in 1..2) {
                    (subABO as ABO).addDummyABO(20.0f)
                }
            }
        }
        bonus = you.computeFirstBonus()
    }

    private var mCurrentNode: TreeNode? = null
    private var nodeCount = 0

    private inner class ViewHolder(view: View) {
        internal var mTextView: TextView

        init {
            mTextView = view.findViewById(R.id.textView)
        }
    }

    private fun getNodeText(): String {
        return "Node " + nodeCount++
    }

}
