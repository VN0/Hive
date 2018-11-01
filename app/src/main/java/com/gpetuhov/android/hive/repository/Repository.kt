package com.gpetuhov.android.hive.repository

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.*
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.util.Constants
import timber.log.Timber
import java.util.*
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.managers.LocationManager
import org.imperiumlabs.geofirestore.GeoFirestore

// Read and write data to remote storage (Firestore)
class Repository : Repo {

    companion object {
        private const val TAG = "Repo"
        private const val USERS_COLLECTION = "users"
        private const val NAME_KEY = "name"
        private const val USERNAME_KEY = "username"
        private const val EMAIL_KEY = "email"
        private const val SERVICE_KEY = "service"
        private const val IS_VISIBLE_KEY = "is_visible"
        private const val IS_ONLINE_KEY = "is_online"
        private const val LOCATION_KEY = "l"
    }

    // Firestore is the single source of truth for the currentUser property.
    // currentUser is updated every time we write data to the corresponding
    // Firestore document (with the uid of the current user).
    // Current user is wrapped inside LiveData
    // so that the UI can easily observe changes and update itself.
    // So the sequence of updates is:
    // 1. Write data to Firestore
    // 2. currentUser is updated
    // 3. UI that observes currentUser (through ViewModel) is updated
    private val currentUser = MutableLiveData<User>()

    // For searchResultList the single source of truth is also Firestore.
    // Sequence of updates:
    // 1. Data in Firestore is updated
    // 2. searchResultList is updated
    // 3. UI the observes searchResultList is updated
    private val searchResultList = MutableLiveData<MutableList<User>>()

    private var isAuthorized = false
    private var currentUserUid: String = ""
    private val firestore = FirebaseFirestore.getInstance()
    private var searchResultListenerRegistration: ListenerRegistration? = null
    private var currentUserListenerRegistration: ListenerRegistration? = null

    init {
        // Offline data caching is enabled by default in Android.
        // But we enable it explicitly to be sure.
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        firestore.firestoreSettings = settings

        resetCurrentUser()
        clearResultList()
    }

    override fun onSignIn(user: User) {
        if (!isAuthorized) {
            isAuthorized = true

            // Current user's uid initially comes from Firebase Auth,
            // so we must save it to start getting updates
            // for the corresponding documents in Firestore.
            currentUserUid = user.uid
            startGettingCurrentUserRemoteUpdates()

            // Current user's name and email initially come from Firebase Auth,
            // so after successful sign in we must write them to Firestore.
            saveUserNameAndEmail(user)
        }
    }

    override fun onSignOut() {
        if (isAuthorized) {
            isAuthorized = false
            stopGettingCurrentUserRemoteUpdates()
            stopGettingSearchResultUpdates()
            resetCurrentUser()
        }
    }

    override fun currentUser() = currentUser

    override fun currentUserUsername() = currentUser.value?.username ?: ""

    override fun currentUserService() = currentUser.value?.service ?: ""

    override fun saveUserUsername(newUsername: String, onError: () -> Unit) {
        val data = HashMap<String, Any>()
        data[USERNAME_KEY] = newUsername

        saveUserDataRemote(data, { /* Do nothing */ }, onError)
    }

    override fun saveUserService(newService: String, onError: () -> Unit) {
        val data = HashMap<String, Any>()
        data[SERVICE_KEY] = newService

        saveUserDataRemote(data, { /* Do nothing */ }, onError)
    }

    override fun deleteUserService(onError: () -> Unit) {
        val data = HashMap<String, Any>()
        data[SERVICE_KEY] = ""
        data[IS_VISIBLE_KEY] = false

        saveUserDataRemote(data, { /* Do nothing */ }, onError)
    }

    override fun saveUserVisibility(newIsVisible: Boolean, onError: () -> Unit) {
        val data = HashMap<String, Any>()
        data[IS_VISIBLE_KEY] = newIsVisible

        saveUserDataRemote(data, { /* Do nothing */ }, onError)
    }

    override fun saveUserLocation(newLocation: LatLng) {
        val geoFirestore = GeoFirestore(firestore.collection(USERS_COLLECTION))
        geoFirestore.setLocation(currentUserUid, GeoPoint(newLocation.latitude, newLocation.longitude))
    }

    override fun saveUserOnlineStatus(newIsOnline: Boolean, onComplete: () -> Unit) {
        val data = HashMap<String, Any>()
        data[IS_ONLINE_KEY] = newIsOnline

        saveUserDataRemote(data, onComplete, onComplete)
    }

    override fun deleteUserDataRemote(onSuccess: () -> Unit, onError: () -> Unit) {
        if (isAuthorized) {
            firestore.collection(USERS_COLLECTION).document(currentUserUid)
                .delete()
                .addOnSuccessListener {
                    Timber.tag(TAG).d("User data successfully deleted")
                    onSuccess()

                }
                .addOnFailureListener {
                    Timber.tag(TAG).d("Error deleting user data")
                    onError()
                }

        } else {
            onError()
        }
    }

    override fun searchResultList() = searchResultList

    override fun search(queryText: String, onComplete: () -> Unit) {
        if (isAuthorized) {
            stopGettingSearchResultUpdates()

            var query = firestore.collection(USERS_COLLECTION)
                .whereEqualTo(IS_VISIBLE_KEY, true)

            if (queryText != "") query = query.whereEqualTo(SERVICE_KEY, queryText)

            searchResultListenerRegistration = query
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    val newResultList = mutableListOf<User>()

                    if (firebaseFirestoreException == null) {
                        if (querySnapshot != null) {
                            Timber.tag(TAG).d("Listen success")

                            for (doc in querySnapshot) {
                                if (doc.id != currentUser.value?.uid) {
                                    newResultList.add(getUserFromDocumentSnapshot(doc))
                                }
                            }

                        } else {
                            Timber.tag(TAG).d("Listen failed")
                        }

                    } else {
                        Timber.tag(TAG).d(firebaseFirestoreException)
                    }

                    searchResultList.value = newResultList
                    onComplete()
                }

        } else {
            onComplete()
        }
    }

    override fun stopGettingSearchResultUpdates() = searchResultListenerRegistration?.remove() ?: Unit

    // === Private methods ===

    private fun resetCurrentUser() {
        currentUser.value = createAnonymousUser()
        currentUserUid = ""
    }

    private fun clearResultList() {
        searchResultList.value = mutableListOf()
    }

    private fun createAnonymousUser(): User {
        return User(
            uid = "",
            name = Constants.Auth.DEFAULT_USER_NAME,
            username = "",
            email = Constants.Auth.DEFAULT_USER_MAIL,
            service = "",
            isVisible = false,
            isOnline = false,
            location = LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE)
        )
    }

    private fun saveUserNameAndEmail(user: User) {
        val data = HashMap<String, Any>()
        data[NAME_KEY] = user.name
        data[EMAIL_KEY] = user.email

        saveUserDataRemote(data, { /* Do nothing */ }, { /* Do nothing */ })
    }

    private fun saveUserDataRemote(data: HashMap<String, Any>, onSuccess: () -> Unit, onError: () -> Unit) {
        if (isAuthorized) {
            firestore.collection(USERS_COLLECTION).document(currentUserUid)
                .set(data, SetOptions.merge())  // this is needed to update only the required data if the user exists
                .addOnSuccessListener {
                    Timber.tag(TAG).d("User data successfully written")
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    Timber.tag(TAG).d("Error writing user data")
                    onError()
                }

        } else {
            onError()
        }
    }

    private fun startGettingCurrentUserRemoteUpdates() {
        if (isAuthorized) {
            currentUserListenerRegistration = firestore.collection(USERS_COLLECTION).document(currentUserUid)
                .addSnapshotListener { snapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException == null) {
                        if (snapshot != null && snapshot.exists()) {
                            Timber.tag(TAG).d("Listen success")
                            val user = getUserFromDocumentSnapshot(snapshot)

                            // Turn off visibility if user is visible and has no services
                            // (this is needed in case service has been cleared on the backend)
                            if (!user.hasService && user.isVisible) saveUserVisibility(false) { /* Do nothing */ }

                            // If current user is visible, start sharing location,
                            // otherwise stop sharing.
                            LocationManager.shareLocation(user.isVisible)

                            currentUser.value = user

                        } else {
                            Timber.tag(TAG).d("Listen failed")
                        }

                    } else {
                        Timber.tag(TAG).d(firebaseFirestoreException)
                    }
                }
        }
    }

    private fun stopGettingCurrentUserRemoteUpdates() = currentUserListenerRegistration?.remove()

    private fun getUserFromDocumentSnapshot(doc: DocumentSnapshot): User {
        val coordinatesList = doc.get(LOCATION_KEY) as List<*>?

        val location = if (coordinatesList != null && coordinatesList.size == 2) {
            LatLng(coordinatesList[0] as Double, coordinatesList[1] as Double)
        } else {
            LatLng(Constants.Map.DEFAULT_LATITUDE, Constants.Map.DEFAULT_LONGITUDE)
        }

        return User(
            uid = doc.id,
            name = doc.getString(NAME_KEY) ?: Constants.Auth.DEFAULT_USER_NAME,
            username = doc.getString(USERNAME_KEY) ?: "",
            email = doc.getString(EMAIL_KEY) ?: Constants.Auth.DEFAULT_USER_MAIL,
            service = doc.getString(SERVICE_KEY) ?: "",
            isVisible = doc.getBoolean(IS_VISIBLE_KEY) ?: false,
            isOnline = doc.getBoolean(IS_ONLINE_KEY) ?: false,
            location = location
        )
    }
}