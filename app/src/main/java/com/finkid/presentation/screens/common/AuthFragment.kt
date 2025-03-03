package com.finkid.presentation.screens.common

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.finkid.MainActivity
import com.finkid.R
import com.finkid.databinding.FragmentAuthBinding
import com.finkid.network.results.NetworkResult
import com.finkid.utils.Constant
import com.google.android.material.bottomsheet.BottomSheetDialog

class AuthFragment : Fragment() {
    private val binding by lazy { FragmentAuthBinding.inflate(layoutInflater) }
    private val databaseManager by lazy { (requireActivity() as MainActivity).networkRepository }
    private var userRole = Constant.STUDENT

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initListeners()
    }

    private fun initListeners() {
        binding.btnAuth.setOnClickListener {
            tryAuth()
        }
        binding.btnTeacher.setOnClickListener {
            userRole = Constant.TEACHER
            selectType(it as TextView)
        }
        binding.btnStudent.setOnClickListener {
            userRole = Constant.STUDENT
            selectType(it as TextView)
        }
    }

    private fun selectType(textView: TextView) {
        binding.btnTeacher.apply {
            backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_100))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
        }
        binding.btnStudent.apply {
            backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_100))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
        }
        textView.apply {
            backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_500))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun tryAuth() {
        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Проверьте корректность почты", Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Мин. длина пароля 6 символов", Toast.LENGTH_SHORT)
                .show()
            return
        }
        binding.btnAuth.isEnabled = false
        databaseManager.authUser(email, password) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    showBottomLoading(email)
                }

                is NetworkResult.Failure -> {
                    binding.btnAuth.isEnabled = true
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showBottomLoading(email: String) {
        val bottomDialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.bottom_loading, null)
        bottomDialog.setContentView(dialogView)
        bottomDialog.setCancelable(false)
        bottomDialog.show()
        val bottomTitle = dialogView.findViewById<TextView>(R.id.bottom_title)
        bottomTitle.text = "Подождите немного"
        databaseManager.checkUserProfile(
            email,
            userRole,
        ) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    requireActivity().finish()
                    startActivity(intent)
                }

                is NetworkResult.Failure -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    binding.btnAuth.isEnabled = true
                }
            }
        }
    }
}