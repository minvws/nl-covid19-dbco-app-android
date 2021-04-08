/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.mycontacts

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.*
import nl.rijksoverheid.dbco.Constants.USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.picker.ContactPickerPermissionFragmentDirections
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsBinding
import nl.rijksoverheid.dbco.items.input.TextButtonItem
import nl.rijksoverheid.dbco.items.ui.*
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TaskType
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.ReversePairingStatus.*
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingCredentials
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.CaseExpired
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.CaseSuccess
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.CaseResult.CaseError
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireError
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.ViewData

/**
 * Overview fragment showing selected or suggested contacts of the user
 */

@ExperimentalSerializationApi
class MyContactsFragment : BaseFragment(R.layout.fragment_my_contacts) {

    lateinit var binding: FragmentMyContactsBinding

    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val userPrefs by lazy {
        LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
    }

    private var dataWipeClickedAmount = 0

    private val tasksViewModel: TasksOverviewViewModel by activityViewModels()

    private val pairingViewModel: PairingViewModel by activityViewModels()

    private val contentSection = Section()
    private lateinit var footerSection: Section
    private lateinit var headerSection: Section
    private var clicksBlocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        headerSection = Section().apply {
            add(MemoryTipMyContactsItem(tasksViewModel.getStartOfContagiousPeriod()))
        }

        footerSection = Section().apply {
            add(
                FooterItem(
                    getString(R.string.mycontact_privacy_footer),
                    clickable = true
                )
            )
            add(
                BuildNumberItem(
                    getString(
                        R.string.status_app_version,
                        BuildConfig.VERSION_NAME,
                        "${BuildConfig.VERSION_CODE}-${BuildConfig.GIT_VERSION}"
                    ), clickable = true
                )
            )
        }

        // pre-set header and footer section to show content even if no tasks are available
        contentSection.setFooter(footerSection)
        contentSection.setHeader(headerSection)

        adapter.add(contentSection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyContactsBinding.bind(view)
        binding.content.adapter = adapter

        binding.toolbar.isVisible = false

        setupSendButton()

        binding.swipeRefresh.setOnRefreshListener {
            tasksViewModel.syncData()
            binding.swipeRefresh.isRefreshing = true
        }

        tasksViewModel.viewData.observe(viewLifecycleOwner, { data ->
            handleViewData(data)
        })

        adapter.setOnItemClickListener { item, _ ->
            if (item is TaskItem) {
                checkPermissionGoToTaskDetails(item.task)
            }
            if (item is BuildNumberItem) {
                if (BuildConfig.DEBUG) {
                    handleQADataWipe()
                }
            }
            if (item is MemoryTipMyContactsItem) {
                findNavController().navigate(
                    MyContactsFragmentDirections.toMyContactsMemoryTipFragment(
                        item.date.toString(DateFormats.selfBcoDateOnly)
                    )
                )
            }
        }

        if (!isUserPaired()) {
            setUpPairingListeners()
        }
    }

    private fun handleViewData(data: ViewData) {
        binding.swipeRefresh.isRefreshing = false
        val caseResult = data.caseResult
        val questionnaireResult = data.questionnaireResult
        var error = false
        when (caseResult) {
            is CaseExpired -> {
                binding.windowClosedView.visibility = View.VISIBLE
                binding.sendButton.visibility = View.VISIBLE
                binding.sendButton.setText(R.string.mycontacts_delete_data_button)
                fillContentSection(caseResult.cachedCase)
            }
            is CaseError -> {
                error = true
                fillContentSection(caseResult.cachedCase)
            }
            is CaseSuccess -> {
                fillContentSection(caseResult.case)
                if (isUserPaired()) {
                    binding.sendButtonHolder.isVisible = caseResult.case.canBeUploaded
                }
            }
        }
        if (error || questionnaireResult is QuestionnaireError) {
            showErrorDialog(getString(R.string.generic_error_prompt_message), {
                binding.swipeRefresh.isRefreshing = true
                tasksViewModel.syncData()
            })
        }
    }

    private fun setupSendButton(
        pairingCredentials: ReversePairingCredentials? = null,
        initReversePairingWithInvalidState: Boolean = false
    ) {
        binding.sendButton.setOnClickListener {
            binding.pairingContainer.isVisible = false
            if (isUserPaired()) {
                if (tasksViewModel.viewData.value?.caseResult !is CaseExpired) {
                    findNavController().navigate(MyContactsFragmentDirections.toFinalizeCheck())
                } else {
                    showLocalDeletionDialog()
                }
            } else {
                // User isn't paired yet, let them pair first
                findNavController()
                    .navigate(
                        MyContactsFragmentDirections.toReversePairingFragment(
                            credentials = pairingCredentials,
                            initWithInvalidCodeState = initReversePairingWithInvalidState
                        )
                    )
            }
        }
    }

    private fun setUpPairingListeners() {
        pairingViewModel.reversePairingStatus.observe(viewLifecycleOwner, { status ->
            when (status) {
                is ReversePairingSuccess -> {
                    binding.pairingContainer.isVisible = false
                    toggleButtonStyle(isPairing = false)
                    binding.sendButton.text = getString(R.string.send_data)
                    binding.sendButton.isVisible = true
                    tasksViewModel.syncData()
                    setupSendButton()
                }
                is ReversePairingStopped -> {
                    toggleButtonStyle(isPairing = false)
                    binding.sendButton.text = getString(R.string.send_data)
                    setupSendButton()
                }
                is ReversePairingError -> {
                    showPairingError(
                        errorText = getString(R.string.selfbco_reverse_pairing_my_contacts_error_message),
                        buttonText = getString(R.string.selfbco_reverse_pairing_error_button_text)
                    )
                    setupSendButton(pairingCredentials = status.credentials)
                }
                ReversePairingExpired -> {
                    showPairingError(
                        errorText = getString(R.string.selfbco_reverse_pairing_expired_code_message),
                        buttonText = getString(R.string.selfbco_reverse_pairing_expired_code_button_text)
                    )
                    setupSendButton(initReversePairingWithInvalidState = true)
                }
                is ReversePairing -> {
                    showPairingInProgress()
                    setupSendButton(pairingCredentials = status.credentials)
                }
            }
        })
    }

    private fun showPairingInProgress() {
        binding.pairingLoadingIndicator.isVisible = true
        with(binding.pairingStateText) {
            setTextColor(ContextCompat.getColor(context, R.color.purple))
            text = getString(R.string.selfbco_reverse_pairing_pairing_waiting)
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
        toggleButtonStyle(isPairing = true)
        binding.sendButton.text = getString(R.string.reverse_pairing_try_again)
        binding.pairingContainer.isVisible = true
    }

    private fun showPairingError(
        errorText: String,
        buttonText: String
    ) {
        binding.pairingLoadingIndicator.isVisible = false
        with(binding.pairingStateText) {
            text = errorText
            setTextColor(ContextCompat.getColor(context, R.color.red))
            setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_error),
                null,
                null,
                null
            )
        }
        toggleButtonStyle(isPairing = true)
        binding.sendButton.text = buttonText
        binding.pairingContainer.isVisible = true
    }

    private fun toggleButtonStyle(isPairing: Boolean) {
        if (isPairing) {
            binding.sendButton.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_secondary
            )
            binding.sendButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary
                )
            )
        } else {
            binding.sendButton.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                R.color.button_primary
            )
            binding.sendButton.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.secondary
                )
            )
        }
    }

    private fun fillContentSection(case: Case) {
        contentSection.clear()

        val sections = if (case.isUploaded) {
            createUploadedSections(case)
        } else {
            createNotUploadedSections(case)
        }

        val topSection = sections.first()
        val bottomSection = sections.last()

        if (topSection.groupCount == 1) {
            contentSection.add(TextButtonItem(getString(R.string.add_contact)) {
                checkPermissionGoToTaskDetails(Task.createAppContact())
            })
        } else {
            contentSection.add(topSection)
        }

        if (bottomSection.groupCount > 1) {
            contentSection.add(bottomSection)
        }

        contentSection.setFooter(footerSection)
    }

    private fun createUploadedSections(case: Case): List<Section> {
        val notUploadedSection = Section().apply {
            setHeader(SubHeaderItem(getString(R.string.mycontacts_not_uploaded_header)))
        }
        val uploadedSection = Section().apply {
            setHeader(SubHeaderItem(getString(R.string.mycontacts_uploaded_header)))
        }

        case.tasks.forEach { task ->
            when (task.taskType) {
                TaskType.Contact -> {
                    if (task.canBeUploaded) {
                        notUploadedSection.add(TaskItem(task))
                    } else {
                        uploadedSection.add(TaskItem(task))
                    }
                }
            }
        }
        if (notUploadedSection.groupCount > 1) {
            notUploadedSection.add(TextButtonItem(getString(R.string.add_contact)) {
                checkPermissionGoToTaskDetails(Task.createAppContact())
            })
        }
        return listOf(notUploadedSection, uploadedSection)
    }

    private fun createNotUploadedSections(case: Case): List<Section> {
        val inProgressSection = Section().apply {
            setHeader(SubHeaderItem(getString(R.string.mycontacts_in_progress_header)))
        }
        val doneSection = Section().apply {
            setHeader(SubHeaderItem(getString(R.string.mycontacts_done_header)))
        }

        case.tasks.forEach { task ->
            when (task.taskType) {
                TaskType.Contact -> {
                    val informed = when (task.communication) {
                        CommunicationType.Staff -> task.linkedContact?.hasValidEmailOrPhone() == true
                        else -> task.didInform
                    }
                    if (informed) {
                        doneSection.add(TaskItem(task))
                    } else {
                        inProgressSection.add(TaskItem(task))
                    }
                }
            }
        }
        if (inProgressSection.groupCount > 1) {
            inProgressSection.add(TextButtonItem(getString(R.string.add_contact)) {
                checkPermissionGoToTaskDetails(Task.createAppContact())
            })
        }
        return listOf(inProgressSection, doneSection)
    }

    override fun onResume() {
        super.onResume()
        clicksBlocked = false
        tasksViewModel.syncData()
        binding.swipeRefresh.isRefreshing = true
    }

    private fun checkPermissionGoToTaskDetails(task: Task) {
        if (clicksBlocked) { // prevents from double click
            return
        }
        clicksBlocked = true

        if (tasksViewModel.viewData.value?.caseResult is CaseExpired) {
            // no need to check permissions, just show the task but disabled
            findNavController().navigate(
                ContactPickerPermissionFragmentDirections.toContactDetails(
                    indexTask = task,
                    enabled = false
                )
            )
            return
        }

        if (tasksViewModel.getCachedQuestionnaire() == null) {
            showErrorDialog(getString(R.string.error_questionnaire_is_empty), { /* NO-OP */ })
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (userPrefs.getBoolean(
                    USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY,
                    false
                )
            ) {
                findNavController().navigate(
                    ContactPickerPermissionFragmentDirections.toContactDetails(
                        indexTask = task,
                        enabled = true
                    )
                )
            } else {
                // If not granted permission - send users to permission grant screen (if he didn't see it before)
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactPickerPermission(task)
                )
            }
        } else {
            if (task.linkedContact != null) {
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactDetails(
                        indexTask = task,
                        enabled = true
                    )
                )
            } else {
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactPickerSelection(task)
                )
            }
        }
    }

    private fun isUserPaired(): Boolean {
        return userPrefs.getBoolean(Constants.USER_IS_PAIRED, false)
    }

    private fun handleQADataWipe() {
        dataWipeClickedAmount++
        if (dataWipeClickedAmount == 4) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setMessage(getString(R.string.qa_clear_data_summary))
            builder.setPositiveButton(R.string.answer_yes) { dialog, _ ->
                dataWipeClickedAmount = 0

                // Clear locally stored data & remove tokens from UserRepository
                val encryptedSharedPreferences: SharedPreferences =
                    LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
                encryptedSharedPreferences.edit().clear().commit()
                dialog.dismiss()
                // Start activity to restart the onboarding flow before killing the original activity
                activity?.startActivity(Intent(activity, MainActivity::class.java))
                activity?.finish()
            }
            builder.setNegativeButton(R.string.answer_no) { dialog, _ ->
                dialog.dismiss()
                dataWipeClickedAmount = 0
            }
            builder.create().show()
        }
    }

    private fun showLocalDeletionDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.mycontacts_delete_data_title))
        builder.setMessage(getString(R.string.mycontacts_delete_data_summary))
        builder.setPositiveButton(R.string.mycontacts_delete_data_ok) { dialog, _ ->
            // Clear locally stored data & remove tokens from UserRepository
            val encryptedSharedPreferences: SharedPreferences =
                LocalStorageRepository.getInstance(requireContext()).getSharedPreferences()
            encryptedSharedPreferences.edit().clear().commit()
            dialog.dismiss()
            // Start activity to restart the onboarding flow before killing the original activity
            activity?.startActivity(Intent(activity, MainActivity::class.java))
            activity?.finish()
        }
        builder.setNegativeButton(R.string.mycontacts_delete_data_cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}