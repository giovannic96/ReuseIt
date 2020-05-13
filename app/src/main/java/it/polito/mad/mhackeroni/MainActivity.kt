package it.polito.mad.mhackeroni

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.mhackeroni.utilities.ImageUtils
import it.polito.mad.mhackeroni.utilities.StorageHelper


class MainActivity : AppCompatActivity(), ShowProfileFragment.OnCompleteListener{

    private val USER_ID = "user_id"
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private lateinit var vm : OnSaleListFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uid = intent.extras?.getString(USER_ID)!!
        vm = ViewModelProvider(this).get(OnSaleListFragmentViewModel::class.java)
        vm.uid = uid

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_showProfile, R.id.nav_itemList, R.id.nav_itemListSale), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.nav_logout -> logout()
                R.id.nav_showProfile -> {
                    val bundle = bundleOf("uid" to uid)
                    navController.navigate(R.id.nav_showProfile, bundle)
                }
            }
            NavigationUI.onNavDestinationSelected(menuItem, navController)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        })
        initialHeader()
        vm.getProfile().observe(this, Observer {
            updateHeader(it)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout(){
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        val i = Intent(this, GoogleSignInActivity::class.java)
        startActivity(i)
    }

    private fun updateHeader(profile: Profile) {
        db = FirebaseFirestore.getInstance()

        val headerView = navView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.drawable_name) as TextView
        val navEmail = headerView.findViewById(R.id.drawable_mail) as TextView
        val navImage = headerView.findViewById(R.id.drawable_pic) as ImageView

        if(profile.fullName.isNullOrEmpty())
            navUsername.text = resources.getString(R.string.defaultFullName)
        else
            navUsername.text = profile.fullName

        if(profile.email.isNullOrEmpty())
            navEmail.text = resources.getString(R.string.defaultEmail)
        else
            navEmail.text = profile.email

        navImage.setImageBitmap(profile.image?.let { ImageUtils.getBitmap(it, this) })
    }

    private fun initialHeader() {
        val headerView = navView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.drawable_name) as TextView
        val navEmail = headerView.findViewById(R.id.drawable_mail) as TextView
        navUsername.text = resources.getString(R.string.defaultFullName)
        navEmail.text = resources.getString(R.string.defaultEmail)
    }

    override fun onComplete(profile: Profile) {
        updateHeader(profile)
    }
}
