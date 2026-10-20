package com.yogesh.lenskart.feature.account

import androidx.lifecycle.viewModelScope
import com.yogesh.lenskart.core.common.AppResult
import com.yogesh.lenskart.core.common.Platform
import com.yogesh.lenskart.core.domain.model.ThemeMode
import com.yogesh.lenskart.core.domain.repository.AuthRepository
import com.yogesh.lenskart.core.domain.repository.SettingsRepository
import com.yogesh.lenskart.core.ui.BaseViewModel
import kotlinx.coroutines.launch

class AccountViewModel(
    private val auth: AuthRepository,
    private val settings: SettingsRepository,
    platform: Platform,
) : BaseViewModel<AccountUiState, Nothing>(
    AccountUiState(platformName = platform.name, platformVersion = platform.version),
), AccountActions {

    init {
        viewModelScope.launch { auth.observeSession().collect { s -> updateState { copy(session = s) } } }
        viewModelScope.launch { settings.observeThemeMode().collect { m -> updateState { copy(themeMode = m) } } }
    }

    override fun onPhoneChanged(value: String) =
        updateState { copy(phone = value.filter { it.isDigit() }.take(10), error = null) }

    override fun onOtpChanged(value: String) =
        updateState { copy(otp = value.filter { it.isDigit() }.take(6), error = null) }

    override fun onSendOtp() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            when (val r = auth.requestOtp(uiState.value.phone)) {
                is AppResult.Success -> updateState { copy(step = LoginStep.OTP, isLoading = false) }
                is AppResult.Failure -> updateState { copy(isLoading = false, error = r.error.message) }
            }
        }
    }

    override fun onVerifyOtp() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            when (val r = auth.verifyOtp(uiState.value.phone, uiState.value.otp)) {
                is AppResult.Success -> updateState { copy(isLoading = false, step = LoginStep.PHONE, phone = "", otp = "") }
                is AppResult.Failure -> updateState { copy(isLoading = false, error = r.error.message) }
            }
        }
    }

    override fun onChangeNumber() = updateState { copy(step = LoginStep.PHONE, otp = "", error = null) }

    override fun onLogout() { viewModelScope.launch { auth.logout() } }

    override fun onThemeSelected(mode: ThemeMode) { viewModelScope.launch { settings.setThemeMode(mode) } }
}
