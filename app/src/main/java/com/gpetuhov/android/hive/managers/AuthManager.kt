package com.gpetuhov.android.hive.managers

import android.app.Activity
import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.auth.Auth
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.domain.network.Network
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.util.Constants
import timber.log.Timber
import javax.inject.Inject

// Manages authentication with Firebase Auth
// Class is open so that it can be mocked in tests by Mockito
open class AuthManager : Auth {

    companion object {
        private const val TAG = "AuthManager"
    }

    @Inject lateinit var context: Context
    @Inject lateinit var repo: Repo
    @Inject lateinit var network: Network

    private var firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var noNetworkDialog: MaterialDialog? = null

    init {
        HiveApp.appComponent.inject(this)
    }

    override fun init(onSignIn: () -> Unit, onSignOut: () -> Unit) {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser != null) {
                // User is signed in
                Timber.tag(TAG).d("Login successful")
                Timber.tag(TAG).d("User id = ${firebaseUser.uid}")
                Timber.tag(TAG).d("User name = ${firebaseUser.displayName}")
                Timber.tag(TAG).d("User email = ${firebaseUser.email}")
                Timber.tag(TAG).d("User pic URL = ${firebaseUser.photoUrl}")
                Timber.tag(TAG).d("User created at = ${firebaseUser.metadata?.creationTimestamp}")

                repo.onSignIn(convertFirebaseUser(firebaseUser))
                onSignIn()

            } else {
                // User is signed out
                Timber.tag(TAG).d("User signed out")

                repo.onSignOut()
                onSignOut()
            }
        }
    }

    override fun showLoginScreen(activity: Activity, resultCode: Int) {
        if (network.isOnline()) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build()
            )

            activity.startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(true)
                    .setAvailableProviders(providers)
                    .setTheme(R.style.AuthTheme)
                    .setLogo(R.drawable.hive_icon_transparent)
                    .build(),
                resultCode
            )

        } else {
            showNoNetworkDialog(activity, resultCode)
        }
    }

    override fun startListenAuth() = firebaseAuth.addAuthStateListener(authStateListener)

    override fun stopListenAuth() = firebaseAuth.removeAuthStateListener(authStateListener)

    override fun dismissDialogs() = dismissNoNetworkDialog() ?: Unit

    override fun signOut(onSuccess: () -> Unit, onError: () -> Unit) {
        // Set user offline status before logout
        repo.setUserOffline()

        AuthUI.getInstance()
            .signOut(context)
            .addOnSuccessListener {
                Timber.tag(TAG).d("Sign out successful")

                // Clear Facebook access token to be able
                // to sign in with Facebook as different person.
                // On some devices this does not help,
                // so to sign in with Facebook as different person,
                // first clear Chrome browser cache.
                AccessToken.setCurrentAccessToken(null)
                LoginManager.getInstance()?.logOut()

                onSuccess()
            }
            .addOnFailureListener {
                Timber.tag(TAG).d("Sign out error")
                onError()
            }
    }

    override fun deleteAccount(onSuccess: () -> Unit, onError: () -> Unit) {
        // Here we delete user from FirebaseAuth.
        // All user related data in Firestore will be deleted by the corresponding Cloud Function.
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

    // === Private methods ===

    private fun convertFirebaseUser(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: Constants.Auth.DEFAULT_USER_NAME,
            username = "",
            email = firebaseUser.email ?: Constants.Auth.DEFAULT_USER_MAIL,
            userPicUrl = firebaseUser.photoUrl?.toString() ?: "",
            description = "",
            isOnline = false,
            location = LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE),
            creationTimestamp = firebaseUser.metadata?.creationTimestamp ?: 0
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

    private fun dismissNoNetworkDialog() = noNetworkDialog?.dismiss()

    private fun closeApp() = System.exit(0)
}