package com.dokiwei.basemvvm.ui.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dokiwei.basemvvm.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }

    }
}