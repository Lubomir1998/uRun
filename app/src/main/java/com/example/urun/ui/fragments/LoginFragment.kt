package com.example.urun.ui.fragments

import android.app.Activity.MODE_PRIVATE
import android.app.Activity.RESULT_OK
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.urun.R
import com.example.urun.databinding.LoginFragmentBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment: Fragment(R.layout.login_fragment) {

    private lateinit var binding: LoginFragmentBinding

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @set:Inject
    var firstTime = true

    private var isCameraPermissionEnabled = false

    companion object{
        const val CAMERA_REQUEST_CODE = 99
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestCameraPermission()

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
            if(isCameraPermissionEnabled) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
            else{
                requestCameraPermission()
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST_CODE && data?.data != null) {
                val image = data.data
                binding.userImg.setImageURI(image)

                saveImgInSharedPrefs(image)
            }
        }
    }

    private fun requestCameraPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 111)
        }
        else{
            isCameraPermissionEnabled = true
        }
    }

    private fun saveImgInSharedPrefs(uri: Uri?){
        val sharedPreferences = requireContext().getSharedPreferences("shpr", MODE_PRIVATE)

        sharedPreferences.edit()
                .putString("keyUri", uri.toString())
                .apply()
    }


}