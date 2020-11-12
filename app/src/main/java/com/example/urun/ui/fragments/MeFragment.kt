package com.example.urun.ui.fragments

import android.app.Activity
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MeFragment: Fragment(R.layout.me_fragment) {

    private lateinit var binding: MeFragmentBinding
    private val model: MeViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var name = ""
    private var age = 0
    private var weight = 0f
    private var userImg = ""
    private var isCameraPermissionEnabled = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(!isCameraPermissionEnabled){
            binding.userImageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.me_img))
        }

        model.isEditSelectedLiveData.observe(requireActivity(), { isEditClicked ->
            checkEditState(isEditClicked)
        })

        loadSharedPrefs()

//        checkEditState(isEditImageSelected)

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
            if(isCameraPermissionEnabled){
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 1000)
            }
            else{
                requestCameraPermission()
            }
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

        }
    }

    private fun saveImgInSharedPrefs(uri: Uri?){
        val sharedPreferences = requireContext().getSharedPreferences("shpr", MODE_PRIVATE)

        sharedPreferences.edit()
            .putString("keyUri", uri.toString())
            .apply()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1000 && data?.data != null) {
                val image = data.data
                binding.userImageView.setImageURI(image)

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

}