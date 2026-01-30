package com.lms.learningpath.service.impl;

import com.lms.content.common.entity.Folder;
import com.lms.content.common.repository.FolderRepository;
import com.lms.learningpath.repository.UserSectionProgressRepository;
import com.lms.learningpath.service.UnlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of unlock logic for learning sections.
 * Uses folder ordering and completion status to determine unlock state.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnlockServiceImpl implements UnlockService {

    private final FolderRepository folderRepository;
    private final UserSectionProgressRepository progressRepository;

    @Override
    public boolean isSectionUnlocked(String userId, String folderId) {
        // Get the folder to find its package and order
        Optional<Folder> folderOpt = folderRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            log.warn("Folder not found: {}", folderId);
            return false;
        }

        Folder folder = folderOpt.get();
        String packageId = folder.getPackageEntity().getId();

        // Get all folders in the package, ordered
        List<Folder> allFolders = folderRepository.findByPackageEntityIdOrderByCreatedAtAsc(packageId);

        // Find the index of current folder
        int currentIndex = -1;
        for (int i = 0; i < allFolders.size(); i++) {
            if (allFolders.get(i).getId().equals(folderId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex == -1) {
            return false;
        }

        // First section is always unlocked
        if (currentIndex == 0) {
            return true;
        }

        // Check if previous section is completed
        Folder previousFolder = allFolders.get(currentIndex - 1);
        return progressRepository.existsByUserIdAndFolderIdAndIsCompletedTrue(userId, previousFolder.getId());
    }

    @Override
    public String getPrerequisiteSection(String folderId) {
        Optional<Folder> folderOpt = folderRepository.findById(folderId);
        if (folderOpt.isEmpty()) {
            return null;
        }

        Folder folder = folderOpt.get();
        String packageId = folder.getPackageEntity().getId();

        List<Folder> allFolders = folderRepository.findByPackageEntityIdOrderByCreatedAtAsc(packageId);

        int currentIndex = -1;
        for (int i = 0; i < allFolders.size(); i++) {
            if (allFolders.get(i).getId().equals(folderId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex <= 0) {
            return null; // No prerequisite for first section
        }

        return allFolders.get(currentIndex - 1).getId();
    }

    @Override
    @Transactional
    public void unlockNextSection(String userId, String packageId, String completedFolderId) {
        // Get all folders in package
        List<Folder> allFolders = folderRepository.findByPackageEntityIdOrderByCreatedAtAsc(packageId);

        // Find completed folder index
        int completedIndex = -1;
        for (int i = 0; i < allFolders.size(); i++) {
            if (allFolders.get(i).getId().equals(completedFolderId)) {
                completedIndex = i;
                break;
            }
        }

        if (completedIndex >= 0 && completedIndex < allFolders.size() - 1) {
            // Next section exists and should be unlocked
            Folder nextFolder = allFolders.get(completedIndex + 1);
            log.info("Section {} completed by user {}. Next section {} is now unlocked",
                    completedFolderId, userId, nextFolder.getId());
        } else {
            log.info("Section {} completed by user {}. This was the last section in the learning path",
                    completedFolderId, userId);
        }
    }
}
