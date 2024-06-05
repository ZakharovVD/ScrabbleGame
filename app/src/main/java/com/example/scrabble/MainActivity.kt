package com.example.scrabble

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        when (view.id) {
            R.id.btnStart -> intent.putExtra(getString(R.string.mode), 1)
            R.id.btnStartCmp -> intent.putExtra(getString(R.string.mode), 2)
            R.id.btnCont -> intent.putExtra(getString(R.string.mode), 3)
        }
        startActivity(intent)
    }
}