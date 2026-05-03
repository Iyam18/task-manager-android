package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.androidmobileapplicationwithrestapiandwebhosting.MainActivity
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.api.RetrofitClient
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.msg_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(username, password)
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun applyTheme() {
        val themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isDarkMode = themePrefs.getBoolean("isDarkMode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun login(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val credentials = mapOf("username" to username, "password" to password)
                val response = RetrofitClient.instance.login(credentials)

                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    user?.let {
                        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putInt("user_id", it.id)
                            putString("username", it.username)
                            putString("email", it.email)
                            apply()
                        }
                    }
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, response.body()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
