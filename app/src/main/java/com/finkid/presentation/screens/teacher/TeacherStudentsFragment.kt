package com.finkid.presentation.screens.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.databinding.FragmentTeacherStudentsBinding
import com.finkid.network.results.NetworkResult
import com.finkid.presentation.adapter.StudentsAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText

class TeacherStudentsFragment : Fragment() {
    private val binding by lazy { FragmentTeacherStudentsBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
    private val studentsAdapter by lazy {
        StudentsAdapter(selectItem = { item ->
            requireActivity().intent.putExtra("email", item.email)
            requireActivity().intent.putExtra("name", item.name)
            findNavController().navigate(R.id.teacherAnswersFragment)
        }, removeItem = { item ->
            item.email.let {
                databaseManager.removeStudentFromTeacher(
                    teacherViewModel,
                    it,
                    result = { message, success ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    })
            }
        })
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
        initViewModel()
        initListeners()
        initRecyclerView()
    }

    private fun initViewModel() {
        teacherViewModel.teacher.observe(viewLifecycleOwner) { value ->
            value?.let { teacherDto ->
                val filteredStudents =
                    teacherViewModel.studentsList.value?.filter { teacherDto.studentIds.contains(it.email) }
                filteredStudents?.let { studentsAdapter.updateList(it.sortedByDescending { it.rating }) }
            }
        }
    }

    private fun initRecyclerView() {
        binding.itemsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.itemsRv.adapter = studentsAdapter
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnAdd.setOnClickListener {
            showBottomAdd()
        }
    }

    private fun showBottomAdd() {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_add_student, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(true)
        bottomDialog.show()
        val bottomAdd = dialogView.findViewById<Button>(R.id.bottom_add)
        val bottomInputName =
            dialogView.findViewById<TextInputEditText>(R.id.bottom_input_name)
        val bottomInputEmail =
            dialogView.findViewById<TextInputEditText>(R.id.bottom_input_email)
        val bottomInputPassword =
            dialogView.findViewById<TextInputEditText>(R.id.bottom_input_password)
        bottomAdd.setOnClickListener {
            val email = bottomInputEmail.text.toString().trim().lowercase()
            val password = bottomInputPassword.text.toString().trim()
            val name = bottomInputName.text.toString().trim()
            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            bottomAdd.isEnabled = false
            databaseManager.createStudent(
                teacherViewModel,
                name,
                email,
                password,
                result = { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            bottomDialog.dismiss()
                        }

                        is NetworkResult.Failure -> {
                            bottomAdd.isEnabled = true
                            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
        }
    }
}