package com.yogesh.lenskart.feature.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yogesh.lenskart.core.designsystem.LkButton
import com.yogesh.lenskart.core.designsystem.resources.*
import com.yogesh.lenskart.core.domain.model.ThemeMode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@Composable
fun AccountRoute(viewModel: AccountViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AccountScreen(state, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(state: AccountUiState, actions: AccountActions) {
    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(Res.string.account_title), fontWeight = FontWeight.Bold) }) },
        contentWindowInsets = WindowInsets.statusBars,
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            val session = state.session
            if (session == null) LoginCard(state, actions) else {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(Res.string.account_welcome, session.user.name ?: session.user.phone), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("+91 ${session.user.phone}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.account_theme), fontWeight = FontWeight.Bold)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { i, mode ->
                        SegmentedButton(
                            selected = state.themeMode == mode, onClick = { actions.onThemeSelected(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                        ) { Text(mode.displayName) }
                    }
                }
            }
            if (session != null) OutlinedButton({ actions.onLogout() }, Modifier.fillMaxWidth()) { Text(stringResource(Res.string.account_logout)) }
            Text(stringResource(Res.string.account_about), fontWeight = FontWeight.Bold)
            // value comes from expect/actual Platform (core:common)
            Text(stringResource(Res.string.account_running_on, state.platformName, state.platformVersion), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoginCard(state: AccountUiState, actions: AccountActions) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.account_sign_in), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            when (state.step) {
                LoginStep.PHONE -> {
                    OutlinedTextField(
                        state.phone, { actions.onPhoneChanged(it) }, Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.account_phone_label)) }, prefix = { Text("+91 ") },
                        singleLine = true, isError = state.error != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    )
                    LkButton(stringResource(Res.string.account_send_otp), { actions.onSendOtp() }, Modifier.fillMaxWidth(), enabled = state.phone.length == 10, loading = state.isLoading)
                }
                LoginStep.OTP -> {
                    OutlinedTextField(
                        state.otp, { actions.onOtpChanged(it) }, Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.account_otp_label)) }, supportingText = { Text(stringResource(Res.string.account_otp_hint)) },
                        singleLine = true, isError = state.error != null, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )
                    LkButton(stringResource(Res.string.account_verify), { actions.onVerifyOtp() }, Modifier.fillMaxWidth(), enabled = state.otp.length == 6, loading = state.isLoading)
                    TextButton({ actions.onChangeNumber() }) { Text(stringResource(Res.string.account_change_number)) }
                }
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

val accountModule = module { viewModelOf(::AccountViewModel) }
