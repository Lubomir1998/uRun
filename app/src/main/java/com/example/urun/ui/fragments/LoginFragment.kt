package com.example.urun.ui.fragments

import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.urun.R
import com.example.urun.databinding.LoginFragmentBinding
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.login_fragment) {

    private var cropActivityResultContract = object : ActivityResultContract<Any?, Uri?>(){
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage.activity().getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uri
        }
    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    private lateinit var binding: LoginFragmentBinding
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @set:Inject
    var firstTime = true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract){
            it?.let { uri ->
                binding.userImg.setImageURI(uri)
                saveImgInSharedPrefs(uri)
            }

        }

        if(!firstTime){
            goToMyRunsFragment(savedInstanceState)
        }

        binding.continueBtn.setOnClickListener {
            if(isSetUpUserSuccessful()){
                goToMyRunsFragment(savedInstanceState)
            }
            else{
                Snackbar.make(requireView(), "All fields must be filled!", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.choosePhotoImage.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }


    }

    private fun goToMyRunsFragment(savedInstanceState: Bundle?){
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build()

        findNavController().navigate(R.id.action_loginFragment_to_myRunsFragment, savedInstanceState, navOptions)
    }

    private fun isSetUpUserSuccessful(): Boolean{
        val name = binding.editTextName.text.toString().trim()
        val age = binding.editTextAge.text.toString().trim()
        val weight = binding.editTextWeight.text.toString().trim()
        val img = binding.userImg.drawable


        if(binding.editTextAge.text.isNotEmpty() && binding.editTextName.text.isNotEmpty() && binding.editTextWeight.text.isNotEmpty()){
            sharedPrefs.edit()
                .putString("keyName", name)
                .putInt("keyAge", age.toInt())
                .putFloat("keyWeight", weight.toFloat())
                .putBoolean("keyFirstTime", false)
                .apply()

            return true
        }
        return false
    }


    private fun saveImgInSharedPrefs(uri: Uri?){
        val sharedPreferences = requireContext().getSharedPreferences("shpr", MODE_PRIVATE)

        sharedPreferences.edit()
                .putString("keyUri", uri.toString())
                .apply()
    }


}