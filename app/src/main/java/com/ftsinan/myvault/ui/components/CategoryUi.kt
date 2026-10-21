package com.ftsinan.myvault.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ftsinan.myvault.data.model.Category

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
