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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import nl.rijksoverheid.dbco.*
import nl.rijksoverheid.dbco.Constants.USER_CHOSE_ADD_CONTACTS_MANUALLY_AFTER_PAIRING_KEY
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.picker.ContactPickerPermissionFragmentDirections
import nl.rijksoverheid.dbco.databinding.FragmentMyContactsBinding
import nl.rijksoverheid.dbco.items.ui.*
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingViewModel
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TaskType
import nl.rijksoverheid.dbco.util.resolve
import timber.log.Timber
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingStatePoller.ReversePairingStatus
import nl.rijksoverheid.dbco.onboarding.PairingViewModel.PairingResult
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingCredentials

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

    private val tasksViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            TasksOverviewViewModel::class.java
        )
    }

    private val reversePairingViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            ReversePairingViewModel::class.java
        )
    }
    private val pairingViewModel by lazy {
        ViewModelProvider(requireActivity(), requireActivity().defaultViewModelProviderFactory).get(
            PairingViewModel::class.java
        )
    }

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

        // Load data from backend
        tasksViewModel.syncTasks()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyContactsBinding.bind(view)
        binding.content.adapter = adapter

        binding.toolbar.isVisible = false

        binding.manualEntryButton.setOnClickListener {
            checkPermissionGoToTaskDetails()
        }

        setupSendButton()

        binding.swipeRefresh.setOnRefreshListener {
            // Don't have to refresh if the user isn't paired yet, only local data
            if (isUserPaired()) {
                tasksViewModel.syncTasks()
            } else {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        tasksViewModel.fetchCase.observe(viewLifecycleOwner, { resource ->
            resource.resolve(onError = {
                binding.swipeRefresh.isRefreshing = false
                // If we get a JSON Serialization error the case window has expired
                if (it is SerializationException) {
                    binding.windowClosedView.visibility = View.VISIBLE
                    binding.sendButton.visibility = View.VISIBLE
                    binding.sendButton.setText(R.string.mycontacts_delete_data_button)
                    // turn off click listener if window is expired
                    adapter.setOnItemClickListener { _, _ -> {} }
                    binding.content.isEnabled = false
                } else {
                    // Generic error
                    showErrorDialog(getString(R.string.error_while_fetching_case), {
                        tasksViewModel.syncTasks()
                    }, it)
                }
                // Show cached data when error occurs
                val cachedCase = tasksViewModel.getCachedCase()
                fillContentSection(cachedCase)
            }, onSuccess = { case ->
                binding.swipeRefresh.isRefreshing = false
                fillContentSection(case)


                if (isUserPaired()) {
                    binding.sendButton.isEnabled = tasksViewModel.ifCaseWasChanged()
                    if (!tasksViewModel.ifCaseWasChanged()) {
                        binding.sendButtonHolder.visibility = View.GONE
                    }
                }
            })

        })

        adapter.setOnItemClickListener { item, view ->
            if (item is TaskItem) {
                checkPermissionGoToTaskDetails(item.task)
            }
            if (item is BuildNumberItem) {
                if (BuildConfig.DEBUG) {
                    handleQADataWipe()
                }
            }
            if (item is MemoryTipMyContactsItem) {
                findNavController().navigate(MyContactsFragmentDirections.toMyContactsMemoryTipFragment())
            }
        }

        if (!isUserPaired()) {
            setUpPairingListeners()
        }
    }

    private fun setupSendButton(
        pairingCredentials: ReversePairingCredentials? = null,
        initReversePairingWithInvalidState: Boolean = false
    ) {
        binding.sendButton.setOnClickListener {
            binding.pairingContainer.isVisible = false
            if (isUserPaired()) {
                if (!tasksViewModel.windowExpired.value!!) {
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
        reversePairingViewModel.pairingStatus.observe(viewLifecycleOwner, { status ->
            when (status) {
                is ReversePairingStatus.Stopped -> {
                    toggleButtonStyle(isPairing = false)
                    binding.sendButton.text = getString(R.string.send_data)
                    setupSendButton()
                }
                is ReversePairingStatus.Success -> {
                    pairingViewModel.pair(status.code)
                    setupSendButton()
                }
                is ReversePairingStatus.Error -> {
                    showPairingError(
                        errorText = getString(R.string.selfbco_reverse_pairing_my_contacts_error_message),
                        buttonText = getString(R.string.selfbco_reverse_pairing_error_button_text)
                    )
                    setupSendButton(pairingCredentials = status.credentials)
                }
                ReversePairingStatus.Expired -> {
                    showPairingError(
                        errorText = getString(R.string.selfbco_reverse_pairing_expired_code_message),
                        buttonText = getString(R.string.selfbco_reverse_pairing_expired_code_button_text)
                    )
                    setupSendButton(initReversePairingWithInvalidState = true)
                }
                is ReversePairingStatus.Pairing -> {
                    showPairingInProgress()
                    setupSendButton(pairingCredentials = status.credentials)
                }
            }
        })

        pairingViewModel.pairingResult.observe(viewLifecycleOwner, { result ->
            when (result) {
                is PairingResult.Success -> {
                    binding.pairingContainer.isVisible = false
                    toggleButtonStyle(isPairing = false)
                    binding.sendButton.text = getString(R.string.send_data)
                    binding.sendButton.isVisible = true
                    tasksViewModel.syncTasks()
                }
                is PairingResult.Error, PairingResult.Invalid -> {
                    showPairingError(
                        errorText = getString(R.string.selfbco_reverse_pairing_expired_code_message),
                        buttonText = getString(R.string.selfbco_reverse_pairing_expired_code_button_text)
                    )
                    setupSendButton(initReversePairingWithInvalidState = true)
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
        val uninformedSection = Section().apply {
            setHeader(
                DuoHeaderItem(
                    getString(R.string.mycontacts_uninformed_header),
                    getString(R.string.mycontacts_uninformed_subtext)
                )
            )
        }
        val informedSection = Section()
            .apply {
                setHeader(
                    DuoHeaderItem(
                        getString(R.string.mycontacts_informed_header),
                        getString(R.string.mycontacts_informed_subtext)
                    )
                )
            }


        case.tasks.forEach { task ->
            Timber.d("Found task $task")
            when (task.taskType) {
                TaskType.Contact -> {
                    val informed = when (task.communication) {
                        CommunicationType.Index -> task.didInform
                        CommunicationType.Staff -> task.linkedContact?.hasValidEmailOrPhone() == true
                        else -> false
                    }
                    if (informed) {
                        informedSection.add(TaskItem(task))
                    } else {
                        uninformedSection.add(TaskItem(task))
                    }
                }
            }
        }

        if (uninformedSection.groupCount > 1) {
            contentSection.add(uninformedSection)
        }

        if (informedSection.groupCount > 1) {
            contentSection.add(informedSection)
        }

        // Re-add footer section after clearing content
        contentSection.setFooter(footerSection)
    }

    override fun onResume() {
        super.onResume()
        clicksBlocked = false
        tasksViewModel.syncTasks()
        binding.swipeRefresh.isRefreshing = true
    }

    private fun checkPermissionGoToTaskDetails(task: Task? = null) {
        if (clicksBlocked) { // prevents from double click
            return
        }
        clicksBlocked = true
        if (tasksViewModel.getCachedQuestionnaire() == null) {
            showErrorDialog(getString(R.string.error_questionarre_is_empty), {
                // TODO: what?
            })
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
                    ContactPickerPermissionFragmentDirections.toContactDetails(indexTask = task)
                )
            } else {
                // If not granted permission - send users to permission grant screen (if he didn't see it before)
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactPickerPermission(task)
                )
            }
        } else {
            if (task?.linkedContact != null) {
                findNavController().navigate(
                    MyContactsFragmentDirections.toContactDetails(task, task.linkedContact)
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