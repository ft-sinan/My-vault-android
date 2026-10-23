package com.ftsinan.myvault.ui.screens.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ftsinan.myvault.R
import com.ftsinan.myvault.ui.components.DocumentCard
import com.ftsinan.myvault.ui.components.ExpressiveChip
import com.ftsinan.myvault.ui.components.HeaderCircleButton
import com.ftsinan.myvault.ui.components.MorphingShapeBox
import com.ftsinan.myvault.ui.components.expressiveReveal
import com.ftsinan.myvault.ui.components.EmptyState
import com.ftsinan.myvault.ui.components.MemberAvatar
import com.ftsinan.myvault.ui.components.memberIdentitySeed
import com.ftsinan.myvault.ui.components.bloodGroupLabel
import com.ftsinan.myvault.ui.components.genderLabel
import com.ftsinan.myvault.ui.components.relationLabel
import com.ftsinan.myvault.util.FormatUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    memberId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDocumentClick: (Long) -> Unit,
    viewModel: ProfileDetailViewModel = viewModel(
        factory = ProfileDetailViewModel.factory(memberId)
    )
) {
    val item by viewModel.memberItem.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun shareProfileImage() {
        scope.launch {
            val uri = viewModel.prepareShareImage() ?: return@launch
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val profile = item
        if (profile == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ProfileIdCard(
                    item = profile,
                    documentCount = documents.size,
                    onBack = onBack,
                    onShare = { shareProfileImage() },
                    onEdit = { onEdit(profile.member.id) },
                    modifier = Modifier.expressiveReveal(0, profile.member.id)
                )
            }
            item {
                Text(
                    stringResource(R.string.profile_documents),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (documents.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Description,
                        title = stringResource(R.string.profile_documents),
                        subtitle = stringResource(R.string.profile_no_documents),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                itemsIndexed(documents, key = { _, it -> it.document.id }) { index, summary ->
                    DocumentCard(
                        summary = summary,
                        onClick = { onDocumentClick(summary.document.id) },
                        modifier = Modifier.expressiveReveal(index + 1, summary.document.id)
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProfileIdCard(
    item: com.ftsinan.myvault.data.model.MemberListItem,
    documentCount: Int,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val member = item.member
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(gradient)
            .padding(18.dp)
    ) {
        MorphingShapeBox(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 42.dp, y = (-58).dp)
                .size(150.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.14f),
            lobes = 12,
            wave = 0.19f,
            spinMillis = 28_000
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeaderCircleButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    onClick = onBack,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                Spacer(Modifier.weight(1f))
                HeaderCircleButton(
                    icon = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.profile_share),
                    onClick = onShare,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
                Spacer(Modifier.width(8.dp))
                HeaderCircleButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.profile_edit),
                    onClick = onEdit,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f)
                )
            }

            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                MorphingShapeBox(
                    modifier = Modifier.size(146.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.30f),
                    lobes = 12,
                    wave = 0.19f,
                    spinMillis = 22_000
                )
                MemberAvatar(
                    name = member.name,
                    photoBytes = item.photoBytes,
                    size = 104.dp,
                    seedColor = memberIdentitySeed(member.id, member.name)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-4).dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = documentCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = member.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            ExpressiveChip(
                text = relationLabel(member.relation),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.66f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            val hasFields = member.dateOfBirth != null ||
                !member.gender.isNullOrBlank() ||
                !member.bloodGroup.isNullOrBlank()
            if (hasFields) {
                Spacer(Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    member.dateOfBirth?.let { dob ->
                        ProfileFieldRow(
                            label = stringResource(R.string.field_dob),
                            value = "${FormatUtils.formatDate(dob)} • " +
                                stringResource(R.string.profile_age_years, ageYears(dob))
                        )
                    }
                    member.gender?.takeIf { it.isNotBlank() }?.let {
                        ProfileFieldRow(stringResource(R.string.field_gender), genderLabel(it))
                    }
                    member.bloodGroup?.takeIf { it.isNotBlank() }?.let {
                        ProfileFieldRow(stringResource(R.string.field_blood_group), bloodGroupLabel(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun ageYears(dateOfBirth: Long): Int {
    val dob = Calendar.getInstance().apply { timeInMillis = dateOfBirth }
    val now = Calendar.getInstance()
    var age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
    if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--
    return age.coerceAtLeast(0)
}
