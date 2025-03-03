package com.finkid.presentation.screens.teacher

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
import com.finkid.databinding.FragmentTeacherProfileBinding
import com.finkid.network.dto.TeacherDto

class TeacherProfileFragment : Fragment() {
    private val binding by lazy { FragmentTeacherProfileBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
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
        teacherViewModel.teacher.observe(viewLifecycleOwner) { value ->
            value?.let { teacherDto ->
                binding.inputEmail.setText(teacherDto.email.replace("-", ".").replace("_", "@"))
                teacherDto.name?.let { binding.inputFullname.setText(it) }
                teacherDto.phone?.let { binding.inputPhone.setText(it) }
                teacherDto.education?.let { binding.inputEducation.setText(it) }
                teacherDto.work?.let { binding.inputWork.setText(it) }
                teacherDto.bio?.let { binding.inputBio.setText(it) }
                teacherDto.services?.let { binding.inputServices.setText(it) }
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
        val education = binding.inputEducation.text.toString().trim()
        val work = binding.inputWork.text.toString().trim()
        val bio = binding.inputBio.text.toString().trim()
        val services = binding.inputServices.text.toString().trim()
        if (fieldsIsNotEmpty(listOf(fullname, phone, education, services))) {
            binding.btnSave.isEnabled = false
            val students = teacherViewModel.teacher.value?.studentIds ?: listOf()
            val teacher = TeacherDto(
                name = fullname,
                phone = phone,
                education = education,
                work = work.ifEmpty { null },
                bio = bio.ifEmpty { null },
                email = databaseManager.getUserEmail(),
                image = imageUri?.toString(),
                services = services,
                studentIds = students,
            )
            if (teacherViewModel.teacher.value != teacher) {
                databaseManager.updateTeacherProfile(teacher, result = { message ->
                    if (isAdded) {
                        binding.btnSave.isEnabled = true
                        teacherViewModel.setTeacher(teacher)
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