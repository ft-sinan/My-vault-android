package com.ftsinan.myvault.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.ftsinan.myvault.R
import com.ftsinan.myvault.data.model.AssignedGroup
import com.ftsinan.myvault.data.model.Category
import com.ftsinan.myvault.data.model.Relation

fun categoryIcon(category: Category): ImageVector = when (category) {
    Category.IDENTITY -> Icons.Filled.Badge
    Category.EDUCATION -> Icons.Filled.School
    Category.CERTIFICATES -> Icons.Filled.WorkspacePremium
    Category.VEHICLE -> Icons.Filled.DirectionsCar
    Category.BANKING -> Icons.Filled.AccountBalance
    Category.HEALTH -> Icons.Filled.HealthAndSafety
    Category.PROPERTY -> Icons.Filled.Home
    Category.OTHER -> Icons.Filled.Folder
}

fun categoryColor(category: Category): Color = when (category) {
    Category.IDENTITY -> Color(0xFF1565C0)
    Category.EDUCATION -> Color(0xFF6A1B9A)
    Category.CERTIFICATES -> Color(0xFF00838F)
    Category.VEHICLE -> Color(0xFFEF6C00)
    Category.BANKING -> Color(0xFF2E7D32)
    Category.HEALTH -> Color(0xFFC62828)
    Category.PROPERTY -> Color(0xFF5D4037)
    Category.OTHER -> Color(0xFF546E7A)
}

fun assignedGroupIcon(group: AssignedGroup): ImageVector = when (group) {
    AssignedGroup.FAMILY -> Icons.Filled.Groups
    AssignedGroup.VEHICLE -> Icons.Filled.DirectionsCar
    AssignedGroup.HOME -> Icons.Filled.Home
    AssignedGroup.OTHER -> Icons.Filled.Folder
}

@Composable
fun categoryLabel(category: Category): String = stringResource(
    when (category) {
        Category.IDENTITY -> R.string.category_identity
        Category.EDUCATION -> R.string.category_education
        Category.CERTIFICATES -> R.string.category_certificates
        Category.VEHICLE -> R.string.category_vehicle
        Category.BANKING -> R.string.category_banking
        Category.HEALTH -> R.string.category_health
        Category.PROPERTY -> R.string.category_property
        Category.OTHER -> R.string.category_other
    }
)

@Composable
fun relationLabel(relation: Relation): String = stringResource(
    when (relation) {
        Relation.SELF -> R.string.relation_self
        Relation.SPOUSE -> R.string.relation_spouse
        Relation.FATHER -> R.string.relation_father
        Relation.MOTHER -> R.string.relation_mother
        Relation.SON -> R.string.relation_son
        Relation.DAUGHTER -> R.string.relation_daughter
        Relation.FAMILY -> R.string.relation_family
        Relation.OTHER -> R.string.relation_other
    }
)

@Composable
fun assignedGroupLabel(group: AssignedGroup): String = stringResource(
    when (group) {
        AssignedGroup.FAMILY -> R.string.group_family
        AssignedGroup.VEHICLE -> R.string.group_vehicle
        AssignedGroup.HOME -> R.string.group_home
        AssignedGroup.OTHER -> R.string.group_other
    }
)

/** Gender is stored as a stable English key; display it in the app language. */
@Composable
fun genderLabel(gender: String): String = when (gender) {
    "Male" -> stringResource(R.string.gender_male)
    "Female" -> stringResource(R.string.gender_female)
    "Other" -> stringResource(R.string.gender_other)
    "Prefer not to say" -> stringResource(R.string.gender_prefer_not_to_say)
    else -> gender
}

/** Blood-group symbols are universal; only the "Unknown" key needs translation. */
@Composable
fun bloodGroupLabel(bloodGroup: String): String = if (bloodGroup == "Unknown") {
    stringResource(R.string.blood_group_unknown)
} else {
    bloodGroup
}
