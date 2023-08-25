package com.example.redphantomapplication.ui.fragments

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.redphantomapplication.R
import com.example.redphantomapplication.databinding.FragmentSetupCryptoBinding
import com.example.redphantomapplication.ui.viewmodels.MainViewModel
import com.example.redphantomapplication.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SetupCryptoFragment : Fragment() {

    // TAG
    private val TAG = "SetupCryptoFragment"

    // view binding
    private var _binding: FragmentSetupCryptoBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: MainViewModel by activityViewModels()

    // alert progress dialog
    private lateinit var dialog: Dialog

    // layout counter
    private var layoutCounter = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupCryptoBinding.inflate(layoutInflater, container, false)

        // init Loading Dialog
        dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(R.layout.custom_dialog)
            setCancelable(false)
            if (dialog.window != null) {
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            // handle on back clicked
            onBack()

            // on click listener
            onClickListener()

            // observers
            observers()

            // set up hexadecimal value
            hexadecimalValue.text = getRandomString(42)
        }
    }

    // on click listeners
    private fun onClickListener() {
        binding.apply {

            // on next generate click
            nextGenerateKeysBtn.setOnClickListener {
                swipeUpAnimation(generateKeysLayout, it)
            }

            // on next generate hash key click
            nextGenerateHashBtn.setOnClickListener {
                swipeUpAnimation(generateHashLayout, it)
            }

            // on next t&c key click
            nextTCBtn.setOnClickListener {
                if (checkboxTc1.isChecked && checkboxTc2.isChecked && checkboxTc3.isChecked) {
                    swipeUpAnimation(tCLayout, it)
                } else {
                    Snackbar.make(
                        root,
                        "Please accept the terms and conditions.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            // on generate phrase clicked
            clickToGeneratePhrasesBtn.setOnClickListener {
                swipeUpAnimation(generateSecurityLayout, it)
            }

            // on generate submit clicked
            nextTCABtn.setOnClickListener {
                if (checkboxTca1.isChecked && checkboxTca2.isChecked) {
                    val email = arguments?.getString("userEmail")
                    if (email != null) {
                        viewModel.setUpHexadecimalPassword(email, hexadecimalValue.text.toString())
                    }
                } else {
                    Snackbar.make(
                        root,
                        "Please accept the terms and conditions.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            // copy hexadecimal value
            copyImg.setOnClickListener {
                val clipboardManager =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("Hexadecimal Value", hexadecimalValue.text)
                clipboardManager.setPrimaryClip(clipData)
                Snackbar.make(root, "Hexadecimal value copied to clipboard!", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // view model observers
    private fun observers() {
        binding.apply {
            viewModel.apply {

                // hexadecimal value
                getHexadecimalStatus.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> {
                            response.data?.let { email ->
                                if (!email.isNullOrBlank()) {
                                    dialog.dismiss()
                                    getHexadecimalStatus.postValue(null)
                                    showDialog()
                                }
                            }
                        }

                        is Resource.Error -> {
                            response.message?.let { message ->
                                Log.d(TAG, "Error occurred : $message")
                                dialog.dismiss()
                            }
                        }

                        is Resource.Loading -> {
                            Log.d(TAG, "Loading!")
                            dialog.show()
                        }
                    }
                }
            }
        }
    }

    // do animation from bottom to left
    private fun swipeUpAnimation(animView: View, disableView: View) {
        binding.apply {
            val animationLeftSwipe =
                AnimationUtils.loadAnimation(requireContext(), R.anim.from_right_to_left)
            animView.startAnimation(animationLeftSwipe)
            disableView.isClickable = false
            animationLeftSwipe.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    animView.visibility = View.GONE
                    layoutCounter++
                    when (layoutCounter) {
                        2 -> {
                            generateHashLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            generateHashLayout.startAnimation(animationFadeIn)
                        }

                        3 -> {
                            tCLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            tCLayout.startAnimation(animationFadeIn)
                        }

                        4 -> {
                            generateSecurityLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            generateSecurityLayout.startAnimation(animationFadeIn)
                        }

                        5 -> {
                            tCAccountLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            tCAccountLayout.startAnimation(animationFadeIn)
                        }
                    }
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
    }

    // hash value
    private fun getRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    // show created dialog
    private fun showDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage("Your cryptography account is created successfully.")
            setPositiveButton("OK") { _, _ ->
                val bundle = bundleOf(
                    "userEmail" to viewModel.getEmail()
                )
                findNavController().navigate(
                    R.id.action_setupCryptoFragment_to_dashboardFragment,
                    bundle
                )
            }
            setCancelable(false)
        }.show()
    }

    // handle onBack pressed
    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.action_setupCryptoFragment_to_setupLoginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}