package com.finkid.presentation.screens.teacher

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.finkid.MainActivity
import com.finkid.databinding.FragmentTeacherAnswersBinding
import com.finkid.presentation.adapter.TeacherHomeworkAdapter

class TeacherAnswersFragment : Fragment() {
    private val binding by lazy { FragmentTeacherAnswersBinding.inflate(layoutInflater) }
    private val teacherViewModel by lazy { (requireActivity() as MainActivity).teacherViewModel }
    private val name by lazy { requireActivity().intent.getStringExtra("name") ?: "" }
    private val userEmail by lazy { requireActivity().intent.getStringExtra("email") ?: "" }
    private val scheduleList by lazy {
        teacherViewModel.scheduleList.value?.filter { it.students.contains(userEmail) && it.lesson != null }
            ?: listOf()
    }
    private val homeworkAdapter by lazy {
        TeacherHomeworkAdapter(
            adapterList = scheduleList,
            userEmail = userEmail,
            downloadDocument = { url, title ->
                downloadFile(url, title)
            }
        )
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
        initTitle()
        initListeners()
        initRecyclerView()
    }

    private fun initTitle() {
        binding.title.text = name
    }

    private fun initRecyclerView() {
        binding.itemsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.itemsRv.adapter = homeworkAdapter
    }

    private fun initListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun downloadFile(document: String?, theme: String) {
        document?.let { url ->
            val downloadManager =
                requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)
            request.setTitle(theme)
            request.setDescription("Результат урока $theme")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalFilesDir(context, null, "FinKid_${theme}")
            downloadManager.enqueue(request)
        }
    }
}