package com.example.redphantomapplication.ui.fragments

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.redphantomapplication.R
import com.example.redphantomapplication.constant.Constants.REMEMBER_ME
import com.example.redphantomapplication.constant.Constants.SHARED_PREFERENCES_NAME
import com.example.redphantomapplication.constant.Constants.captchaList
import com.example.redphantomapplication.constant.Constants.captchaMap
import com.example.redphantomapplication.databinding.FragmentSetupLoginBinding
import com.example.redphantomapplication.ui.viewmodels.MainViewModel
import com.example.redphantomapplication.utils.Resource
import com.google.android.material.snackbar.Snackbar
import java.util.regex.Pattern
import kotlin.math.floor

class SetupLoginFragment : Fragment() {

    // TAG
    private val TAG = "SetupLoginFragment"

    // view binding
    private var _binding: FragmentSetupLoginBinding? = null
    private val binding get() = _binding!!

    // view model
    private val viewModel: MainViewModel by activityViewModels()

    // alert progress dialog
    private lateinit var dialog: Dialog

    // layout counter
    private var layoutCounter = 1
    private var checkValidSetPassword = false
    private var selectedCaptchaString: String? = null

    // shared preference
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupLoginBinding.inflate(layoutInflater, container, false)

        // init Loading Dialog
        dialog = Dialog(requireContext())
        dialog.apply {
            setContentView(R.layout.custom_dialog)
            setCancelable(false)
            if (dialog.window != null) {
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
        }

        // init shared pref
        sharedPreferences =
            requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        if (sharedPreferences.contains(REMEMBER_ME)) {
            val email = sharedPreferences.getString(REMEMBER_ME, "")
            if (!email.isNullOrBlank()) {
                val bundle = bundleOf(
                    "userEmail" to email
                )
                findNavController().navigate(
                    R.id.action_setupLoginFragment_to_dashboardFragment,
                    bundle
                )
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            // handle on back clicked
            onBack()

            // on click listeners
            onClickListeners()

            // view model observers
            observers()
        }
    }

    // view model observers
    private fun observers() {
        binding.apply {
            viewModel.apply {

                // sign in observers
                getSignInStatus.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> {
                            response.data?.let { email ->
                                if (!email.isNullOrBlank()) {
                                    if (materialSwitch.isChecked) {
                                        sharedPreferences.edit()
                                            .putString(REMEMBER_ME, loginEmail.toString()).apply()
                                    } else {
                                        sharedPreferences.edit().putString(REMEMBER_ME, "").apply()
                                    }
                                    dialog.dismiss()
                                    val bundle = bundleOf(
                                        "userEmail" to loginEmail.toString()
                                    )
                                    findNavController().navigate(
                                        R.id.action_setupLoginFragment_to_dashboardFragment,
                                        bundle
                                    )
                                    getSignInStatus.postValue(null)
                                }
                            }
                        }

                        is Resource.Error -> {
                            response.message?.let { message ->
                                Log.d(TAG, "Error occurred : $message")
                                dialog.dismiss()
                                if (message == "The password is invalid or the user does not have a password.") {
                                    Snackbar.make(root, "Wrong password!", Snackbar.LENGTH_SHORT)
                                        .show()
                                } else {
                                    Snackbar.make(
                                        root,
                                        "Wrong email or user doesn't exist!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        is Resource.Loading -> {
                            Log.d(TAG, "Loading!")
                            dialog.show()
                        }
                    }
                }

                // sign up observers
                getSignUpStatus.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Resource.Success -> {
                            response.data?.let { email ->
                                if (!email.isNullOrBlank()) {
                                    swipeUpAnimation(passwordLayout)
                                    sendEmailVerificationLink()
                                    getSignUpStatus.postValue(null)
                                }
                                dialog.dismiss()
                            }
                        }

                        is Resource.Error -> {
                            response.message?.let { message ->
                                Log.d(TAG, "Error occurred : $message")
                                dialog.dismiss()
                                if (message == "The email address is already in use by another account.") {
                                    Snackbar.make(
                                        root,
                                        "Email already registered!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Snackbar.make(
                                        root,
                                        "Error occurred please try again!",
                                        Snackbar.LENGTH_SHORT
                                    ).show()
                                }
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

    // on click listeners
    private fun onClickListeners() {
        binding.apply {

            // init captcha img
            initCaptcha()

            // login clicked
            loginBtn.setOnClickListener {
                if (isLoginFieldsValid()) {
                    viewModel.signInWithEmailPass(
                        loginEmail.text.toString(),
                        loginPassword.text.toString()
                    )
                }
            }

            // register clicked
            registrationBtn.setOnClickListener {
                swipeUpAnimation(loginLayout)
            }

            // forgot password
            forgotPassword.setOnClickListener {
                Snackbar.make(root, "Forgot Password!", Snackbar.LENGTH_SHORT).show()
            }

            // on sign up  next clicked
            signUpNextBtn.setOnClickListener {
                if (isRegisterFieldsValid()) {
                    swipeUpAnimation(signUpLayout)
                }
            }

            // go to sign in
            goToSignIn.setOnClickListener {

                // reduce layout counter
                layoutCounter--

                // going in progress
                loginLayout.visibility = View.VISIBLE
                val animationLeftSwipe =
                    AnimationUtils.loadAnimation(requireContext(), R.anim.from_left_to_right)
                loginLayout.startAnimation(animationLeftSwipe)
                animationLeftSwipe.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p0: Animation?) {
                        val anim = AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.from_right_to_left_2
                        )
                        signUpLayout.startAnimation(anim)
                    }

                    override fun onAnimationEnd(p0: Animation?) {
                        signUpLayout.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(p0: Animation?) {
                    }
                })
            }

            // open your email
            openEmailBtn.setOnClickListener {
                try {
                    val intent =
                        requireContext().packageManager.getLaunchIntentForPackage("com.google.android.gm")
                    if (intent != null) {
                        requireContext().startActivity(intent)
                    } else {
                        Log.d("RAG", "intent is null")
                        Snackbar.make(
                            root, "Error in opening mail!", Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (exception: ActivityNotFoundException) {
                    Log.d("RAG", exception.message.toString())
                    Snackbar.make(
                        root, "Error in opening mail!", Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            // go to phone verify
            goToPhoneVerify.setOnClickListener {
                swipeUpAnimation(emailVerifyLayout)
            }

            // resend OTP
            resendMobileOtp.setOnClickListener {
                Snackbar.make(root, "OTP sent!", Snackbar.LENGTH_SHORT).show()
            }

            // resend OTP
            nextToPassword.setOnClickListener {
                val code = otpView.text.toString().trim()
                if (TextUtils.isEmpty(code)) {
                    // Snack bar on empty OTP
                    Snackbar.make(
                        binding.root,
                        "Please enter verification code!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    if (!viewModel.getEmail().isNullOrBlank()) {
                        val bundle = bundleOf(
                            "userEmail" to viewModel.getEmail()
                        )
                        findNavController().navigate(
                            R.id.action_setupLoginFragment_to_setupCryptoFragment,
                            bundle
                        )
                    } else {
                        Snackbar.make(root, "Error Occurred!", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }

            // submit password
            submitPassword.setOnClickListener {
                if (isSetPasswordValid()) {
                    viewModel.signUpWithEmailPass(
                        signUpEmail.text.toString(),
                        setupRepassword.text.toString(),
                        signUpName.text.toString(),
                        signUpMobile.text.toString()
                    )
                }
            }

            // check for valid password
            setupPassword.addTextChangedListener { text ->
                if (!text.isNullOrBlank()) {
                    if (text.length in 8..20) {
                        passwordCheck1.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.verified_icon
                            )
                        )

                        if (isMatched(text.toString())) {
                            passwordCheck2.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.verified_icon
                                )
                            )
                            passwordCheck3.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.verified_icon
                                )
                            )
                            checkValidSetPassword = true
                        } else {
                            passwordCheck2.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.unverified_icon
                                )
                            )
                            passwordCheck3.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.unverified_icon
                                )
                            )
                            checkValidSetPassword = false
                        }
                    } else {
                        passwordCheck1.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.unverified_icon
                            )
                        )
                        checkValidSetPassword = false
                    }
                }
            }
        }
    }

    // is all present
    private fun isMatched(text: String): Boolean {
        val pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
        return pattern.matcher(text).matches()
    }

    // init captcha
    private fun initCaptcha() {
        binding.apply {
            val randomIndex = floor(Math.random() * captchaList.size)

            val captchaKey = captchaList[randomIndex.toInt()]

            captchaImage.setImageDrawable(resources.getDrawable(captchaKey.toInt()))

            selectedCaptchaString = captchaMap[captchaKey]
        }
    }

    // check for empty value on set password
    private fun isSetPasswordValid(): Boolean {
        binding.apply {

            val snackBarMessage: String = if (setupPassword.text.isNullOrBlank()) {
                "Enter password!"
            } else if (setupPassword.text.toString() != setupRepassword.text.toString()) {
                "Password doesn't match!"
            } else {
                return true
            }

            Snackbar.make(root, snackBarMessage, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    // check for empty value on login
    private fun isLoginFieldsValid(): Boolean {
        binding.apply {

            val snackBarMessage: String = if (loginEmail.text.isNullOrBlank()) {
                "Enter your email!"
            } else if (loginPassword.text.isNullOrBlank()) {
                "Enter your password!"
            } else if (loginPassword.text.toString().length < 6) {
                "Password length can't smaller than 6!"
            } else if (loginCaptcha.text.isNullOrBlank()) {
                "Please enter captcha"
            } else if (loginCaptcha.text.toString() != selectedCaptchaString) {
                "Wrong captcha"
            } else {
                return true
            }

            Snackbar.make(root, snackBarMessage, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    // check for empty value on sign up
    private fun isRegisterFieldsValid(): Boolean {
        binding.apply {

            val snackBarMessage: String = if (signUpName.text.isNullOrBlank()) {
                "Enter your name!"
            } else if (signUpEmail.text.isNullOrBlank()) {
                "Enter your email!"
            } else if (signUpMobile.text.toString()
                    .isNullOrBlank() || signUpMobile.text.toString().length < 10
            ) {
                "Enter your valid mobile!"
            } else {
                return true
            }

            Snackbar.make(root, snackBarMessage, Snackbar.LENGTH_SHORT).show()
            return false
        }
    }

    // do animation from bottom to left
    private fun swipeUpAnimation(animView: View) {
        binding.apply {
            val animationLeftSwipe =
                AnimationUtils.loadAnimation(requireContext(), R.anim.from_right_to_left)
            animView.startAnimation(animationLeftSwipe)
            animationLeftSwipe.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    animView.visibility = View.GONE
                    layoutCounter++
                    when (layoutCounter) {
                        2 -> {
                            signUpLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            signUpLayout.startAnimation(animationFadeIn)
                        }

                        3 -> {
                            passwordLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            passwordLayout.startAnimation(animationFadeIn)
                        }

                        4 -> {
                            emailVerifyLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            emailVerifyLayout.startAnimation(animationFadeIn)
                        }

                        5 -> {
                            mobileVerifyLayout.visibility = View.VISIBLE
                            val animationFadeIn =
                                AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_box)
                            mobileVerifyLayout.startAnimation(animationFadeIn)
                        }
                    }
                }

                override fun onAnimationRepeat(p0: Animation?) {}
            })
        }
    }

    // handle onBack pressed
    private fun onBack() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}