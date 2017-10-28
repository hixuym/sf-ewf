/*
 * Copyright (C) 2017. the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sunflower.ewf.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import io.sunflower.ewf.uploads.FileItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;

/**
 * This {@link FileItem} type wraps a file received via a form parameter.
 *
 * @author Jens Fendler <jf@jensfendler.com>
 */
public class ParameterFileItem implements FileItem {

    private String filename;

    private Map<String, String> headers;

    private File file;

    public ParameterFileItem() {
        headers = Maps.newHashMap();
    }

    public ParameterFileItem(String filename, File file, Map<String, String> headers) {
        this.filename = filename;
        this.file = file;
        this.headers = headers;
    }

    /**
     * @see FileItem#getFileName()
     */
    @Override
    public String getFileName() {
        return filename;
    }

    /**
     * @see FileItem#getInputStream()
     */
    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * @see FileItem#getFile()
     */
    @Override
    public File getFile() {
        return file;
    }

    /**
     * @see FileItem#getContentType()
     */
    @Override
    public String getContentType() {
        return headers.get("Content-Type");
    }

    /**
     * @see FileItem#getHeaders()
     */
    @Override
    @JsonIgnore
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * @see FileItem#cleanup()
     */
    @Override
    public void cleanup() {
        // TODO check from where cleanup() is called and consider removing the
        // file.
    }

    @Override
    public String toString() {
        return "ParameterFileItem [filename=" + filename + ", file=" + file.getAbsolutePath() + "]";
    }
}
