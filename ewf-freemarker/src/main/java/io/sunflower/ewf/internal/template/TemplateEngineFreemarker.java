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

package io.sunflower.ewf.internal.template;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.ParseException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.errors.RenderingException;
import io.sunflower.ewf.i18n.Lang;
import io.sunflower.ewf.i18n.Messages;
import io.sunflower.ewf.internal.template.directives.TemplateEngineFreemarkerAuthenticityFormDirective;
import io.sunflower.ewf.internal.template.directives.TemplateEngineFreemarkerAuthenticityTokenDirective;
import io.sunflower.ewf.spi.TemplateEngine;
import io.sunflower.ewf.support.Constants;
import io.sunflower.ewf.support.ResponseStreams;
import io.sunflower.ewf.support.Settings;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * @author michael
 */
@Singleton
public class TemplateEngineFreemarker implements TemplateEngine {

    private final static String FILE_SUFFIX = ".ftl.html";

    private final Version INCOMPATIBLE_IMPROVEMENTS_VERSION = new Version(2, 3, 22);

    private final Configuration cfg;

    private final Messages messages;

    private final Lang lang;

    private final TemplateEngineHelper templateEngineHelper;

    private final Logger logger;

    private final TemplateEngineFreemarkerReverseRouteMethod templateEngineFreemarkerReverseRouteMethod;

    private final TemplateEngineFreemarkerAssetsAtMethod templateEngineFreemarkerAssetsAtMethod;

    private final TemplateEngineFreemarkerWebJarsAtMethod templateEngineFreemarkerWebJarsAtMethod;

    private final Settings configuration;

    @Inject
    public TemplateEngineFreemarker(Messages messages,
                                    Lang lang,
                                    Logger logger,
                                    Settings configuration,
                                    TemplateEngineHelper templateEngineHelper,
                                    TemplateEngineFreemarkerReverseRouteMethod templateEngineFreemarkerReverseRouteMethod,
                                    TemplateEngineFreemarkerAssetsAtMethod templateEngineFreemarkerAssetsAtMethod,
                                    TemplateEngineFreemarkerWebJarsAtMethod templateEngineFreemarkerWebJarsAtMethod)
            throws Exception {
        this.messages = messages;
        this.lang = lang;
        this.logger = logger;
        this.configuration = configuration;
        this.templateEngineHelper = templateEngineHelper;
        this.templateEngineFreemarkerReverseRouteMethod = templateEngineFreemarkerReverseRouteMethod;
        this.templateEngineFreemarkerAssetsAtMethod = templateEngineFreemarkerAssetsAtMethod;
        this.templateEngineFreemarkerWebJarsAtMethod = templateEngineFreemarkerWebJarsAtMethod;

        cfg = new Configuration(INCOMPATIBLE_IMPROVEMENTS_VERSION);

        // Set your preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding(Constants.UTF_8);

        // Set the charset of the output. This is actually just a hint, that
        // templates may require for URL encoding and for generating META element
        // that uses http-equiv="Content-type".
        cfg.setOutputEncoding(Constants.UTF_8);

        // Ninja does the localization itself - lookup is not needed.
        cfg.setLocalizedLookup(false);

        ///////////////////////////////////////////////////////////////////////
        // 1) In dev we load templates from src/java/main first, then from the
        //    classpath.
        //    Therefore Freemarker can handle reloading of changed templates without
        //    the need to restart the server (e.g automatic reload of jetty:run)
        // 2) In test and prod we never refresh templates and load them
        //    from the classpath
        ///////////////////////////////////////////////////////////////////////
        String srcDir
                = System.getProperty("user.dir")
                + File.separator
                + "src"
                + File.separator
                + "main"
                + File.separator
                + "java";

        if (configuration.isDev()
                && new File(srcDir).exists()) {

            try {
                // the src dir of user's project.
                FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(new File(srcDir));
                // then ftl.html files from the classpath (eg. from inherited modules
                // or the ninja core module)
                ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(this.getClass(), "/");

                TemplateLoader[] templateLoader = new TemplateLoader[]{
                        fileTemplateLoader,
                        classTemplateLoader};

                MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(templateLoader);

                cfg.setTemplateLoader(multiTemplateLoader);

            } catch (IOException e) {
                logger.error("Error Loading Freemarker Template " + srcDir, e);
            }

            // check for updates each second
            cfg.setTemplateUpdateDelayMilliseconds(1000);

        } else {
            // load templates from classpath
            cfg.setClassForTemplateLoading(this.getClass(), "/");

            // never update the templates in production or while testing...
            cfg.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);

            // Hold 20 templates as strong references as recommended by:
            // http://freemarker.sourceforge.net/docs/pgui_config_templateloading.html
            cfg.setCacheStorage(new freemarker.cache.MruCacheStorage(20, Integer.MAX_VALUE));

        }

        // we are going to enable html escaping by default using this template
        // loader:
        cfg.setTemplateLoader(new TemplateEngineFreemarkerEscapedLoader(cfg.getTemplateLoader()));

        // We also do not want Freemarker to chose a platform dependent
        // number formatting. Eg "1000" could be printed out by FTL as "1,000"
        // on some platforms. This is not "least astonishemnt". It will also
        // break stuff really badly sometimes.
        // See also: http://freemarker.sourceforge.net/docs/app_faq.html#faq_number_grouping
        cfg.setNumberFormat("0.######");  // now it will print 1000000

        cfg.setObjectWrapper(createBeansWrapperWithExposedFields());

    }

    @Override
    public void invoke(Context context, Result result) {

        Object object = result.getRenderable();

        Map<String, Object> map;
        // if the object is null we simply render an empty map...
        if (object == null) {
            map = Maps.newHashMap();

        } else if (object instanceof Map) {
            map = (Map) object;

        } else {
            // We are getting an arbitrary Object and put that into
            // the root of freemarker

            // If you are rendering something like Results.ok().render(new MyObject())
            // Assume MyObject has a public String name field.
            // You can then access the fields in the template like that:
            // ${myObject.publicField}

            String realClassNameLowerCamelCase = CaseFormat.UPPER_CAMEL.to(
                    CaseFormat.LOWER_CAMEL, object.getClass().getSimpleName());

            map = Maps.newHashMap();
            map.put(realClassNameLowerCamelCase, object);

        }

        // set language from framework. You can access
        // it in the templates as ${lang}
        Optional<String> language = lang.getLanguage(context, Optional.of(result));
        language.ifPresent(s -> map.put("lang", s));

        // put all entries of the session cookie to the map.
        // You can access the values by their key in the cookie
        if (context.getSession() != null && !context.getSession().isEmpty()) {
            map.put("session", context.getSession().getData());
        }

        map.put("contextPath", context.getContextPath());
        map.put("validation", context.getValidation());

        //////////////////////////////////////////////////////////////////////
        // A method that renders i18n messages and can also render messages with
        // placeholders directly in your template:
        // E.g.: ${i18n("mykey", myPlaceholderVariable)}
        //////////////////////////////////////////////////////////////////////
        map.put("i18n", new TemplateEngineFreemarkerI18nMethod(messages, context, result));

        Optional<String> requestLang = lang.getLanguage(context, Optional.of(result));
        Locale locale = lang.getLocaleFromStringOrDefault(requestLang);
        map.put("prettyTime", new TemplateEngineFreemarkerPrettyTimeMethod(locale));

        map.put("reverseRoute", templateEngineFreemarkerReverseRouteMethod);
        map.put("assetsAt", templateEngineFreemarkerAssetsAtMethod);
        map.put("webJarsAt", templateEngineFreemarkerWebJarsAtMethod);

        map.put("authenticityToken", new TemplateEngineFreemarkerAuthenticityTokenDirective(context));
        map.put("authenticityForm", new TemplateEngineFreemarkerAuthenticityFormDirective(context));

        ///////////////////////////////////////////////////////////////////////
        // Convenience method to translate possible flash scope keys.
        // !!! If you want to set messages with placeholders please do that
        // !!! in your controller. We only can set simple messages.
        // Eg. A message like "errorMessage=my name is: {0}" => translate in controller and pass directly.
        //     A message like " errorMessage=An error occurred" => use that as errorMessage.
        //
        // get keys via ${flash.KEYNAME}
        //////////////////////////////////////////////////////////////////////
        if (context.getFlashScope() != null) {
            Map<String, String> translatedFlashCookieMap = Maps.newHashMap();
            for (Entry<String, String> entry : context.getFlashScope().getCurrentFlashCookieData()
                    .entrySet()) {

                String messageValue;

                Optional<String> messageValueOptional = messages
                        .get(entry.getValue(), context, Optional.of(result));

                messageValue = messageValueOptional.orElseGet(entry::getValue);
                // new way
                translatedFlashCookieMap.put(entry.getKey(), messageValue);
            }

            // now we can retrieve flash cookie messages via ${flash.MESSAGE_KEY}
            map.put("flash", translatedFlashCookieMap);
        }

        // Specify the data source where the template files come from.
        // Here I set a file directory for it:
        String templateName = templateEngineHelper
                .getTemplateForResult(context.getRoute(), result, FILE_SUFFIX);

        Template freemarkerTemplate;

        try {

            freemarkerTemplate = cfg.getTemplate(templateName);

            // Fully buffer the response so in the case of a template error we can
            // return the applications 500 error message. Without fully buffering
            // we can't guarantee we haven't flushed part of the response to the
            // client.
            StringWriter buffer = new StringWriter(64 * 1024);
            freemarkerTemplate.process(map, buffer);

            ResponseStreams responseStreams = context.finalizeHeaders(result);
            try (Writer writer = responseStreams.getWriter()) {
                writer.write(buffer.toString());
            }
        } catch (Exception cause) {

            // delegate rendering exception handling back to Ninja
            throwRenderingException(context, result, cause, templateName);

        }
    }

    public void throwRenderingException(
            Context context,
            Result result,
            Exception cause,
            String knownTemplateSourcePath) {

        // parse method above may throw an IOException whose cause is really
        // a more useful ParseException
        if (cause instanceof IOException
                && cause.getCause() != null
                && cause.getCause() instanceof ParseException) {
            cause = (ParseException) cause.getCause();
        }

        if (cause instanceof TemplateNotFoundException) {

            // inner cause will be better to display
            throw new RenderingException(cause.getMessage(), cause, result,
                    "FreeMarker template not found", knownTemplateSourcePath, -1);

        } else if (cause instanceof TemplateException) {

            TemplateException te = (TemplateException) cause;
            String templateSourcePath = te.getTemplateSourceName();
            if (templateSourcePath == null) {
                templateSourcePath = knownTemplateSourcePath;
            }

            throw new RenderingException(cause.getMessage(), cause, result, "FreeMarker render exception",
                    templateSourcePath, te.getLineNumber());

        } else if (cause instanceof ParseException) {

            ParseException pe = (ParseException) cause;

            String templateSourcePath = pe.getTemplateName();
            if (templateSourcePath == null) {
                templateSourcePath = knownTemplateSourcePath;
            }

            throw new RenderingException(cause.getMessage(), cause, result, "FreeMarker parser exception",
                    templateSourcePath, pe.getLineNumber());

        }

        // fallback to throwing generic rendering exception
        throw new RenderingException(cause.getMessage(), cause, result, knownTemplateSourcePath, -1);

    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getSuffixOfTemplatingEngine() {
        return FILE_SUFFIX;
    }

    /**
     * Allows to modify the FreeMarker configuration. According to the FreeMarker documentation, the
     * configuration will be thread-safe once all settings have been set via a safe publication
     * technique. Therefore, consider modifying this configuration only within the configure() method
     * of your application Module singleton.
     *
     * @return the freemarker configuration object
     */
    public Configuration getConfiguration() {
        return cfg;
    }

    private BeansWrapper createBeansWrapperWithExposedFields() {
        DefaultObjectWrapperBuilder defaultObjectWrapperBuilder
                = new DefaultObjectWrapperBuilder(INCOMPATIBLE_IMPROVEMENTS_VERSION);
        defaultObjectWrapperBuilder.setExposeFields(true);
        return defaultObjectWrapperBuilder.build();
    }

}
