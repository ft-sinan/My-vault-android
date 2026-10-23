package com.ftsinan.myvault.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.settings.AccentColor
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.ThemeMode
import com.ftsinan.myvault.ui.components.ExpressiveChip
import com.ftsinan.myvault.ui.components.ExpressiveStat
import com.ftsinan.myvault.ui.components.HeaderCircleButton
import com.ftsinan.myvault.ui.components.BlobShape
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.VaultMark
import com.ftsinan.myvault.ui.components.expressivePastel
import com.ftsinan.myvault.ui.components.expressiveReveal
import com.ftsinan.myvault.ui.components.springPress
import com.ftsinan.myvault.ui.screens.lock.canUseBiometric
import com.ftsinan.myvault.ui.theme.LocalAnimationsEnabled
import com.ftsinan.myvault.ui.theme.isDarkScheme
import com.ftsinan.myvault.ui.theme.localSelectionHaptic
import com.ftsinan.myvault.ui.theme.vaultColorScheme
import com.ftsinan.myvault.util.FormatUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLock: () -> Unit,
    onManageMembers: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsStateWithLifecycle()
    val compactMode by viewModel.compactMode.collectAsStateWithLifecycle()
    val reduceAnimations by viewModel.reduceAnimations.collectAsStateWithLifecycle()
    val glassBottomBar by viewModel.glassBottomBar.collectAsStateWithLifecycle()
    val selectionHaptic = localSelectionHaptic()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val documentCount by viewModel.documentCount.collectAsStateWithLifecycle()
    val totalSize by viewModel.totalSize.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val biometricAvailable = remember { canUseBiometric(context) }
    val dynamicColorAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    // Clear the PIN fields once a change succeeded.
    LaunchedEffect(message) {
        if (message?.isError == false) {
            currentPin = ""
            newPin = ""
            confirmPin = ""
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sectionOffsets = remember { mutableStateMapOf<String, Int>() }
    fun sectionModifier(key: String): Modifier = Modifier.onGloballyPositioned { coordinates ->
        sectionOffsets[key] = coordinates.positionInParent().y.toInt()
    }
    fun jumpToSection(key: String) {
        val target = sectionOffsets[key] ?: return
        coroutineScope.launch {
            scrollState.animateScrollBy(((target - 10).coerceAtLeast(0) - scrollState.value).toFloat())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsHubHeader(
                onBack = onBack,
                documentCount = documentCount,
                biometricChecked = biometricEnabled && biometricAvailable,
                biometricEnabled = biometricAvailable,
                onBiometricChange = viewModel::setBiometricEnabled,
                dynamicChecked = dynamicColorEnabled && dynamicColorAvailable,
                dynamicEnabled = dynamicColorAvailable,
                onDynamicChange = viewModel::setDynamicColorEnabled,
                hapticsChecked = hapticsEnabled,
                onHapticsChange = viewModel::setHapticsEnabled,
                animationsChecked = !reduceAnimations,
                onAnimationsChange = { enabled -> viewModel.setReduceAnimations(!enabled) },
                compactChecked = compactMode,
                onCompactChange = viewModel::setCompactMode,
                glassChecked = glassBottomBar,
                onGlassChange = viewModel::setGlassBottomBar,
                themeMode = themeMode,
                accentColor = accentColor,
                languageName = language?.nativeName.orEmpty(),
                storageText = stringResource(
                    R.string.settings_storage_using,
                    FormatUtils.formatFileSize(totalSize)
                ),
                onJump = ::jumpToSection,
                modifier = Modifier.expressiveReveal(0, "settings-hub")
            )

            // ---- Language --------------------------------------------------
            SettingsCard(modifier = sectionModifier("language"), title = stringResource(R.string.settings_language), icon = { Icon(Icons.Filled.Language, null) }) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { option ->
                        FilterChip(
                            selected = language == option,
                            onClick = { onLanguageSelected(option) },
                            label = { Text(option.nativeName) }
                        )
                    }
                }
            }

            // ---- Family ----------------------------------------------------
            SettingsCard(modifier = sectionModifier("family"), title = stringResource(R.string.settings_family), icon = { Icon(Icons.Filled.Groups, null) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.manage_members_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            stringResource(R.string.settings_family_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onManageMembers) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
            }

            // ---- Appearance -------------------------------------------------
            SettingsCard(modifier = sectionModifier("appearance"), title = stringResource(R.string.settings_appearance), icon = { Icon(Icons.Filled.Palette, null) }) {
                Text(
                    stringResource(R.string.settings_theme_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeModePreviews(
                    selected = themeMode,
                    accentColor = accentColor,
                    dynamicColorEnabled = dynamicColorEnabled && dynamicColorAvailable,
                    onSelect = { mode ->
                        selectionHaptic()
                        viewModel.setThemeMode(mode)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_wallpaper_colors),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (dynamicColorAvailable) {
                                stringResource(R.string.settings_wallpaper_colors_subtitle)
                            } else {
                                stringResource(R.string.settings_wallpaper_colors_unavailable)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = dynamicColorEnabled && dynamicColorAvailable,
                        onCheckedChange = viewModel::setDynamicColorEnabled,
                        enabled = dynamicColorAvailable
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_brand_theme_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    stringResource(R.string.settings_colour_palette),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    stringResource(R.string.settings_accent_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                PaletteSwatches(
                    selected = accentColor,
                    dynamicColorActive = dynamicColorEnabled && dynamicColorAvailable,
                    onSelect = { accent ->
                        selectionHaptic()
                        viewModel.selectAccentColor(accent)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                AppearancePreview()
            }

            // ---- Motion & feel ------------------------------------------------
            SettingsCard(title = stringResource(R.string.settings_motion_feel), icon = { Icon(Icons.Filled.TouchApp, null) }) {
                FeelToggleRow(
                    icon = Icons.Filled.BlurOn,
                    title = stringResource(R.string.settings_glass_bottom_bar),
                    subtitle = stringResource(R.string.settings_glass_bottom_bar_subtitle),
                    checked = glassBottomBar,
                    onCheckedChange = viewModel::setGlassBottomBar
                )
                FeelToggleRow(
                    icon = Icons.Filled.Vibration,
                    title = stringResource(R.string.settings_haptics),
                    subtitle = stringResource(R.string.settings_haptics_subtitle),
                    checked = hapticsEnabled,
                    onCheckedChange = viewModel::setHapticsEnabled
                )
                FeelToggleRow(
                    icon = Icons.Filled.Compress,
                    title = stringResource(R.string.settings_compact_mode),
                    subtitle = stringResource(R.string.settings_compact_mode_subtitle),
                    checked = compactMode,
                    onCheckedChange = viewModel::setCompactMode
                )
                FeelToggleRow(
                    icon = Icons.Filled.Animation,
                    title = stringResource(R.string.settings_reduce_animations),
                    subtitle = stringResource(R.string.settings_reduce_animations_subtitle),
                    checked = reduceAnimations,
                    onCheckedChange = viewModel::setReduceAnimations,
                    showDivider = false
                )
            }

            // ---- Security -------------------------------------------------
            SettingsCard(modifier = sectionModifier("security"), title = stringResource(R.string.settings_security), icon = { Icon(Icons.Filled.Lock, null) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.settings_biometric_title),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (biometricAvailable) {
                                stringResource(R.string.settings_biometric_available)
                            } else {
                                stringResource(R.string.settings_biometric_unavailable)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled && biometricAvailable,
                        onCheckedChange = viewModel::setBiometricEnabled,
                        enabled = biometricAvailable
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.settings_change_pin),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                PinField(
                    value = currentPin,
                    onValueChange = { currentPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_current_pin)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PinField(
                    value = newPin,
                    onValueChange = { newPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_new_pin)
                )
                Spacer(modifier = Modifier.height(8.dp))
                PinField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it; viewModel.clearMessage() },
                    label = stringResource(R.string.settings_confirm_new_pin)
                )
                message?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (msg.formatArg != null) {
                            stringResource(msg.textRes, msg.formatArg)
                        } else {
                            stringResource(msg.textRes)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (msg.isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.changePin(currentPin, newPin, confirmPin) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_change_pin))
                }
            }

            // ---- Storage --------------------------------------------------
            SettingsCard(modifier = sectionModifier("storage"), title = stringResource(R.string.settings_storage), icon = { Icon(Icons.Filled.Storage, null) }) {
                Text(
                    text = if (documentCount == 1) {
                        stringResource(R.string.settings_storage_one)
                    } else {
                        stringResource(R.string.settings_storage_many, documentCount)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = stringResource(
                        R.string.settings_storage_using,
                        FormatUtils.formatFileSize(totalSize)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---- Privacy info ---------------------------------------------
            SettingsCard(title = stringResource(R.string.settings_how_safe), icon = { Icon(Icons.Filled.Info, null) }) {
                Text(
                    text = stringResource(R.string.settings_safety_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CreatorSpace(
                documentCount = documentCount,
                totalSize = totalSize,
                modifier = sectionModifier("creator")
            )

            OutlinedButton(
                onClick = onLock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.settings_lock_now))
            }

            Text(
                text = "My Vault v1.0.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MorphingShapeBox(
                    modifier = Modifier.size(42.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    lobes = 9,
                    wave = 0.13f,
                    spinMillis = 24_000
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content(this)
        }
    }
}

@Composable
private fun AppearancePreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.settings_appearance_preview),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
    }
}

@Composable
private fun PinField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            onValueChange(input.filter(Char::isDigit).take(PinManager.PIN_LENGTH))
        },
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun themeModeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.settings_theme_system
    ThemeMode.LIGHT -> R.string.settings_theme_light
    ThemeMode.DARK -> R.string.settings_theme_dark
    ThemeMode.OLED -> R.string.settings_theme_oled
}

private fun accentLabelRes(accent: AccentColor): Int = when (accent) {
    AccentColor.ORION -> R.string.accent_orion
    AccentColor.TEAL -> R.string.accent_teal
    AccentColor.NAVY -> R.string.accent_navy
    AccentColor.EMERALD -> R.string.accent_emerald
    AccentColor.INDIGO -> R.string.accent_indigo
    AccentColor.OCEAN -> R.string.accent_ocean
    AccentColor.GRAPE -> R.string.accent_grape
    AccentColor.COCOA -> R.string.accent_cocoa
    AccentColor.SLATE -> R.string.accent_slate
}

/**
 * Theme choices as little phones drawn from the real colour schemes,
 * Orion-style: System shows light and dark halves side by side.
 */
@Composable
private fun ThemeModePreviews(
    selected: ThemeMode,
    accentColor: AccentColor,
    dynamicColorEnabled: Boolean,
    onSelect: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val lightScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = false, oled = false, dynamicColorEnabled, accentColor)
    }
    val darkScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = true, oled = false, dynamicColorEnabled, accentColor)
    }
    val oledScheme = remember(context, accentColor, dynamicColorEnabled) {
        vaultColorScheme(context, darkTheme = true, oled = true, dynamicColorEnabled, accentColor)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val scheme = when (mode) {
                ThemeMode.SYSTEM, ThemeMode.LIGHT -> lightScheme
                ThemeMode.DARK -> darkScheme
                ThemeMode.OLED -> oledScheme
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiniPhonePreview(
                    scheme = scheme,
                    splitScheme = if (mode == ThemeMode.SYSTEM) darkScheme else null,
                    selected = selected == mode,
                    onClick = { onSelect(mode) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(themeModeLabelRes(mode)),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected == mode) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected == mode) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MiniPhonePreview(
    scheme: ColorScheme,
    splitScheme: ColorScheme?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val corner by animateDpAsState(
        targetValue = if (selected) 20.dp else 14.dp,
        animationSpec = if (animationsEnabled) tween(180) else snap(),
        label = "themePreviewCorner"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.62f)
            .clip(RoundedCornerShape(corner))
            .background(scheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = RoundedCornerShape(corner)
            )
            .clickable(onClick = onClick)
    ) {
        if (splitScheme != null) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(splitScheme.surface)
            )
        }
        Column(modifier = Modifier.fillMaxSize().padding(7.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.surfaceContainerHigh)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(scheme.primaryContainer)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(scheme.tertiaryContainer)
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.45f))
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.onSurface.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(50))
                    .background(scheme.primary)
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

/**
 * Palettes as split swatches (primary over secondary-container/tertiary),
 * shown in the current light/dark brightness. Picking one turns wallpaper
 * colours off so the palette applies immediately.
 */
@Composable
private fun PaletteSwatches(
    selected: AccentColor,
    dynamicColorActive: Boolean,
    onSelect: (AccentColor) -> Unit
) {
    val context = LocalContext.current
    val dark = MaterialTheme.colorScheme.isDarkScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AccentColor.entries.forEach { accent ->
            val scheme = remember(context, accent, dark) {
                vaultColorScheme(
                    context,
                    darkTheme = dark,
                    oled = false,
                    useDynamicColor = false,
                    accentColor = accent
                )
            }
            val isSelected = !dynamicColorActive && selected == accent
            Column(
                modifier = Modifier.width(62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .clickable { onSelect(accent) }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(scheme.primary)
                        )
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(scheme.secondaryContainer)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(scheme.tertiary)
                            )
                        }
                    }
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(scheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = scheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(accentLabelRes(accent)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeelToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    if (showDivider) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
    }
}


/** The Orion-style settings hub: quick-toggle pills and pastel bento shortcuts. */
@Composable
private fun SettingsHubHeader(
    onBack: () -> Unit,
    documentCount: Int,
    biometricChecked: Boolean,
    biometricEnabled: Boolean,
    onBiometricChange: (Boolean) -> Unit,
    dynamicChecked: Boolean,
    dynamicEnabled: Boolean,
    onDynamicChange: (Boolean) -> Unit,
    hapticsChecked: Boolean,
    onHapticsChange: (Boolean) -> Unit,
    animationsChecked: Boolean,
    onAnimationsChange: (Boolean) -> Unit,
    compactChecked: Boolean,
    onCompactChange: (Boolean) -> Unit,
    glassChecked: Boolean,
    onGlassChange: (Boolean) -> Unit,
    themeMode: ThemeMode,
    accentColor: AccentColor,
    languageName: String,
    storageText: String,
    onJump: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack
            )
            Spacer(Modifier.weight(1f))
            VaultMark(size = 42.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.tab_settings),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "My Vault v1.0.0 · " + if (documentCount == 1) {
                stringResource(R.string.settings_storage_one)
            } else {
                stringResource(R.string.settings_storage_many, documentCount)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_quick_toggles),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Fingerprint,
                label = stringResource(R.string.settings_quick_biometric),
                checked = biometricChecked,
                enabled = biometricEnabled,
                onCheckedChange = onBiometricChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.Palette,
                label = stringResource(R.string.settings_quick_wallpaper),
                checked = dynamicChecked,
                enabled = dynamicEnabled,
                onCheckedChange = onDynamicChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Vibration,
                label = stringResource(R.string.settings_haptics),
                checked = hapticsChecked,
                onCheckedChange = onHapticsChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.BlurOn,
                label = stringResource(R.string.settings_glass_bottom_bar),
                checked = glassChecked,
                onCheckedChange = onGlassChange,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickToggleTile(
                icon = Icons.Filled.Animation,
                label = stringResource(R.string.settings_quick_animations),
                checked = animationsChecked,
                onCheckedChange = onAnimationsChange,
                modifier = Modifier.weight(1f)
            )
            QuickToggleTile(
                icon = Icons.Filled.Compress,
                label = stringResource(R.string.settings_compact_mode),
                checked = compactChecked,
                onCheckedChange = onCompactChange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.settings_everything_else),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        SettingsBentoCard(
            icon = Icons.Filled.Palette,
            title = stringResource(R.string.settings_appearance),
            subtitle = if (dynamicChecked) {
                "${stringResource(R.string.settings_quick_wallpaper)} · ${stringResource(themeModeLabelRes(themeMode))}"
            } else {
                "${stringResource(accentLabelRes(accentColor))} · ${stringResource(themeModeLabelRes(themeMode))}"
            },
            seed = Color(0xFF6B48BC),
            onClick = { onJump("appearance") },
            modifier = Modifier.fillMaxWidth().height(118.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(196.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsBentoCard(
                icon = Icons.Filled.Lock,
                title = stringResource(R.string.settings_security),
                subtitle = stringResource(R.string.settings_biometric_title),
                seed = Color(0xFF087A4E),
                onClick = { onJump("security") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsBentoCard(
                    icon = Icons.Filled.Groups,
                    title = stringResource(R.string.settings_family),
                    subtitle = stringResource(R.string.settings_family_subtitle),
                    seed = Color(0xFF0077A8),
                    onClick = { onJump("family") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
                SettingsBentoCard(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = languageName,
                    seed = Color(0xFF4C4ECD),
                    onClick = { onJump("language") },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(118.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsBentoCard(
                icon = Icons.Filled.Storage,
                title = stringResource(R.string.settings_storage),
                subtitle = storageText,
                seed = Color(0xFFC65D00),
                onClick = { onJump("storage") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            SettingsBentoCard(
                icon = Icons.Filled.Star,
                title = stringResource(R.string.creator_title),
                subtitle = stringResource(R.string.creator_name),
                seed = Color(0xFFB03070),
                onClick = { onJump("creator") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun QuickToggleTile(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val animationsEnabled = LocalAnimationsEnabled.current
    val corner by animateDpAsState(
        targetValue = if (checked) 36.dp else 22.dp,
        animationSpec = if (animationsEnabled) {
            spring(dampingRatio = 0.68f, stiffness = 420f)
        } else {
            snap()
        },
        label = "quickToggleCorner"
    )
    val shape = RoundedCornerShape(corner)
    val container by animateColorAsState(
        targetValue = if (checked) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = if (animationsEnabled) tween(220) else snap(),
        label = "quickToggleContainer"
    )
    val contentColor = if (checked) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = modifier
            .height(78.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.48f }
            .clip(shape)
            .background(container)
            .springPress(shape, enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(21.dp))
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(
                        if (checked) R.string.settings_toggle_on else R.string.settings_toggle_off
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.82f)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingsBentoCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    seed: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(expressivePastel(seed))
            .springPress(shape, onClick = onClick)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 28.dp, y = (-32).dp)
                .size(92.dp),
            color = seed.copy(alpha = 0.17f),
            lobes = 12,
            wave = 0.18f,
            spinMillis = 26_000
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            MorphingShapeBox(
                modifier = Modifier.size(42.dp),
                color = seed.copy(alpha = 0.24f),
                lobes = 9,
                wave = 0.13f,
                spinMillis = 22_000
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** A complete creator area modelled on Orion's About tab, including its flipping coin. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreatorSpace(
    documentCount: Int,
    totalSize: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animationsEnabled = LocalAnimationsEnabled.current
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = if (animationsEnabled) tween(680) else snap(),
        label = "creatorCoinFlip"
    )
    LaunchedEffect(flipped) {
        if (flipped) {
            delay(2_400)
            flipped = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .padding(20.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 54.dp, y = (-70).dp)
                .size(180.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            lobes = 12,
            wave = 0.19f,
            spinMillis = 30_000
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExpressiveChip(
                text = stringResource(R.string.creator_title).uppercase(),
                icon = Icons.Filled.Star,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                modifier = Modifier.align(Alignment.Start)
            )

            Box(
                modifier = Modifier.size(width = 230.dp, height = 190.dp),
                contentAlignment = Alignment.Center
            ) {
                MorphingShapeBox(
                    modifier = Modifier.size(178.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.58f),
                    lobes = 12,
                    wave = 0.19f,
                    spinMillis = 24_000
                )
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 18.dp.toPx()
                        }
                        .springPress(BlobShape(lobes = 12, wave = 0.16f)) { flipped = !flipped },
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        MorphingShapeBox(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            lobes = 12,
                            wave = 0.16f,
                            spinMillis = 18_000
                        ) {
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .clip(BlobShape(lobes = 12, wave = 0.16f))
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
                CreatorOrbitBadge(
                    icon = Icons.Filled.Star,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 16.dp, y = 18.dp)
                )
                CreatorOrbitBadge(
                    icon = Icons.Filled.Code,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-4).dp)
                )
                CreatorOrbitBadge(
                    icon = Icons.Filled.Shield,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 28.dp, y = (-8).dp)
                )
            }

            Text(
                text = if (flipped) {
                    stringResource(R.string.creator_back_title)
                } else {
                    stringResource(R.string.creator_name)
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (flipped) {
                    stringResource(R.string.creator_back_subtitle)
                } else {
                    "@ft-sinan"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.creator_role),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.creator_bio),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.creator_tap_emblem),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveChip(
                    text = stringResource(R.string.creator_badge_creator),
                    icon = Icons.Filled.Star,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
                ExpressiveChip(
                    text = stringResource(R.string.creator_badge_privacy),
                    icon = Icons.Filled.Shield,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
                ExpressiveChip(
                    text = "Kotlin · Android",
                    icon = Icons.Filled.Code,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f))
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpressiveStat(
                    value = "1.0.0",
                    label = stringResource(R.string.creator_version),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = documentCount.toString(),
                    label = stringResource(R.string.stat_docs),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(34.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                ExpressiveStat(
                    value = FormatUtils.formatFileSize(totalSize),
                    label = stringResource(R.string.settings_storage),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.creator_connect),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(10.dp))
            val githubShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .clip(githubShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
                    .springPress(githubShape) {
                        runCatching {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ft-sinan"))
                            )
                        }
                    }
                    .padding(horizontal = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MorphingShapeBox(
                        modifier = Modifier.size(42.dp),
                        color = MaterialTheme.colorScheme.primary,
                        lobes = 9,
                        wave = 0.13f
                    ) {
                        Icon(
                            Icons.Filled.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.creator_github),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "github.com/ft-sinan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Filled.OpenInNew, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun CreatorOrbitBadge(icon: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(19.dp)
        )
    }
}
