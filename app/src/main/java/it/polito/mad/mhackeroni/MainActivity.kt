package it.polito.mad.mhackeroni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    //private lateinit var itemListFragment: ItemListFragment
    private lateinit var showProfileFragment: ShowProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showProfileFragment = ShowProfileFragment()
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, showProfileFragment, "fragHome")
            .commit()
    }
}
