package com.fpt.metroll.gateway.service;

import java.io.IOException;

public interface SecretStoreService {
    String getStatic(String secretUrl) throws IOException;
}
