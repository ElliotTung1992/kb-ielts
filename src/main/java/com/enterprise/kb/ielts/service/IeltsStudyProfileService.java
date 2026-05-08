package com.enterprise.kb.ielts.service;

import com.enterprise.kb.ielts.dto.PlanSuggestionResponse;
import com.enterprise.kb.ielts.model.IeltsStudyProfile;

public interface IeltsStudyProfileService {
    IeltsStudyProfile getProfile();
    IeltsStudyProfile saveProfile(IeltsStudyProfile profile);
    PlanSuggestionResponse getPlanSuggestion();
}
