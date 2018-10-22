package com.gpetuhov.android.hive.managers

import android.app.Activity
import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.model.User
import com.gpetuhov.android.hive.util.Constants
import com.gpetuhov.android.hive.util.isOnline
import timber.log.Timber

class AuthManager {

    companion object {
        private const val TAG = "AuthManager"
    }

    var user = createAnonymousUser()
    var isAuthorized = false

    private var firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var noNetworkDialog: MaterialDialog? = null

    fun init(onSignIn: (User) -> Unit, onSignOut: () -> Unit) {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser != null) {
                // User is signed in
                Timber.tag(TAG).d("Login successful")
                Timber.tag(TAG).d("User id = ${firebaseUser.uid}")
                Timber.tag(TAG).d("User name = ${firebaseUser.displayName}")
                Timber.tag(TAG).d("User email = ${firebaseUser.email}")

                user = convertFirebaseUser(firebaseUser)
                isAuthorized = true
                onSignIn(user)

            } else {
                // User is signed out
                Timber.tag(TAG).d("User signed out")

                isAuthorized = false
                user = createAnonymousUser()
                onSignOut()
            }
        }
    }

    fun showLoginScreen(activity: Activity, resultCode: Int) {
        if (isOnline(activity)) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
//                    AuthUI.IdpConfig.PhoneBuilder().build(),
//                    AuthUI.IdpConfig.FacebookBuilder().build(),
//                    AuthUI.IdpConfig.TwitterBuilder().build()
            )

            activity.startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(true)
                    .setAvailableProviders(providers)
                    .setTheme(R.style.AuthTheme)
                    .build(),
                resultCode
            )

        } else {
            showNoNetworkDialog(activity, resultCode)
        }
    }

    fun startListenAuth() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun stopListenAuth() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    fun signOut(context: Context?, onSuccess: () -> Unit, onError: () -> Unit) {
        if (context != null) {
            AuthUI.getInstance()
                .signOut(context)
                .addOnSuccessListener {
                    Timber.tag(TAG).d("Sign out successful")
                    onSuccess()
                }
                .addOnFailureListener {
                    Timber.tag(TAG).d("Sign out error")
                    onError()
                }
        }
    }

    fun deleteAccount(context: Context?, onSuccess: () -> Unit, onError: () -> Unit) {
        if (context != null) {
            AuthUI.getInstance()
                .delete(context)
                .addOnSuccessListener {
                    Timber.tag(TAG).d("User deleted successfully")
                    onSuccess()
                }
                .addOnFailureListener {
                    Timber.tag(TAG).d("Error deleting user")
                    onError()
                }
        }
    }

    fun dismissDialogs() {
        dismissNoNetworkDialog()
    }

    private fun createAnonymousUser(): User {
        return User(
            uid = "",
            name = Constants.Auth.DEFAULT_USER_NAME,
            email = Constants.Auth.DEFAULT_USER_MAIL,
            isOnline = false,
            location = LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE)
        )
    }

    private fun convertFirebaseUser(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: Constants.Auth.DEFAULT_USER_NAME,
            email = firebaseUser.email ?: Constants.Auth.DEFAULT_USER_MAIL,
            isOnline = false,
            location = LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE)
        )
    }

    private fun initNoNetworkDialog(activity: Activity, resultCode: Int) {
        noNetworkDialog = MaterialDialog(activity)
            .title(R.string.sign_in_error)
            .message(R.string.sign_in_no_network)
            .cancelable(false)
            .positiveButton { showLoginScreen(activity, resultCode) }
            .negativeButton { closeApp() }
    }

    private fun showNoNetworkDialog(activity: Activity, resultCode: Int) {
        dismissNoNetworkDialog()
        initNoNetworkDialog(activity, resultCode)
        noNetworkDialog?.show()
    }

    private fun dismissNoNetworkDialog() {
        noNetworkDialog?.dismiss()
    }

    private fun closeApp() {
        System.exit(0)
    }
}