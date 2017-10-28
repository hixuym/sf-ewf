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
package io.sunflower.ewf.assets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author michael
 */
public class AssetsResourceHelper {

    private final static Logger logger = LoggerFactory.getLogger(AssetsResourceHelper.class);

    /**
     * If we get - for whatever reason - a relative URL like assets/../conf/application.conf we expand
     * that to the "real" path. In the above case conf/application.conf.
     * <p>
     * You should then add the assets prefix.
     * <p>
     * Otherwise someone can create an attack and read all resources of our app. If we expand and
     * normalize the incoming path this is no longer possible.
     *
     * @param fileName             A potential "fileName"
     * @param enforceUnixSeparator If true it will force the usage of the unix separator '/' If false
     *                             it will use the separator of the underlying system. usually '/' in case of unix and '\' in case
     *                             of windows.
     * @return A normalized fileName.
     */
    public String normalizePathWithoutLeadingSlash(String fileName, boolean enforceUnixSeparator) {
        if (StringUtils.isEmpty(fileName)) {
            return fileName;
        }

        Path p = Paths.get(fileName);
        String fileNameNormalized = p.normalize().toString().trim();

        return StringUtils.removeStart(fileNameNormalized, "/");
    }

    /**
     * Check the URL is a directory. With war style deployment, AssetsResource exposes the file list
     * of assets directories. For example, a request to http://localhost:8080/assets/css/ displays the
     * file list of css directory. So this method checks the URL is a directory.
     *
     * @param url A URL of assets
     * @return true if the URL is a directory
     */
    public boolean isDirectoryURL(URL url) {
        try {
            return url.getProtocol().equals("file") && new File(url.toURI()).isDirectory();
        } catch (URISyntaxException e) {
            logger.error("Could not URL convert to URI", e);
        }
        return false;
    }

}
