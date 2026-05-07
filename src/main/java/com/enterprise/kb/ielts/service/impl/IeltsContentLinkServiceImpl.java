package com.enterprise.kb.ielts.service.impl;

import com.enterprise.kb.common.exception.ResourceExistException;
import com.enterprise.kb.common.exception.ResourceNotFoundException;
import com.enterprise.kb.ielts.dto.AddLinkRequest;
import com.enterprise.kb.ielts.dto.ContentLinkDto;
import com.enterprise.kb.ielts.mapper.IeltsContentLinkMapper;
import com.enterprise.kb.ielts.model.IeltsContentLink;
import com.enterprise.kb.ielts.service.IeltsContentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IeltsContentLinkServiceImpl implements IeltsContentLinkService {

    private final IeltsContentLinkMapper linkMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ContentLinkDto> listBySource(String sourceType, UUID sourceId) {
        return linkMapper.findBySource(sourceType, sourceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentLinkDto> listByTarget(String targetType, UUID targetId) {
        return linkMapper.findByTarget(targetType, targetId);
    }

    @Override
    @Transactional
    public IeltsContentLink addLink(String sourceType, UUID sourceId, AddLinkRequest request) {
        IeltsContentLink link = new IeltsContentLink();
        link.setId(UUID.randomUUID());
        link.setSourceType(sourceType);
        link.setSourceId(sourceId);
        link.setTargetType(request.targetType());
        link.setTargetId(request.targetId());
        link.setLinkType(request.linkType());
        link.setNote(request.note());
        link.setCreatedAt(Instant.now());
        try {
            linkMapper.insert(link);
        } catch (DuplicateKeyException e) {
            throw new ResourceExistException("关联已存在");
        }
        return link;
    }

    @Override
    @Transactional
    public void removeLink(UUID linkId) {
        linkMapper.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("IeltsContentLink", linkId));
        linkMapper.deleteById(linkId);
    }

    @Override
    @Transactional
    public void removeBySource(String sourceType, UUID sourceId) {
        linkMapper.deleteBySource(sourceType, sourceId);
    }

    @Override
    @Transactional
    public void removeByTarget(String targetType, UUID targetId) {
        linkMapper.deleteByTarget(targetType, targetId);
    }
}
