package com.example.redphantomapplication.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.redphantomapplication.R
import com.example.redphantomapplication.constant.Constants
import com.example.redphantomapplication.data.UserDetails
import com.example.redphantomapplication.databinding.FragmentDashBoardBinding
import com.example.redphantomapplication.ui.viewmodels.MainViewModel
import com.example.redphantomapplication.utils.Resource
import com.google.android.material.snackbar.Snackbar

class DashboardFragment : Fragment() {

    // TAG
    private val TAG = "DashboardFragment"

    // view binding
    private var _binding: FragmentDashBoardBinding? = null
    private val binding get() = _binding!!

    // shared preference
    private lateinit var sharedPreferences: SharedPreferences

    // view model
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashBoardBinding.inflate(layoutInflater, container, false)

        // init shared pref
        sharedPreferences =
            requireActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE
            )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            // handle on back clicked
            onBack()

            // get data from cloud
            if (!viewModel.getEmail().isNullOrBlank()) {
                viewModel.getUserData(viewModel.getEmail().toString())
            } else {
                updateUI(UserDetails("Vimal", "vimal@email", "9898998989", "fgHJ567*(fgHJ6YHBNM"))
            }

            // view model observers
            observers()

            // on click listeners
            onClickListener()
        }
    }

    // observers
    private fun observers() {
        binding.apply {
            viewModel.apply {

                // get users data
                getUserData.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> {
                            response.data?.let { userDetails ->
                                if (!userDetails.email.isNullOrBlank()) {
                                    updateUI(userDetails)
                                    getUserData.postValue(null)
                                }
                            }
                        }

                        is Resource.Error -> {
                            response.message?.let { message ->
                                Log.d(TAG, "Error occurred : $message")
                            }
                        }

                        is Resource.Loading -> {
                            Log.d(TAG, "Loading!")
                        }
                    }
                }
            }
        }
    }

    // UI update
    private fun updateUI(userDetails: UserDetails) {
        binding.apply {

            // name
            name.text = userDetails.name

            // email
            email.text = userDetails.email

            // name
            phone.text = userDetails.mobile

            // name
            name.text = userDetails.email
        }
    }

    // on click listeners
    private fun onClickListener() {
        binding.apply {

            // on settings clicked
            settings.setOnClickListener {
                Snackbar.make(root, "Settings!", Snackbar.LENGTH_SHORT).show()
            }

            // on edit profile clicked
            editProfile.setOnClickListener {
                Snackbar.make(root, "Edit profile!", Snackbar.LENGTH_SHORT).show()
            }

            // on edit digital verification clicked
            editDigitalVerify.setOnClickListener {
                Snackbar.make(root, "Edit Digital Verification!", Snackbar.LENGTH_SHORT).show()
            }

            // on bank details clicked
            editBankDetails.setOnClickListener {
                Snackbar.make(root, "Edit Bank Details!", Snackbar.LENGTH_SHORT).show()
            }

            // menu button clicks
            menuCirProfile.setOnClickListener {
                Snackbar.make(root, "Profile!", Snackbar.LENGTH_SHORT).show()
            }

            menuProfile1.setOnClickListener {
                Snackbar.make(root, "Profile Settings!", Snackbar.LENGTH_SHORT).show()
            }

            menuWallet.setOnClickListener {
                Snackbar.make(root, "Wallet!", Snackbar.LENGTH_SHORT).show()
            }

            menuCirProfile.setOnClickListener {
                Snackbar.make(root, "Profile!", Snackbar.LENGTH_SHORT).show()
            }

            menuCalender.setOnClickListener {
                Snackbar.make(root, "Calender!", Snackbar.LENGTH_SHORT).show()
            }

            menuCash.setOnClickListener {
                Snackbar.make(root, "Cash Flow!", Snackbar.LENGTH_SHORT).show()
            }

            menuTask.setOnClickListener {
                Snackbar.make(root, "Tasks!", Snackbar.LENGTH_SHORT).show()
            }

            menuBadges.setOnClickListener {
                Snackbar.make(root, "Priority Task!", Snackbar.LENGTH_SHORT).show()
            }

            menuTaskPriority.setOnClickListener {
                Snackbar.make(root, "Task Completed!", Snackbar.LENGTH_SHORT).show()
            }

            menuLogout.setOnClickListener {
                sharedPreferences.edit().putString(Constants.REMEMBER_ME, "").apply()
                viewModel.logout()
                findNavController().navigate(R.id.action_dashboardFragment_to_setupLoginFragment)
            }

            menuFiles.setOnClickListener {
                Snackbar.make(root, "Cloud Storage!", Snackbar.LENGTH_SHORT).show()
            }

            menuEdit.setOnClickListener {
                Snackbar.make(root, "Edit!", Snackbar.LENGTH_SHORT).show()
            }

            menuPerson.setOnClickListener {
                Snackbar.make(root, "Profile!", Snackbar.LENGTH_SHORT).show()
            }

            menuSettings.setOnClickListener {
                Snackbar.make(root, "Settings!", Snackbar.LENGTH_SHORT).show()
            }

            menuLogoutMain.setOnClickListener {
                sharedPreferences.edit().putString(Constants.REMEMBER_ME, "").apply()
                viewModel.logout()
                findNavController().navigate(R.id.action_dashboardFragment_to_setupLoginFragment)
            }
        }
    }

    // handle onBack pressed
    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            sharedPreferences.edit().putString(Constants.REMEMBER_ME, "").apply()
            viewModel.logout()
            findNavController().navigate(R.id.action_dashboardFragment_to_setupLoginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}