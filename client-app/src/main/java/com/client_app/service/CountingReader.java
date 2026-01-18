package com.client_app.service;

import com.client_app.model.ImportStatus;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Reader;

@RequiredArgsConstructor
public class CountingReader extends Reader {
    private final Reader delegate;
    private final ImportStatus importStatus;

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int numRead = delegate.read(cbuf, off, len);
        if (numRead != -1) {
            for (int i = off; i < off + numRead; i++) {
                if (cbuf[i] == '\n') {
                    importStatus.setProcessedRows(importStatus.getProcessedRows() + 1);
                }
            }
        }
        return numRead;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
