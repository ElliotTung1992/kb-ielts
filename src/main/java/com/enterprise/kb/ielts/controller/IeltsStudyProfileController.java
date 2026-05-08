package com.enterprise.kb.ielts.controller;

import com.enterprise.kb.common.dto.ApiResponse;
import com.enterprise.kb.ielts.dto.PlanSuggestionResponse;
import com.enterprise.kb.ielts.model.IeltsStudyProfile;
import com.enterprise.kb.ielts.service.IeltsStudyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ielts/profile")
@RequiredArgsConstructor
public class IeltsStudyProfileController {

    private final IeltsStudyProfileService profileService;

    @GetMapping
    public ApiResponse<IeltsStudyProfile> getProfile() {
        return ApiResponse.ok(profileService.getProfile());
    }

    @PutMapping
    public ApiResponse<IeltsStudyProfile> saveProfile(@RequestBody IeltsStudyProfile profile) {
        return ApiResponse.ok(profileService.saveProfile(profile), "备考档案已保存");
    }

    @GetMapping("/plan-suggestion")
    public ApiResponse<PlanSuggestionResponse> getPlanSuggestion() {
        return ApiResponse.ok(profileService.getPlanSuggestion());
    }
}
