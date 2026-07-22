package com.example.farmi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmi.theme.CardBackground
import com.example.farmi.theme.CardBorder
import com.example.farmi.theme.IndigoPrimary

@Composable
fun AddAssetDialog(
    initialCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, subtitle: String, status: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var category by remember { mutableStateOf(initialCategory) }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Healthy") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Crops", "Cattle", "Garden", "Poultry & Eggs", "Birds & Bees", "Fish & Shrimp"
    )
    val statuses = listOf(
        "Planted", "Growing", "Harvesting", "Harvested", "Healthy", "Active", "Under Observation", "Milking"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Asset",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Category Picker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Category", color = Color.Gray, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .clickable { categoryDropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category, color = Color.White)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.background(CardBackground).border(1.dp, CardBorder)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat, color = Color.White) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                        // Update default status logically
                                        status = when (cat) {
                                            "Crops" -> "Planted"
                                            "Cattle" -> "Healthy"
                                            else -> "Active"
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name / Tag Number") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Subtitle Input
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Breed / Variety / Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = IndigoPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = IndigoPrimary,
                        unfocusedLabelColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Status Picker
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Current Status", color = Color.Gray, fontSize = 12.sp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CardBackground)
                            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                            .clickable { statusDropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = status, color = Color.White)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false },
                            modifier = Modifier.background(CardBackground).border(1.dp, CardBorder)
                        ) {
                            statuses.forEach { stat ->
                                DropdownMenuItem(
                                    text = { Text(stat, color = Color.White) },
                                    onClick = {
                                        status = stat
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isNotEmpty()) {
                        onConfirm(category, title.trim(), subtitle.trim(), status)
                    }
                },
                enabled = title.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F734D),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF0F734D).copy(alpha = 0.5f)
                )
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}
