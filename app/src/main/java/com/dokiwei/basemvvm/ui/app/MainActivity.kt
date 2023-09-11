package com.dokiwei.basemvvm.ui.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dokiwei.basemvvm.R
import com.dokiwei.basemvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
    }
}