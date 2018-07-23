package com.lgsdiamond.amtree

import android.content.Context
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import com.lgsdiamond.amtree.Amway.*
import com.lgsdiamond.amtree.TreeView.BaseTreeAdapter
import com.lgsdiamond.amtree.TreeView.TreeNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

lateinit var gMainActivity: MainActivity
val gMainContext: Context by lazy { gMainActivity.applicationContext }

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    init {
        gMainActivity = this@MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //
        initTrees()
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
                val backColor = when (data) {
                    is ABO -> colorABO
                    is OnMember -> colorOnMember
                    is OffMember -> colorOffMember
                    else -> 0x000000
                }

                viewHolder.mTreeviewMain.setBackgroundColor(backColor)
                if (data is Member) {
                    viewHolder.mTextTitle.text = data.toTitle()
                    viewHolder.mTextDesc.text = data.toDesc()
                } else {
                    viewHolder.mTextTitle.text = data.toString()
                    viewHolder.mTextDesc.text = "Not a Member"
                }
            }
        }

        teeviewMain.setAdapter(adapter)
        btnAddNode.setOnClickListener {
            if (mCurrentNode != null) {
                mCurrentNode!!.addChild(TreeNode(getNodeText()))
            } else {
                adapter.setRootNode(TreeNode(getNodeText()))
            }
        }

        // example tree
        mCurrentNode = AmNode(ABO("S"))
        adapter.setRootNode(mCurrentNode!!)

        // 6-4-2 Model. step-1
        val you = ABO("You")
        mCurrentNode!!.addChild(AmNode(you))

        val yourNode = you.amNode

        for (i in 1..6) {
            yourNode.addChild(AmNode(ABO("A-$i")))
        }
        var bonus = you.computeFirstBonus()

        // 6-4-2 Model. step-2
        for (child in yourNode.children) {
            for (i in 1..4) {
                child.addChild(AmNode(ABO("B-$i")))
            }
        }
        bonus = you.computeFirstBonus()

        // 6-4-2 Model. step-3
        for (son in yourNode.children) {
            for (grandSon in son.children) {
                for (i in 1..2) {
                    grandSon.addChild(AmNode(ABO("C-$i")))
                }
            }
        }
        bonus = you.computeFirstBonus()

        teeviewMain.setOnItemClickListener { parent, view, position, id ->
            mCurrentNode = adapter.getNode(position)
            Snackbar.make(teeviewMain, "Clicked on " + mCurrentNode!!.data!!.toString(), LENGTH_SHORT).show()
        }

    }

    private var mCurrentNode: TreeNode? = null
    private var nodeCount = 0

    private inner class ViewHolder(view: View) {
        var mTextTitle: TextView = view.findViewById(R.id.tvNodeTitle)
        var mTextDesc: TextView = view.findViewById(R.id.tvNodeDesc)
        var mTreeviewMain: CardView = view.findViewById(R.id.card_view)
    }

    private fun getNodeText(): String {
        return "Node " + nodeCount++
    }

    companion object {
        val colorABO: Int by lazy { ContextCompat.getColor(gMainContext, R.color.colorABO) }
        val colorOnMember: Int by lazy { ContextCompat.getColor(gMainContext, R.color.colorOnMember) }
        val colorOffMember: Int by lazy { ContextCompat.getColor(gMainContext, R.color.colorOffMember) }
    }
}
