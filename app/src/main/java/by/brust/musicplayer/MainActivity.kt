package by.brust.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.brust.musicplayer.databinding.ActivityMainBinding
import by.brust.musicplayer.ui.PlayerFragment
import dagger.hilt.android.AndroidEntryPoint

private lateinit var binding: ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fm = supportFragmentManager.beginTransaction()
            .add(binding.container.id, PlayerFragment())
            .commit()
    }
}