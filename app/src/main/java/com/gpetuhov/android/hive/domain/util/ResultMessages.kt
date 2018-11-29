package com.gpetuhov.android.hive.domain.util

interface ResultMessages {
    fun getSignOutErrorMessage(): String
    fun getSignOutNetworkErrorMessage(): String
    fun getDeleteUserSuccessMessage(): String
    fun getDeleteUserErrorMessage(): String
    fun getDeleteUserNetworkErrorMessage(): String
    fun getSaveUsernameErrorMessage(): String
    fun getSaveDescriptionErrorMessage(): String
    fun getSendMessageErrorMessage(): String
    fun getChangeUserPicErrorMessage(): String
}