package com.finkid.network

import android.content.Context
import android.net.Uri
import android.util.Log
import com.finkid.network.dto.AnswerDto
import com.finkid.network.dto.HomeworkDto
import com.finkid.network.dto.LessonDto
import com.finkid.network.dto.QuestionDto
import com.finkid.network.dto.ScheduleDto
import com.finkid.network.dto.StudentDto
import com.finkid.network.dto.TeacherDto
import com.finkid.network.results.NetworkResult
import com.finkid.presentation.viewmodel.TeacherViewModel
import com.finkid.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NetworkRepository(context: Context) {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDatabase by lazy { FirebaseDatabase.getInstance().reference }
    private val firebaseStorage by lazy { FirebaseStorage.getInstance().reference }
    private val prefs by lazy { context.getSharedPreferences("prefs", Context.MODE_PRIVATE) }

    fun authUser(email: String, password: String, result: (NetworkResult<String?>) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            result(NetworkResult.Success(null))
        }.addOnFailureListener {
            result(NetworkResult.Failure("Кажется, вы ввели неверные данные"))
        }
    }

    fun checkAuth() = firebaseAuth.currentUser != null

    fun getUserRole() = prefs.getString(Constant.ROLE, Constant.STUDENT).toString()

    fun getUserEmail() = prefs.getString("email", "").toString()

    private fun saveType(userRole: String) {
        prefs.edit().putString("email", firebaseAuth.currentUser?.email ?: "").apply()
        prefs.edit().putString(Constant.ROLE, userRole).apply()
    }

    fun signOutUser() {
        firebaseAuth.signOut()
    }

    fun checkUserProfile(
        email: String, userRole: String, result: (NetworkResult<String?>) -> Unit
    ) {
        val formattedEmail = formatEmail(email)
        firebaseDatabase.child(Constant.USERS).child(formattedEmail).let { reference ->
            reference.get().addOnSuccessListener {
                if (it.exists()) {
                    val roleTrue = it.child(Constant.ROLE).value.toString().trim()
                    saveType(roleTrue)
                    result(NetworkResult.Success(null))
                } else {
                    reference.child(Constant.ROLE).setValue(userRole).addOnSuccessListener {
                        saveType(userRole)
                        result(NetworkResult.Success(null))
                    }.addOnFailureListener {
                        result(NetworkResult.Failure("Что-то пошло не так..."))
                    }
                }
            }.addOnFailureListener {
                result(NetworkResult.Failure("Что-то пошло не так..."))
            }
        }
    }

    suspend fun getTeacher(): TeacherDto? {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val formattedEmail = formatEmail(getUserEmail())
                firebaseDatabase.child(Constant.USERS).child(formattedEmail).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val fullname = it.child(Constant.NAME).value?.toString()
                            val phone = it.child(Constant.PHONE).value?.toString()
                            val education = it.child(Constant.EDUCATION).value?.toString()
                            val work = it.child(Constant.WORK).value?.toString()
                            val bio = it.child(Constant.BIO).value?.toString()
                            val services = it.child(Constant.SERVICES).value?.toString()
                            val image = it.child(Constant.IMAGE).value?.toString()
                            var students = listOf<String>()
                            it.child(Constant.STUDENTS).value?.toString()?.let { studentsValue ->
                                students = formatStringToList(studentsValue)
                            }
                            val teacher = TeacherDto(
                                name = fullname,
                                phone = phone,
                                education = education,
                                work = work,
                                bio = bio,
                                services = services,
                                image = image,
                                email = formattedEmail,
                                studentIds = students,
                            )
                            coroutine.resume(teacher)
                        } else {
                            coroutine.resume(null)
                        }
                    }.addOnFailureListener {
                        coroutine.resume(null)
                    }
            }
        }
    }

    suspend fun getStudent(): StudentDto? {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val formattedEmail = formatEmail(getUserEmail())
                firebaseDatabase.child(Constant.USERS).child(formattedEmail).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            val name = it.child(Constant.NAME).value.toString()
                            val phone = it.child(Constant.PHONE).value?.toString()
                            val image = it.child(Constant.IMAGE).value?.toString()
                            val birthDate = it.child(Constant.BIRTH_DATE).value?.toString()
                            val student = StudentDto(
                                name = name,
                                phone = phone,
                                image = image,
                                email = formattedEmail,
                                birthDate = birthDate,
                            )
                            coroutine.resume(student)
                        } else {
                            coroutine.resume(null)
                        }
                    }.addOnFailureListener {
                        coroutine.resume(null)
                    }
            }
        }
    }

    suspend fun getStudentsList(): List<StudentDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val studentsList = mutableListOf<StudentDto>()
                firebaseDatabase.child(Constant.USERS).get().addOnSuccessListener {
                    if (it.exists()) {
                        for (item in it.children) {
                            val role = item.child(Constant.ROLE).value.toString()
                            if (role == Constant.STUDENT) {
                                val email = formatEmail(item.key.toString())
                                val name = item.child(Constant.NAME).value.toString()
                                val phone = item.child(Constant.PHONE).value?.toString()
                                val birthDate = item.child(Constant.BIRTH_DATE).value?.toString()
                                val image = item.child(Constant.IMAGE).value?.toString()
                                val student = StudentDto(
                                    name = name,
                                    phone = phone,
                                    birthDate = birthDate,
                                    image = image,
                                    email = email,
                                )
                                studentsList.add(student)
                            }
                        }
                        coroutine.resume(studentsList)
                    } else {
                        coroutine.resume(studentsList)
                    }
                }.addOnFailureListener {
                    coroutine.resume(studentsList)
                }
            }
        }
    }

    suspend fun getTeacherLessonList(): List<LessonDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val formattedEmail = formatEmail(getUserEmail())
                val lessonsList = mutableListOf<LessonDto>()
                firebaseDatabase.child(Constant.LESSONS).child(formattedEmail).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            for (item in it.children) {
                                val lessonId = item.key.toString().trim()
                                val theme = item.child(Constant.THEME).value.toString().trim()
                                val document = item.child(Constant.DOCUMENT).value.toString().trim()
                                val homework = mutableListOf<QuestionDto>()
                                for (questionItem in item.child(Constant.HOMEWORK).children) {
                                    val question =
                                        questionItem.child(Constant.QUESTION).value.toString()
                                            .trim()
                                    val answer1 =
                                        questionItem.child(Constant.ANSWER_1).value.toString()
                                            .trim()
                                    val answer2 =
                                        questionItem.child(Constant.ANSWER_2).value.toString()
                                            .trim()
                                    val answer3 =
                                        questionItem.child(Constant.ANSWER_3).value.toString()
                                            .trim()
                                    val answerTrue =
                                        questionItem.child(Constant.ANSWER_TRUE).value.toString()
                                            .trim()
                                    val questionDto = QuestionDto(
                                        question = question,
                                        answer1 = answer1,
                                        answer2 = answer2,
                                        answer3 = answer3,
                                        answerTrue = answerTrue
                                    )
                                    homework.add(questionDto)
                                }
                                val lessonDto = LessonDto(
                                    id = lessonId,
                                    theme = theme,
                                    homework = homework,
                                    document = document,
                                )
                                lessonsList.add(lessonDto)
                            }
                            coroutine.resume(lessonsList)
                        } else {
                            coroutine.resume(lessonsList)
                        }
                    }.addOnFailureListener {
                        coroutine.resume(lessonsList)
                    }
            }
        }
    }

    suspend fun getStudentLessonList(scheduleList: List<ScheduleDto>): List<LessonDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val lessonsList = mutableListOf<LessonDto>()
                if (scheduleList.isNotEmpty()) {
                    scheduleList.forEach { schedule ->
                        val creatorEmail = if (schedule.teacherEmail != null) schedule.teacherEmail
                            ?: "error" else schedule.creatorEmail
                        firebaseDatabase.child(Constant.LESSONS).child(creatorEmail).get()
                            .addOnSuccessListener {
                                if (it.exists()) {
                                    for (item in it.children) {
                                        val lessonId = item.key.toString().trim()
                                        val theme =
                                            item.child(Constant.THEME).value.toString().trim()
                                        val document =
                                            item.child(Constant.DOCUMENT).value.toString().trim()
                                        val homework = mutableListOf<QuestionDto>()
                                        for (questionItem in item.child(Constant.HOMEWORK).children) {
                                            val question =
                                                questionItem.child(Constant.QUESTION).value.toString()
                                                    .trim()
                                            val answer1 =
                                                questionItem.child(Constant.ANSWER_1).value.toString()
                                                    .trim()
                                            val answer2 =
                                                questionItem.child(Constant.ANSWER_2).value.toString()
                                                    .trim()
                                            val answer3 =
                                                questionItem.child(Constant.ANSWER_3).value.toString()
                                                    .trim()
                                            val answerTrue =
                                                questionItem.child(Constant.ANSWER_TRUE).value.toString()
                                                    .trim()
                                            val questionDto = QuestionDto(
                                                question = question,
                                                answer1 = answer1,
                                                answer2 = answer2,
                                                answer3 = answer3,
                                                answerTrue = answerTrue
                                            )
                                            homework.add(questionDto)
                                        }
                                        val lessonDto = LessonDto(
                                            id = lessonId,
                                            theme = theme,
                                            homework = homework,
                                            document = document,
                                        )
                                        lessonsList.add(lessonDto)
                                    }
                                }
                            }
                    }
                    coroutine.resume(lessonsList)
                } else {
                    coroutine.resume(lessonsList)
                }
            }
        }
    }

    suspend fun getTeacherScheduleList(): List<ScheduleDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val formattedEmail = formatEmail(getUserEmail())
                val scheduleList = mutableListOf<ScheduleDto>()
                firebaseDatabase.child(Constant.SCHEDULE).child(formattedEmail).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            for (item in it.children) {
                                val id = item.key.toString().trim()
                                val teacherEmail =
                                    item.child(Constant.TEACHER).value?.toString()?.trim()
                                val lesson = item.child(Constant.LESSON).value?.toString()?.trim()
                                val theme = item.child(Constant.THEME).value?.toString()?.trim()
                                val students = item.child(Constant.STUDENTS).value.toString().trim()
                                val subject = item.child(Constant.SUBJECT).value.toString().trim()
                                val timeEnd =
                                    item.child(Constant.TIME_END).value.toString().trim().toLong()
                                val timeStart =
                                    item.child(Constant.TIME_START).value.toString().trim().toLong()
                                val answersList = mutableListOf<AnswerDto>()
                                for (answerItem in item.child(Constant.ANSWERS).children) {
                                    val answer =
                                        answerItem.child(Constant.ANSWER).value.toString().trim()
                                    val question =
                                        answerItem.child(Constant.QUESTION).value.toString().trim()
                                    val isTrue =
                                        answerItem.child(Constant.IS_TRUE).value.toString().trim()
                                            .toBoolean()
                                    val answerDto = AnswerDto(
                                        question = question, answer = answer, isTrue = isTrue
                                    )
                                    answersList.add(answerDto)
                                }
                                val scheduleDto = ScheduleDto(
                                    creatorEmail = formattedEmail,
                                    id = id,
                                    lesson = lesson,
                                    theme = theme,
                                    students = students,
                                    subject = subject,
                                    timeEnd = timeEnd,
                                    timeStart = timeStart,
                                    teacherEmail = teacherEmail
                                )
                                scheduleList.add(scheduleDto)
                            }
                            coroutine.resume(scheduleList)
                        } else {
                            coroutine.resume(scheduleList)
                        }
                    }.addOnFailureListener {
                        coroutine.resume(scheduleList)
                    }
            }
        }
    }

    suspend fun getStudentScheduleList(
        nickname: String
    ): List<ScheduleDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val scheduleList = mutableListOf<ScheduleDto>()
                firebaseDatabase.child(Constant.SCHEDULE).get().addOnSuccessListener {
                    if (it.exists()) {
                        for (item in it.children) {
                            val creatorEmail = item.key.toString().trim()
                            for (teacherItem in item.children) {
                                val students =
                                    teacherItem.child(Constant.STUDENTS).value.toString().trim()
                                if (students.contains(nickname)) {
                                    val id = teacherItem.key.toString().trim()
                                    val teacherEmail =
                                        teacherItem.child(Constant.TEACHER).value?.toString()
                                            ?.trim()
                                    val lesson =
                                        teacherItem.child(Constant.LESSON).value?.toString()?.trim()
                                    val subject =
                                        teacherItem.child(Constant.SUBJECT).value.toString().trim()
                                    val timeEnd =
                                        teacherItem.child(Constant.TIME_END).value.toString().trim()
                                            .toLong()
                                    val timeStart =
                                        teacherItem.child(Constant.TIME_START).value.toString()
                                            .trim().toLong()
                                    val theme =
                                        teacherItem.child(Constant.THEME).value.toString().trim()
                                    val answersList = mutableListOf<AnswerDto>()
                                    for (answerItem in teacherItem.child(Constant.ANSWERS).children) {
                                        val answer =
                                            answerItem.child(Constant.ANSWER).value.toString()
                                                .trim()
                                        val question =
                                            answerItem.child(Constant.QUESTION).value.toString()
                                                .trim()
                                        val isTrue =
                                            answerItem.child(Constant.IS_TRUE).value.toString()
                                                .trim().toBoolean()
                                        val answerDto = AnswerDto(
                                            question = question, answer = answer, isTrue = isTrue
                                        )
                                        answersList.add(answerDto)
                                    }
                                    val scheduleDto = ScheduleDto(
                                        creatorEmail = creatorEmail,
                                        id = id,
                                        lesson = lesson,
                                        theme = theme,
                                        students = students,
                                        subject = subject,
                                        timeEnd = timeEnd,
                                        timeStart = timeStart,
                                        teacherEmail = teacherEmail
                                    )
                                    scheduleList.add(scheduleDto)
                                }
                            }
                        }
                        coroutine.resume(scheduleList)
                    } else {
                        coroutine.resume(scheduleList)
                    }
                }.addOnFailureListener {
                    coroutine.resume(scheduleList)
                }
            }
        }
    }

    suspend fun getHomeworksAll(
    ): List<HomeworkDto> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { coroutine ->
                val tempList = mutableListOf<HomeworkDto>()
                firebaseDatabase.child(Constant.HOMEWORK).get().addOnSuccessListener {
                    if (it.exists()) {
                        for (user in it.children) {
                            val userEmail = user.key.toString().trim()
                            for (item in user.children) {
                                val id = item.key.toString().trim()
                                val document = item.child(Constant.DOCUMENT).value.toString().trim()
                                val answersList = mutableListOf<AnswerDto>()
                                for (answerItem in item.child(Constant.ANSWERS).children) {
                                    val answer =
                                        answerItem.child(Constant.ANSWER).value.toString().trim()
                                    val question =
                                        answerItem.child(Constant.QUESTION).value.toString().trim()
                                    val isTrue =
                                        answerItem.child(Constant.IS_TRUE).value.toString().trim()
                                            .toBoolean()
                                    val newAnswer = AnswerDto(
                                        question = question,
                                        answer = answer,
                                        isTrue = isTrue
                                    )
                                    answersList.add(newAnswer)
                                }
                                val newItem = HomeworkDto(
                                    userEmail = userEmail,
                                    id = id,
                                    document = document,
                                    answersList = answersList
                                )
                                tempList.add(newItem)
                            }
                        }
                        coroutine.resume(tempList)
                    } else {
                        coroutine.resume(tempList)
                    }
                }.addOnFailureListener {
                    coroutine.resume(tempList)
                }
            }
        }
    }

    fun updateTeacherProfile(teacherDto: TeacherDto, result: (String) -> Unit) {
        val formattedEmail = formatEmail(teacherDto.email)
        firebaseDatabase.child(Constant.USERS).child(formattedEmail).let { reference ->
            reference.child(Constant.NAME).setValue(teacherDto.name)
            reference.child(Constant.PHONE).setValue(teacherDto.phone)
            reference.child(Constant.EDUCATION).setValue(teacherDto.education)
            reference.child(Constant.SERVICES).setValue(teacherDto.services)
            teacherDto.work.let { reference.child(Constant.WORK).setValue(it) }
            if (teacherDto.image != null) {
                teacherDto.bio.let { reference.child(Constant.BIO).setValue(it) }
                firebaseStorage.child("users/$formattedEmail").putFile(Uri.parse(teacherDto.image))
                    .addOnSuccessListener { storageReference ->
                        val imageUrl =
                            "https://firebasestorage.googleapis.com/v0/b/classapp-c0f82.appspot.com/o/users%2F$formattedEmail?alt=media"
                        reference.child(Constant.IMAGE).setValue(imageUrl).addOnSuccessListener {
                            result("Данные обновлены")
                        }.addOnFailureListener {
                            result("Произошла ошибка")
                        }
                    }.addOnCanceledListener {
                        result("Произошла ошибка")
                    }
            } else {
                teacherDto.bio.let { reference.child(Constant.BIO).setValue(it) }
                    .addOnSuccessListener {
                        result("Данные обновлены")
                    }.addOnFailureListener {
                        result("Произошла ошибка")
                    }
            }
        }
    }

    fun saveHomeworkResult(
        scheduleDto: ScheduleDto,
        documentUri: Uri,
        answersList: List<AnswerDto>,
        result: (String, HomeworkDto?) -> Unit
    ) {
        firebaseStorage.child("homework/${scheduleDto.id}").putFile(documentUri)
            .addOnSuccessListener {
                val formattedEmail = formatEmail(getUserEmail())
                firebaseDatabase.child(Constant.HOMEWORK).child(formattedEmail)
                    .child(scheduleDto.id)
                    .let { reference ->
                        val documentUrl =
                            "https://firebasestorage.googleapis.com/v0/b/classapp-c0f82.appspot.com/o/homework%2F${scheduleDto.id}?alt=media"
                        reference.child(Constant.DOCUMENT).setValue(documentUrl)
                        reference.child(Constant.ANSWERS).setValue(answersList)
                            .addOnSuccessListener {
                                val homeworkDto = HomeworkDto(
                                    userEmail = formattedEmail,
                                    id = scheduleDto.id,
                                    answersList = answersList,
                                    document = documentUrl
                                )
                                result("Результат сохранен", homeworkDto)
                            }.addOnFailureListener {
                                result("Произошла ошибка", null)
                            }
                    }
            }.addOnFailureListener {
                result("Произошла ошибка", null)
            }
    }

    fun updateStudentProfile(studentDto: StudentDto, result: (String) -> Unit) {
        val formattedEmail = formatEmail(studentDto.email)
        firebaseDatabase.child(Constant.USERS).child(formattedEmail).let { reference ->
            reference.child(Constant.NAME).setValue(studentDto.name)
            reference.child(Constant.PHONE).setValue(studentDto.phone)
            if (studentDto.image != null) {
                reference.child(Constant.BIRTH_DATE).setValue(studentDto.birthDate)
                firebaseStorage.child("users/$formattedEmail").putFile(Uri.parse(studentDto.image))
                    .addOnSuccessListener { storageReference ->
                        val imageUrl =
                            "https://firebasestorage.googleapis.com/v0/b/classapp-c0f82.appspot.com/o/users%2F$formattedEmail?alt=media"
                        reference.child(Constant.IMAGE).setValue(imageUrl).addOnSuccessListener {
                            result("Данные обновлены")
                        }.addOnFailureListener {
                            result("Произошла ошибка")
                        }
                    }.addOnCanceledListener {
                        result("Произошла ошибка")
                    }
            } else {
                reference.child(Constant.BIRTH_DATE).setValue(studentDto.birthDate)
                    .addOnSuccessListener {
                        result("Данные обновлены")
                    }.addOnFailureListener {
                        result("Произошла ошибка")
                    }
            }
        }
    }

    fun createLesson(lessonDto: LessonDto, result: (String, String?) -> Unit) {
        val formattedEmail = formatEmail(getUserEmail())
        firebaseDatabase.child(Constant.LESSONS).child(formattedEmail).let { reference ->
            val lessonId = reference.push().key.toString().trim()
            firebaseStorage.child("lessons/${lessonId}").putFile(Uri.parse(lessonDto.document))
                .addOnSuccessListener {
                    val documentUrl =
                        "https://firebasestorage.googleapis.com/v0/b/classapp-c0f82.appspot.com/o/lessons%2F$lessonId?alt=media"
                    reference.child(lessonId).child(Constant.THEME).setValue(lessonDto.theme)
                    reference.child(lessonId).child(Constant.DOCUMENT).setValue(documentUrl)
                    reference.child(lessonId).child(Constant.HOMEWORK).setValue(lessonDto.homework)
                        .addOnSuccessListener {
                            result("Урок создан", lessonId)
                        }.addOnFailureListener {
                            result("Произошла ошибка", null)
                        }
                }.addOnFailureListener {
                    result("Произошла ошибка", null)
                }
        }
    }

    fun addSchedule(scheduleDto: ScheduleDto, result: (String, String?) -> Unit) {
        val formattedEmail = formatEmail(getUserEmail())
        if (scheduleDto.id.isNotEmpty()) {
            firebaseDatabase.child(Constant.SCHEDULE).child(scheduleDto.creatorEmail)
                .let { reference ->
                    reference.child(scheduleDto.id).child(Constant.LESSON)
                        .setValue(scheduleDto.lesson)
                    reference.child(scheduleDto.id).child(Constant.THEME)
                        .setValue(scheduleDto.theme)
                    reference.child(scheduleDto.id).child(Constant.STUDENTS)
                        .setValue(scheduleDto.students)
                    reference.child(scheduleDto.id).child(Constant.SUBJECT)
                        .setValue(scheduleDto.subject)
                    reference.child(scheduleDto.id).child(Constant.TIME_END)
                        .setValue(scheduleDto.timeEnd)
                    reference.child(scheduleDto.id).child(Constant.TIME_START)
                        .setValue(scheduleDto.timeStart)
                        .addOnSuccessListener {
                            result("Занятие добавлено", scheduleDto.id)
                        }.addOnFailureListener {
                            result("Произошла ошибка", null)
                        }
                }
        } else {
            firebaseDatabase.child(Constant.SCHEDULE).child(formattedEmail).let { reference ->
                val lessonId = reference.push().key.toString().trim()
                reference.child(lessonId).child(Constant.LESSON).setValue(scheduleDto.lesson)
                reference.child(lessonId).child(Constant.THEME).setValue(scheduleDto.theme)
                reference.child(lessonId).child(Constant.STUDENTS).setValue(scheduleDto.students)
                reference.child(lessonId).child(Constant.SUBJECT).setValue(scheduleDto.subject)
                reference.child(lessonId).child(Constant.TIME_END).setValue(scheduleDto.timeEnd)
                reference.child(lessonId).child(Constant.TIME_START).setValue(scheduleDto.timeStart)
                    .addOnSuccessListener {
                        result("Занятие обновлено", lessonId)
                    }.addOnFailureListener {
                        result("Произошла ошибка", null)
                    }
            }
        }
    }

    fun createStudent(
        teacherViewModel: TeacherViewModel,
        name: String,
        email: String,
        password: String,
        result: (NetworkResult<String?>) -> Unit
    ) {
        val formattedEmail = formatEmail(getUserEmail())
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            val formattedEmailStudent = formatEmail(email)
            firebaseDatabase.child(Constant.USERS).child(formattedEmailStudent).let { reference ->
                reference.child(Constant.ROLE).setValue(Constant.STUDENT)
                reference.child(Constant.NAME).setValue(name).addOnSuccessListener {
                    firebaseDatabase.child(Constant.USERS)
                        .child(formattedEmail).child(Constant.STUDENTS)
                        .let { referenceTeacher ->
                            referenceTeacher.get().addOnSuccessListener {
                                if (it.exists()) {
                                    var students = it.value.toString().trim()
                                    if (students.isNotEmpty()) {
                                        students += "|$formattedEmailStudent"
                                    } else {
                                        students = formattedEmailStudent
                                    }
                                    referenceTeacher.setValue(students).addOnSuccessListener {
                                        teacherViewModel.setStudentsList(
                                            mutableListOf<StudentDto>().apply {
                                                addAll(
                                                    teacherViewModel.studentsList.value
                                                        ?: emptyList()
                                                )
                                                add(
                                                    StudentDto(
                                                        email = formattedEmailStudent,
                                                        name = name,
                                                        phone = null,
                                                        birthDate = null,
                                                        image = null,
                                                        rating = 0
                                                    )
                                                )
                                            }
                                        )
                                        teacherViewModel.setTeacher(
                                            teacherViewModel.teacher.value?.copy(
                                                studentIds = formatStringToList(
                                                    students
                                                )
                                            )
                                        )
                                        result(NetworkResult.Success(null))
                                    }.addOnFailureListener {
                                        result(NetworkResult.Failure("Не удалось создать профиль ученика"))
                                    }
                                } else {
                                    referenceTeacher.setValue(formattedEmailStudent)
                                        .addOnSuccessListener {
                                            teacherViewModel.setTeacher(
                                                teacherViewModel.teacher.value?.copy(
                                                    studentIds = listOf(
                                                        formattedEmailStudent
                                                    )
                                                )
                                            )
                                            teacherViewModel.setStudentsList(
                                                mutableListOf<StudentDto>().apply {
                                                    addAll(
                                                        teacherViewModel.studentsList.value
                                                            ?: emptyList()
                                                    )
                                                    add(
                                                        StudentDto(
                                                            email = formattedEmailStudent,
                                                            name = name,
                                                            phone = null,
                                                            birthDate = null,
                                                            image = null,
                                                            rating = 0
                                                        )
                                                    )
                                                }
                                            )
                                            result(NetworkResult.Success(null))
                                        }.addOnFailureListener {
                                            result(NetworkResult.Failure("Не удалось создать профиль ученика"))
                                        }
                                }
                            }.addOnFailureListener {
                                result(NetworkResult.Failure("Не удалось создать профиль ученика"))
                            }
                        }
                }.addOnFailureListener {
                    result(NetworkResult.Failure("Не удалось создать профиль ученика"))
                }
            }
        }.addOnFailureListener {
            result(NetworkResult.Failure("Не удалось создать профиль ученика"))
        }
    }

    fun removeStudentFromTeacher(
        teacherViewModel: TeacherViewModel, email: String, result: (String, Boolean) -> Unit
    ) {
        val formattedEmail = formatEmail(getUserEmail())
        firebaseDatabase.child(Constant.USERS).child(formattedEmail).child(Constant.STUDENTS)
            .let { reference ->
                reference.get().addOnSuccessListener {
                    if (it.exists()) {
                        var students = it.value.toString().trim()
                        students =
                            if (students.contains("|$email")) students.replace("|$email", "")
                            else if (students.contains("$email|")) students.replace(
                                "$email|",
                                ""
                            )
                            else students.replace("$email", "")
                        reference.setValue(students).addOnSuccessListener {
                            teacherViewModel.setTeacher(
                                teacherViewModel.teacher.value?.copy(
                                    studentIds = formatStringToList(
                                        students
                                    )
                                )
                            )
                            firebaseDatabase.child(Constant.USERS).child(email).removeValue()
                                .addOnSuccessListener {
                                    result("Ученик удален из списка", true)
                                }.addOnFailureListener {
                                    result("Произошла ошибка", false)
                                }
                        }.addOnFailureListener {
                            result("Произошла ошибка", false)
                        }
                    } else {
                        teacherViewModel.setTeacher(
                            teacherViewModel.teacher.value?.copy(studentIds = listOf())
                        )
                        result("Список учеников пуст", false)
                    }
                }.addOnFailureListener {
                    result("Произошла ошибка", false)
                }
            }
    }

    fun removeLesson(lessonId: String, result: (String, Boolean) -> Unit) {
        val formattedEmail = formatEmail(getUserEmail())
        firebaseDatabase.child(Constant.LESSONS).child(formattedEmail).child(lessonId).removeValue()
            .addOnSuccessListener {
                result("Урок удален", false)
            }.addOnFailureListener {
                result("Произошла ошибка", false)
            }
    }

    fun removeSchedule(item: ScheduleDto, result: (String, Boolean) -> Unit) {
        val formattedEmail = formatEmail(getUserEmail())
        firebaseDatabase.child(Constant.SCHEDULE).child(formattedEmail).child(item.id).removeValue()
            .addOnSuccessListener {
                result("Занятие удалено", true)
            }.addOnFailureListener {
                result("Произошла ошибка", false)
            }
    }

    private fun formatEmail(email: String): String {
        return email.replace("@", "-").replace(".", "_")
    }

    private fun formatStringToList(studentsValue: String): List<String> {
        return studentsValue.split("|")
    }
}