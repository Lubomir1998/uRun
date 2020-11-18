package com.example.urun.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.urun.R
import com.example.urun.databinding.MeFragmentBinding
import com.example.urun.viewModels.MeViewModel
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MeFragment: Fragment(R.layout.me_fragment) {

    private lateinit var binding: MeFragmentBinding
    private val model: MeViewModel by viewModels()

    private var cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var name = ""
    private var age = 0
    private var weight = 0f
    private var userImg = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideKeyboard(requireActivity())
        binding.userImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.me_img))

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract){
            it?.let { uri ->
                binding.userImageView.setImageURI(uri)
                saveImgInSharedPrefs(uri)
            }

        }

        model.isEditSelectedLiveData.observe(requireActivity(), { isEditClicked ->
            checkEditState(isEditClicked)
        })

        loadSharedPrefs()


        name = sharedPreferences.getString("keyName", "") ?: ""
        age = sharedPreferences.getInt("keyAge", 20)
        weight = sharedPreferences.getFloat("keyWeight", 80f)

        binding.editImage.setOnClickListener {
            model.editButtonClicked()
            model.isEditSelectedLiveData.observe(requireActivity(), {
                checkEditState(it)
            })
            if(binding.nameTextView.text.trim().isEmpty() || binding.ageTextView.text.trim().isEmpty() || binding.weightTextView.text.trim().isEmpty()){
                Snackbar.make(requireView(), "All fields must be filled!", Snackbar.LENGTH_SHORT).show()
            }
            else {
                sharedPreferences.edit()
                    .putString("keyName", binding.nameTextView.text.toString())
                    .putInt("keyAge", binding.ageTextView.text.toString().toInt())
                    .putFloat("keyWeight", binding.weightTextView.text.toString().toFloat())
                    .apply()
            }

        }

        binding.takePhotoImg.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }


        binding.nameTextView.setText(name)
        binding.ageTextView.setText(age.toString())
        binding.weightTextView.setText(weight.toString())
        if(userImg != "") {
            binding.userImageView.setImageURI(userImg.toUri())
        }
        else{
            binding.userImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.me_img))
        }

    }

    private fun loadSharedPrefs(){
        val sharedprefs = requireContext().getSharedPreferences("shpr", MODE_PRIVATE)
        userImg = sharedprefs.getString("keyUri", "") ?: ""
    }

    private fun checkEditState(boolean: Boolean){
        if(boolean){
            binding.editImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.done_img))
            binding.nameTextView.isEnabled = true
            binding.ageTextView.isEnabled = true
            binding.weightTextView.isEnabled = true
            binding.takePhotoImg.visibility = View.VISIBLE
        }
        else{
            binding.editImage.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.edit_img))
            binding.nameTextView.isEnabled = false
            binding.ageTextView.isEnabled = false
            binding.weightTextView.isEnabled = false
            binding.takePhotoImg.visibility = View.GONE
            hideKeyboard(requireActivity())
        }
    }

    private fun saveImgInSharedPrefs(uri: Uri?){
        val sharedPreferences = requireContext().getSharedPreferences("shpr", MODE_PRIVATE)

        sharedPreferences.edit()
            .putString("keyUri", uri.toString())
            .apply()
    }

    private fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}