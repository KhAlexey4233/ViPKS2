package com.finkid.presentation.screens.student

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.finkid.MainActivity
import com.finkid.databinding.FragmentStudentProfileBinding
import com.finkid.network.dto.StudentDto

class StudentProfileFragment : Fragment() {
    private val binding by lazy { FragmentStudentProfileBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private val studentsViewModel by lazy { (requireActivity() as MainActivity).studentViewModel }
    private var imageUri: Uri? = null
    private val pickPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                imageUri = uri
                Glide.with(requireActivity()).load(uri).into(binding.image)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initProfile()
        initListeners()
    }

    private fun initProfile() {
        studentsViewModel.student.observe(viewLifecycleOwner) { value ->
            value?.let { teacherDto ->
                binding.inputEmail.setText(teacherDto.email.replace("-", ".").replace("_", "@"))
                teacherDto.name.let { binding.inputFullname.setText(it) }
                teacherDto.phone?.let { binding.inputPhone.setText(it) }
                teacherDto.birthDate?.let { binding.inputBirthDate.setText(it) }
                teacherDto.image?.let {
                    Glide.with(requireActivity()).load(it).into(binding.image)
                    imageUri = Uri.parse(it)
                }
            }
        }
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnSave.setOnClickListener {
            trySaveProfile()
        }
        binding.image.setOnClickListener {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun fieldsIsNotEmpty(stringList: List<String>): Boolean {
        return !stringList.any { it.isEmpty() }
    }

    private fun trySaveProfile() {
        val fullname = binding.inputFullname.text.toString().trim()
        val phone = binding.inputPhone.text.toString().trim()
        val birthDate = binding.inputBirthDate.text.toString().trim()
        if (fieldsIsNotEmpty(listOf(fullname, phone))) {
            binding.btnSave.isEnabled = false
            val student = StudentDto(
                name = fullname,
                phone = phone,
                email = databaseManager.getUserEmail(),
                image = imageUri?.toString(),
                birthDate = birthDate.ifEmpty { null },
            )
            if (studentsViewModel.student.value != student) {
                databaseManager.updateStudentProfile(student, result = { message ->
                    if (isAdded) {
                        binding.btnSave.isEnabled = true
                        studentsViewModel.setStudent(student)
                        Toast.makeText(requireContext(), "$message", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Вы не внесли изменения", Toast.LENGTH_SHORT)
                    .show()
                binding.btnSave.isEnabled = true
            }
        } else {
            Toast.makeText(requireContext(), "Заполните обязательные поля", Toast.LENGTH_SHORT)
                .show()
        }
    }
}