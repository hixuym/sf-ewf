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

import ch.qos.logback.core.Appender;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.i18n.Messages;
import io.sunflower.ewf.support.Constants;
import io.sunflower.ewf.validation.ConstraintViolation;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertThat;

/**
 * @author ra
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateEngineFreemarkerI18nMethodTest {

    @Mock
    Context context;

    @Mock
    Result result;

    @Mock
    Messages messages;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Captor
    public ArgumentCaptor<List<String>> listCaptor;

    TemplateEngineFreemarkerI18nMethod templateEngineFreemarkerI18nMethod;

    Appender mockAppender;

    @Before
    public void before() {

        templateEngineFreemarkerI18nMethod
                = Mockito.spy(new TemplateEngineFreemarkerI18nMethod(
                messages,
                context,
                result));

        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        mockAppender = Mockito.mock(Appender.class);
//        Mockito.when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);

    }


    @Test
    public void testThatNoKeyYieldsException() throws Exception {

        List args = Collections.EMPTY_LIST;

        thrown.expect(TemplateModelException.class);

        templateEngineFreemarkerI18nMethod.exec(args);

    }

    @Test
    public void testThatSingleKeyWithValueWorks() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        Mockito.when(
                messages.get("my.message.key", context, resultOptional))
                .thenReturn(Optional.of("This simulates the translated message!"));

        List args = new ArrayList();
        args.add(new SimpleScalar("my.message.key"));

        TemplateModel returnValue
                = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(),
                CoreMatchers.equalTo("This simulates the translated message!"));

        Mockito.verify(mockAppender, Mockito.never()).doAppend(Matchers.anyObject());

    }

    @Test
    public void testThatSingleKeyWithMissingValueReturnsDefaultKey() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        Mockito.when(
                messages.get("my.message.key", context, resultOptional))
                .thenReturn(Optional.<String>empty());

        List args = new ArrayList();
        args.add(new SimpleScalar("my.message.key"));

        TemplateModel returnValue
                = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(), CoreMatchers.equalTo("my.message.key"));

        // There must have been logged something because we did not find
        // the value for the key...
        Mockito.verify(mockAppender).doAppend(Matchers.anyObject());
    }

    @Test
    public void testThatKeyWithPlaceholderWorks() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        List args = new ArrayList();
        args.add(new SimpleScalar("my.message.key"));
        args.add(new SimpleScalar("1000"));

        Mockito.when(
                messages.get(
                        Matchers.eq("my.message.key"),
                        Matchers.eq(context),
                        Matchers.eq(resultOptional),
                        Matchers.any(Object.class)))
                .thenReturn(Optional.of("This simulates the translated message number 1000!"));

        TemplateModel returnValue
                = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(),
                CoreMatchers.equalTo("This simulates the translated message number 1000!"));

        Mockito.verify(mockAppender, Mockito.never()).doAppend(Matchers.anyObject());
    }

    @Test
    public void testThatKeyWithPlaceholderReturnsDefaultKeyWhenKeyCannotBeFound() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        List args = new ArrayList();
        args.add(new SimpleScalar("my.message.key"));
        args.add(new SimpleScalar("1000"));

        Mockito.when(
                messages.get(
                        Matchers.eq("my.message.key"),
                        Matchers.eq(context),
                        Matchers.eq(resultOptional),
                        Matchers.any(Object.class)))
                .thenReturn(Optional.<String>empty());

        TemplateModel returnValue
                = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(), CoreMatchers.equalTo("my.message.key"));

        // There must have been logged something because we did not find
        // the value for the key...
        Mockito.verify(mockAppender).doAppend(Matchers.anyObject());

    }

    @Test
    public void testThatConstraintViolationWorks() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        Mockito.when(
                messages.get(Constants.INT_KEY, context, resultOptional))
                .thenReturn(Optional.of("This simulates the translated message!"));

        ConstraintViolation violation = new ConstraintViolation(Constants.INT_KEY, "theField",
                Constants.INT_MESSAGE);

        List args = new ArrayList();
        args.add(new StringModel(violation, new BeansWrapper()));

        TemplateModel returnValue = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(),
                CoreMatchers.equalTo("This simulates the translated message!"));

        Mockito.verify(mockAppender, Mockito.never()).doAppend(Matchers.anyObject());

    }

    @Test
    public void testThatConstraintViolationWorksWithDefault() throws Exception {

        Optional<Result> resultOptional = Optional.of(result);

        Mockito.when(
                messages.get(Constants.INT_KEY, context, resultOptional))
                .thenReturn(Optional.empty());

        ConstraintViolation violation = new ConstraintViolation(Constants.INT_KEY, "theField",
                Constants.INT_MESSAGE);

        List args = new ArrayList();
        args.add(new StringModel(violation, new BeansWrapper()));

        TemplateModel returnValue = templateEngineFreemarkerI18nMethod.exec(args);

        assertThat(((SimpleScalar) returnValue).getAsString(),
                CoreMatchers.equalTo("theField must be an integer"));

        Mockito.verify(mockAppender, Mockito.never()).doAppend(Matchers.anyObject());

    }
}
