package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.mapper.IeltsSpeakingMaterialMapper;
import com.enterprise.kb.ielts.model.IeltsSpeakingMaterial;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ielts/speaking-materials")
@RequiredArgsConstructor
public class IeltsSpeakingMaterialController {

    private final IeltsSpeakingMaterialMapper materialMapper;

    @GetMapping
    public ApiResponse<List<IeltsSpeakingMaterial>> list(@RequestParam(required = false) String category,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(materialMapper.findRecent(category, Math.min(Math.max(1, limit), 100)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IeltsSpeakingMaterial> create(@RequestBody IeltsSpeakingMaterial material) {
        Instant now = Instant.now();
        material.setId(UUID.randomUUID());
        material.setCreatedAt(now);
        material.setUpdatedAt(now);
        materialMapper.insert(material);
        return ApiResponse.ok(material, "口语素材已保存");
    }
}
