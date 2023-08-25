package com.example.redphantomapplication.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.redphantomapplication.data.UserDetails
import com.example.redphantomapplication.utils.Resource
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {

    private val TAG = "MainViewModel"

    private var auth = Firebase.auth
    private val db = Firebase.firestore

    // sign in live data
    var getSignInStatus: MutableLiveData<Resource<String>> = MutableLiveData()

    // sign up live data
    var getSignUpStatus: MutableLiveData<Resource<String>> = MutableLiveData()

    // setup hexadecimal setup live data
    var getHexadecimalStatus: MutableLiveData<Resource<String>> = MutableLiveData()

    // get user data
    var getUserData: MutableLiveData<Resource<UserDetails>> = MutableLiveData()

    // sign in with email & pass
    fun signInWithEmailPass(email: String, password: String) {
        getSignInStatus.postValue(Resource.Loading())

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser
                getSignInStatus.postValue(Resource.Success(user?.email.toString()))
            }.addOnFailureListener {
                getSignInStatus.postValue(Resource.Error(it.message.toString()))
            }
    }

    // sign up with email & pass
    fun signUpWithEmailPass(email: String, password: String, name: String, mobile: String) {
        getSignUpStatus.postValue(Resource.Loading())

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userEmail = auth.currentUser?.email
                if (!userEmail.isNullOrBlank()) {
                    addUserDetails(userEmail, name, mobile)
                } else {
                    getSignUpStatus.postValue(Resource.Error("Null user"))
                }
            }.addOnFailureListener {
                getSignUpStatus.postValue(Resource.Error(it.message.toString()))
            }
    }

    // add user details to cloud
    private fun addUserDetails(email: String, name: String, mobile: String) {
        val users = UserDetails(name, email, mobile, "")

        db.collection("users").document(email)
            .set(users)
            .addOnSuccessListener {
                getSignUpStatus.postValue(Resource.Success(email))
            }
            .addOnFailureListener {
                getSignUpStatus.postValue(Resource.Error(it.message.toString()))
            }
    }

    // send email verification link
    fun sendEmailVerificationLink() {
        if (auth.currentUser != null) {
            if (!auth!!.currentUser!!.isEmailVerified) {
                auth.currentUser!!.sendEmailVerification()
                    .addOnSuccessListener {
                        Log.d(TAG, "Email sent successful!")
                    }.addOnFailureListener {
                        Log.d(TAG, "Email verify error : ${it.message}")
                    }
            }
        }
    }

    // sign up with email & pass
    fun setUpHexadecimalPassword(email: String, hexadecimalKey: String) {
        getHexadecimalStatus.postValue(Resource.Loading())

        db.collection("users").document(email)
            .update("hexadecimalKey", hexadecimalKey)
            .addOnSuccessListener {
                getHexadecimalStatus.postValue(Resource.Success(email))
            }
            .addOnFailureListener {
                getHexadecimalStatus.postValue(Resource.Error(it.message.toString()))
            }
    }

    // return email
    fun getEmail() = auth.currentUser?.email

    // logout
    fun logout() {
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }

    // sign in with email & pass
    fun getUserData(email: String) {
        getUserData.postValue(Resource.Loading())

        if (!email.isNullOrBlank()) {
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        val userDetails = document.toObject(UserDetails::class.java)!!
                        getUserData.postValue(Resource.Success(userDetails))
                    } else {
                        val error = "No such document"
                        Log.d(TAG, error)
                        getUserData.postValue(Resource.Error(error))
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error in fetching user data : ", exception)
                    getUserData.postValue(Resource.Error(exception.message.toString()))
                }
        } else {
            getUserData.postValue(Resource.Error("Email is null"))
        }
    }
}