package by.brust.musicplayer.ui

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.brust.musicplayer.R
import by.brust.musicplayer.databinding.PlayerFragmentBinding
import by.brust.musicplayer.mediaplayer.isPlaying
import by.brust.musicplayer.model.Track
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment() {

    private var _binding: PlayerFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModels()
    private var currentPlayingTrack: Track? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = PlayerFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.trackList.observe(viewLifecycleOwner) {
            tracks -> if (currentPlayingTrack == null && tracks.isNotEmpty()) {
                currentPlayingTrack = tracks[0]
            updateImage(currentPlayingTrack)
            updateTitle(currentPlayingTrack)
        }
        }

        viewModel.currentPlayingTrack.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            val thisTrack = Track(
                id = it.description.mediaId.toString(),
                title = it.description.title.toString(),
                author = it.description.subtitle.toString(),
                songUri = it.description.mediaUri.toString(),
                imageUri = it.description.iconUri.toString()
            )
            currentPlayingTrack = thisTrack
            updateImage(currentPlayingTrack)
            updateTitle(currentPlayingTrack)
        }

        viewModel.playbackStateCompat.observe(viewLifecycleOwner) {
            binding.playButton.setImageResource( if (it?.isPlaying != true)
                R.drawable.play else R.drawable.pause)
        }

        binding.playButton.setOnClickListener{
            currentPlayingTrack?.let{
                viewModel.playOrToogleTrack(it,true)
            }
        }

        binding.prevButton.setOnClickListener {
            viewModel.skipToPreviousTrack()
        }

        binding.nextButton.setOnClickListener {
            viewModel.skipToNextTrack()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun updateTitle(track: Track?) {
        binding.titleTextview.text = track?.title
        binding.authorTextview.text = track?.author
    }

    private fun updateImage(track: Track?) {
        binding.apply {
            Glide.with(this@PlayerFragment).load(track?.imageUri).centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(songImageImageview)
        }
    }

}