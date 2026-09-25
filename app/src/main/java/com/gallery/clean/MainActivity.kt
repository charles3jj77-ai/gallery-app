package com.gallery.clean

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gallery.clean.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return if (position == 0) PhotosFragment() else AlbumsFragment()
            }
        }

        if (binding.bottomNav.menu.size() == 0) {
            binding.bottomNav.inflateMenu(R.menu.bottom_nav_menu)
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_photos -> {
                    binding.viewPager.setCurrentItem(0, false)
                    true
                }
                R.id.tab_albums -> {
                    binding.viewPager.setCurrentItem(1, false)
                    true
                }
                else -> false
            }
        }
    }
}
