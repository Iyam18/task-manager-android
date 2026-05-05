package com.example.androidmobileapplicationwithrestapiandwebhosting.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.androidmobileapplicationwithrestapiandwebhosting.R
import com.example.androidmobileapplicationwithrestapiandwebhosting.api.RetrofitClient
import com.example.androidmobileapplicationwithrestapiandwebhosting.databinding.FragmentProfileBinding
import com.example.androidmobileapplicationwithrestapiandwebhosting.ui.auth.LoginActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var userId: Int = -1

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", -1)
        
        // AUTO-FILL: Immediate display from preferences
        binding.etUsername.setText(prefs.getString("username", ""))
        binding.etEmail.setText(prefs.getString("email", ""))

        if (userId != -1) {
            loadProfile()
        }

        // Clicking either the photo or the button now opens the picker
        val openPicker = { pickImageLauncher.launch("image/*") }
        binding.ivProfile.setOnClickListener { openPicker() }
        binding.btnChangePhoto.setOnClickListener { openPicker() }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        setupDarkModeSwitch()
    }

    private fun setupDarkModeSwitch() {
        val themePrefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDarkMode = themePrefs.getBoolean("isDarkMode", false)
        
        binding.switchDarkMode.isChecked = isDarkMode
        updateDarkModeIcon(isDarkMode)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            themePrefs.edit().putBoolean("isDarkMode", isChecked).apply()
            updateDarkModeIcon(isChecked)
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun updateDarkModeIcon(isDark: Boolean) {
        if (isDark) {
            binding.ivDarkModeIcon.setImageResource(android.R.drawable.ic_menu_recent_history) // Using as a moon-ish placeholder
            binding.ivDarkModeIcon.contentDescription = "Dark Mode Active"
        } else {
            binding.ivDarkModeIcon.setImageResource(android.R.drawable.ic_menu_day)
            binding.ivDarkModeIcon.contentDescription = "Light Mode Active"
        }
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getProfile(userId)
                if (response.isSuccessful && _binding != null) {
                    val user = response.body()?.user
                    user?.let {
                        binding.etUsername.setText(it.username)
                        binding.etEmail.setText(it.email)
                        binding.etBio.setText(it.bio ?: "")
                        
                        if (!it.profileImage.isNullOrEmpty()) {
                            val imageUrl = "https://task-manager-backend-qrb6.onrender.com${it.profileImage}"
                            updateProfileImageWithPalette(imageUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, getString(R.string.msg_profile_load_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfile() {
        val email = binding.etEmail.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        lifecycleScope.launch {
            try {
                val data = mapOf("email" to email, "bio" to bio)
                val response = RetrofitClient.instance.updateProfile(userId, data)
                if (response.isSuccessful && _binding != null) {
                    Toast.makeText(context, getString(R.string.msg_profile_update_success), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, getString(R.string.msg_profile_update_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        val file = getFileFromUri(uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.uploadProfileImage(userId, body)
                if (response.isSuccessful && _binding != null) {
                    val newImageUrl = response.body()?.profileImage
                    newImageUrl?.let {
                        val fullUrl = "https://task-manager-backend-qrb6.onrender.com$it"
                        updateProfileImageWithPalette(fullUrl)
                    }
                    Toast.makeText(context, getString(R.string.msg_photo_update_success), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(context, getString(R.string.msg_upload_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateProfileImageWithPalette(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .centerCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    if (_binding == null) return
                    
                    // Set circle cropped image to ImageView
                    Glide.with(this@ProfileFragment)
                        .load(resource)
                        .circleCrop()
                        .into(binding.ivProfile)

                    // Extract color and update cover
                    Palette.from(resource).generate { palette ->
                        if (_binding == null) return@generate

                        val dominantColor = palette?.getDominantColor(
                            ContextCompat.getColor(requireContext(), R.color.primary)
                        ) ?: ContextCompat.getColor(requireContext(), R.color.primary)
                        
                        binding.appBarLayout.setBackgroundColor(dominantColor)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val file = File(requireContext().cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        return file
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
