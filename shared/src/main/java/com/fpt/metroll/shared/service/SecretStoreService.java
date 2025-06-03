package com.fpt.metroll.shared.service;

import java.io.IOException;

public interface SecretStoreService {
    String getStatic(String secretUrl) throws IOException;
}
