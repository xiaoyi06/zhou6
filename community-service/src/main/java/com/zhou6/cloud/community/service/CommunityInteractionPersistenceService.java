package com.zhou6.cloud.community.service;

import com.zhou6.cloud.community.mq.CommunityInteractionMessage;

public interface CommunityInteractionPersistenceService {

    void persist(CommunityInteractionMessage message);
}
