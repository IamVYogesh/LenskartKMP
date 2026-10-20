package com.yogesh.lenskart.feature.account

import com.yogesh.lenskart.core.domain.model.Session
import com.yogesh.lenskart.core.domain.model.ThemeMode

enum class LoginStep { PHONE, OTP }

data class AccountUiState(
    val session: Session? = null,
    val step: LoginStep = LoginStep.PHONE,
    val phone: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val platformName: String = "",
    val platformVersion: String = "",
)

/** What the View can ask the ViewModel to do. The ViewModel implements it; screens depend only on this. */
interface AccountActions {
    fun onPhoneChanged(value: String)
    fun onOtpChanged(value: String)
    fun onSendOtp()
    fun onVerifyOtp()
    fun onChangeNumber()
    fun onLogout()
    fun onThemeSelected(mode: ThemeMode)
}
