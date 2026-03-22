package com.opencontacts.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.opencontacts.core.model.ContactSummary
import com.opencontacts.core.model.FolderSummary
import com.opencontacts.core.model.TagSummary
import com.opencontacts.core.vault.VaultSessionManager
import com.opencontacts.domain.contacts.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private enum class WorkspaceSelectionType { TAG, FOLDER }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkspaceRoute(
    onBack: () -> Unit,
    viewModel: WorkspaceViewModel = hiltViewModel(),
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    var newTag by remember { mutableStateOf<String?>(null) }
    var newFolder by remember { mutableStateOf<String?>(null) }
    var editTag by remember { mutableStateOf<String?>(null) }
    var editFolder by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }

    val filteredContacts = remember(contacts, selectedTag, selectedFolder) {
        when {
            selectedTag != null -> contacts.filter { selectedTag in it.tags }
            selectedFolder != null -> contacts.filter { it.folderName == selectedFolder }
            else -> emptyList()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CardDefaults.elevatedShape,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Groups & folders", style = MaterialTheme.typography.headlineMedium)
                    Text("Manage vault-local tags and folders. Tap any tag or folder to view the contacts classified under it.")
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Text("Folders", style = MaterialTheme.typography.titleLarge)
                        }
                        Row {
                            if (selectedFolder != null) {
                                IconButton(onClick = { editFolder = selectedFolder }) { Icon(Icons.Default.Edit, contentDescription = "Edit folder") }
                                IconButton(onClick = { viewModel.deleteFolder(selectedFolder!!); selectedFolder = null }) { Icon(Icons.Default.Delete, contentDescription = "Delete folder") }
                            }
                            IconButton(onClick = { newFolder = "" }) { Icon(Icons.Default.Add, contentDescription = "Add folder") }
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        folders.ifEmpty { listOf(FolderSummary("Personal"), FolderSummary("Work"), FolderSummary("Medical")) }.forEach { folder ->
                            FilterChip(
                                selected = selectedFolder == folder.name,
                                onClick = {
                                    selectedFolder = if (selectedFolder == folder.name) null else folder.name
                                    if (selectedFolder != null) selectedTag = null
                                },
                                label = { Text(folder.name) },
                                leadingIcon = { Icon(Icons.Default.Folder, null) },
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, contentDescription = null)
                            Text("Tags", style = MaterialTheme.typography.titleLarge)
                        }
                        Row {
                            if (selectedTag != null) {
                                IconButton(onClick = { editTag = selectedTag }) { Icon(Icons.Default.Edit, contentDescription = "Edit tag") }
                                IconButton(onClick = { viewModel.deleteTag(selectedTag!!); selectedTag = null }) { Icon(Icons.Default.Delete, contentDescription = "Delete tag") }
                            }
                            IconButton(onClick = { newTag = "" }) { Icon(Icons.Default.Add, contentDescription = "Add tag") }
                        }
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag.name,
                                onClick = {
                                    selectedTag = if (selectedTag == tag.name) null else tag.name
                                    if (selectedTag != null) selectedFolder = null
                                },
                                label = { Text(tag.name) },
                                leadingIcon = { Icon(Icons.Default.Label, null) },
                            )
                        }
                    }
                }
            }

            if (selectedTag != null || selectedFolder != null) {
                val title = selectedTag ?: selectedFolder ?: "Items"
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text("${filteredContacts.size} contact(s)")
                        if (filteredContacts.isEmpty()) {
                            Text("No contacts are classified under this item yet.")
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                items(filteredContacts, key = { it.id }) { contact ->
                                    ContactMiniCard(contact)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    newTag?.let { value ->
        NameDialog(
            title = "New tag",
            value = value,
            label = "Tag name",
            onValueChange = { newTag = it },
            onDismiss = { newTag = null },
            onConfirm = {
                val clean = value.trim()
                if (clean.isNotBlank()) viewModel.saveTag(clean)
                newTag = null
            },
        )
    }
    newFolder?.let { value ->
        NameDialog(
            title = "New folder",
            value = value,
            label = "Folder name",
            onValueChange = { newFolder = it },
            onDismiss = { newFolder = null },
            onConfirm = {
                val clean = value.trim()
                if (clean.isNotBlank()) viewModel.saveFolder(clean)
                newFolder = null
            },
        )
    }
    editTag?.let { value ->
        NameDialog(
            title = "Edit tag",
            value = value,
            label = "Tag name",
            onValueChange = { editTag = it },
            onDismiss = { editTag = null },
            onConfirm = {
                val old = selectedTag
                val clean = value.trim()
                if (old != null && clean.isNotBlank()) viewModel.renameTag(old, clean)
                selectedTag = clean.ifBlank { old }
                editTag = null
            },
        )
    }
    editFolder?.let { value ->
        NameDialog(
            title = "Edit folder",
            value = value,
            label = "Folder name",
            onValueChange = { editFolder = it },
            onDismiss = { editFolder = null },
            onConfirm = {
                val old = selectedFolder
                val clean = value.trim()
                if (old != null && clean.isNotBlank()) viewModel.renameFolder(old, clean)
                selectedFolder = clean.ifBlank { old }
                editFolder = null
            },
        )
    }
}

@Composable
private fun ContactMiniCard(contact: ContactSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(contact.displayName, style = MaterialTheme.typography.titleMedium)
            Text(contact.primaryPhone ?: "No phone", style = MaterialTheme.typography.bodyMedium)
            if (contact.tags.isNotEmpty()) Text(contact.tags.joinToString(), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun NameDialog(title: String, value: String, label: String, onValueChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, singleLine = true) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkspaceViewModel @Inject constructor(
    private val vaultSessionManager: VaultSessionManager,
    private val contactRepository: ContactRepository,
) : ViewModel() {
    val tags = vaultSessionManager.activeVaultId
        .combine(vaultSessionManager.isLocked) { vaultId, isLocked -> vaultId to isLocked }
        .flatMapLatest { (vaultId, isLocked) ->
            if (vaultId == null || isLocked) flowOf(emptyList()) else contactRepository.observeTags(vaultId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val folders = vaultSessionManager.activeVaultId
        .combine(vaultSessionManager.isLocked) { vaultId, isLocked -> vaultId to isLocked }
        .flatMapLatest { (vaultId, isLocked) ->
            if (vaultId == null || isLocked) flowOf(emptyList()) else contactRepository.observeFolders(vaultId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val contacts = vaultSessionManager.activeVaultId
        .combine(vaultSessionManager.isLocked) { vaultId, isLocked -> vaultId to isLocked }
        .flatMapLatest { (vaultId, isLocked) ->
            if (vaultId == null || isLocked) flowOf(emptyList()) else contactRepository.observeContacts(vaultId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveTag(name: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch { contactRepository.upsertTag(vaultId, TagSummary(name = name)) }
    }

    fun saveFolder(name: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch { contactRepository.upsertFolder(vaultId, FolderSummary(name = name)) }
    }

    fun deleteTag(name: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch { contactRepository.deleteTag(vaultId, name) }
    }

    fun deleteFolder(name: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch { contactRepository.deleteFolder(vaultId, name) }
    }

    fun renameTag(oldName: String, newName: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch {
            if (oldName != newName) {
                contactRepository.upsertTag(vaultId, TagSummary(name = newName))
                contactRepository.deleteTag(vaultId, oldName)
            }
        }
    }

    fun renameFolder(oldName: String, newName: String) {
        val vaultId = vaultSessionManager.activeVaultId.value ?: return
        viewModelScope.launch {
            if (oldName != newName) {
                contactRepository.upsertFolder(vaultId, FolderSummary(name = newName))
                contactRepository.deleteFolder(vaultId, oldName)
            }
        }
    }
}
