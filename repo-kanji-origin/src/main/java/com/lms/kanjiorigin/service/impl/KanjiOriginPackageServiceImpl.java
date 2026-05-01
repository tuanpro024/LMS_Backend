package com.lms.kanjiorigin.service.impl;

import com.lms.common.http.TicketAccessClient;
import com.lms.content.common.entity.Package;
import com.lms.content.common.mapper.PackageMapper;
import com.lms.content.common.repository.FolderRepository;
import com.lms.content.common.repository.PackageRepository;
import com.lms.content.common.repository.TypeRepository;
import com.lms.content.common.service.FolderService;
import com.lms.content.common.service.impl.PackageServiceImpl;
import com.lms.content.common.service.impl.StudySetDeletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kanji-origin specific override for Package deletion.
 *
 * Deletes dependent rows in subjects before deleting package to avoid FK
 * violations.
 */
@Service
@Primary
@Slf4j
@Transactional
public class KanjiOriginPackageServiceImpl extends PackageServiceImpl {

    private final JdbcTemplate jdbcTemplate;

    public KanjiOriginPackageServiceImpl(
            PackageRepository packageRepository,
            FolderRepository folderRepository,
            PackageMapper packageMapper,
            TypeRepository typeRepository,
            FolderService folderService,
            StudySetDeletionService studySetDeletionService,
            TicketAccessClient ticketAccessClient,
            JdbcTemplate jdbcTemplate) {
        super(packageRepository, folderRepository, packageMapper, typeRepository, folderService,
                studySetDeletionService, ticketAccessClient);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void beforeDeletePackage(Package entity, String userId) {
        String packageId = entity.getId();
        int deletedSubjects = jdbcTemplate.update("delete from subjects where package_id = ?", packageId);
        if (deletedSubjects > 0) {
            log.info("[KanjiOriginPackageServiceImpl] Deleted {} subjects for packageId={}", deletedSubjects,
                    packageId);
        }
    }
}
