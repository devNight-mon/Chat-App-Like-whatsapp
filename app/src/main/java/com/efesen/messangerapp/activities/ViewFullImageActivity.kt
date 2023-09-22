package com.efesen.messangerapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.efesen.messangerapp.databinding.ActivityViewFullImageBinding
import com.squareup.picasso.Picasso

class ViewFullImageActivity : AppCompatActivity() {
private lateinit var binding: ActivityViewFullImageBinding

    private var imageViewer: ImageView? = null
    private var imageUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUrl = intent.getStringExtra("url").toString()
        imageViewer = binding.imageViewer

        Picasso.get().load(imageUrl).into(imageViewer)
    }
}