package com.OdysseyAi.demo.auth.interfaces;

import com.OdysseyAi.demo.auth.entity.User;

public interface AuthenticationFacade {
    User getCurrentUser();
    Long getCurrentUserId();
}