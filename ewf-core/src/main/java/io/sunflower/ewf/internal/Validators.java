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

import com.google.inject.Inject;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.i18n.Lang;
import io.sunflower.ewf.validation.*;

import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;
import java.io.Serializable;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Built in validators.
 *
 * @author James Roper
 * @author Thibault Meyer
 */
public class Validators {

    private static String fieldKey(String fieldName, String configuredFieldKey) {
        if (configuredFieldKey.length() > 0) {
            return configuredFieldKey;
        } else {
            return fieldName;
        }
    }

    /**
     * A basic Message interpolator.
     *
     * @author Thibault Meyer
     */
    private static class EwfContextMsgInterpolator
            implements MessageInterpolator.Context, Serializable {

        private final Object value;
        private final ConstraintDescriptor<?> descriptor;

        /**
         * Create message interpolator context.
         *
         * @param value      value being validated
         * @param descriptor Constraint being validated
         */
        public EwfContextMsgInterpolator(Object value, ConstraintDescriptor<?> descriptor) {
            this.value = value;
            this.descriptor = descriptor;
        }

        /**
         * Get the constraint descriptor.
         *
         * @return The constraint descriptor
         * @see javax.validation.metadata.ConstraintDescriptor
         */
        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return this.descriptor;
        }

        /**
         * Get the validated value.
         *
         * @return The value
         */
        @Override
        public Object getValidatedValue() {
            return this.value;
        }

        @Override
        public <T> T unwrap(Class<T> type) {
            return null;
        }
    }

    @Singleton
    public static class BeanValidator implements Validator<Object> {

        private final Lang requestLanguage;
        private final javax.validation.Validator validator;
        private final ValidatorFactory validatorFactory;

        @Inject
        public BeanValidator(Lang requestLanguage,
                             ValidatorFactory validatorFactory,
                             javax.validation.Validator validator) {
            this.requestLanguage = requestLanguage;
            this.validator = validator;
            this.validatorFactory = validatorFactory;
        }

        /**
         * Validate the given value.
         *
         * @param value   The value, may be null
         * @param field   The name of the field being validated, if applicable
         * @param context The RequestHandler request context
         */
        @Override
        public void validate(Object value, String field, Context context) {
            if (value != null) {
                final Set<javax.validation.ConstraintViolation<Object>> violations = validator.validate(value);
                final Locale localeToUse = this.requestLanguage.getLocaleFromStringOrDefault(
                        this.requestLanguage.getLanguage(context, Optional.empty()));

                final Validation validation = context.getValidation();

                for (final javax.validation.ConstraintViolation<Object> violation : violations) {
                    final String violationMessage = validatorFactory.getMessageInterpolator().interpolate(
                            violation.getMessageTemplate(),
                            new EwfContextMsgInterpolator(value, violation.getConstraintDescriptor()), localeToUse);
                    final String messageKey = violation.getMessageTemplate().replaceAll("[{}]", "");
                    final ConstraintViolation constraintViolation = new ConstraintViolation(
                            messageKey, violation.getPropertyPath().toString(), violationMessage, violation.getInvalidValue());
                    validation.addViolation(constraintViolation);
                }
            }
        }

        @Override
        public Class<Object> getValidatedType() {
            return Object.class;
        }
    }

    public static class LengthValidator implements Validator<String> {

        private final Size size;

        public LengthValidator(Size size) {
            this.size = size;
        }

        /**
         * Validate the given value
         *
         * @param value   The value, may be null
         * @param field   The name of the field being validated, if applicable
         * @param context The RequestHandler request context
         */
        @Override
        public void validate(String value, String field, Context context) {
            if (value != null) {
                if (this.size.max() != -1 && value.length() > this.size.max()) {
                    context.getValidation().addViolation(
                            new ConstraintViolation(this.size.maxKey(),
                                    fieldKey(field, this.size.fieldKey()),
                                    this.size.maxMessage(), this.size.max(), value));
                } else if (this.size.min() != -1 && value.length() < this.size.min()) {
                    context.getValidation().addViolation(
                            new ConstraintViolation(this.size.minKey(),
                                    fieldKey(field, this.size.fieldKey()),
                                    this.size.minMessage(), this.size.min(), value));
                }
            }
        }

        @Override
        public Class<String> getValidatedType() {
            return String.class;
        }
    }

    public static class MatchesValidator implements Validator<String> {

        private final Matches matches;
        private final Pattern pattern;

        public MatchesValidator(Matches matches) {
            this.matches = matches;
            this.pattern = Pattern.compile(matches.regexp());
        }

        /**
         * Validate the given value
         *
         * @param value   The value, may be null
         * @param field   The name of the field being validated, if applicable
         * @param context The RequestHandler request context
         */
        @Override
        public void validate(String value, String field, Context context) {
            if (value != null) {
                if (!this.pattern.matcher(value).matches()) {
                    context.getValidation().addViolation(
                            new ConstraintViolation(
                                    this.matches.key(),
                                    fieldKey(field, this.matches.fieldKey()),
                                    this.matches.message(),
                                    this.matches.regexp(), value));
                }
            }
        }

        @Override
        public Class<String> getValidatedType() {
            return String.class;
        }
    }

    public static class NumberValidator implements Validator<Number> {

        private final NumberValue number;

        public NumberValidator(NumberValue number) {
            this.number = number;
        }

        /**
         * Validate the given value
         *
         * @param value   The value, may be null
         * @param field   The name of the field being validated, if applicable
         * @param context The RequestHandler request context
         */
        @Override
        public void validate(Number value, String field, Context context) {
            if (value != null) {
                if (this.number.max() != Double.MAX_VALUE
                        && value.doubleValue() > this.number.max()) {
                    context.getValidation().addViolation(
                            new ConstraintViolation(this.number.maxKey(),
                                    fieldKey(field, this.number.fieldKey()),
                                    this.number.maxMessage(), this.number.max(), value));
                } else if (this.number.min() != -1
                        && value.doubleValue() < this.number.min()) {
                    context.getValidation().addViolation(
                            new ConstraintViolation(this.number.minKey(),
                                    fieldKey(field, this.number.fieldKey()),
                                    this.number.minMessage(), this.number.min(), value));
                }
            }
        }

        @Override
        public Class<Number> getValidatedType() {
            return Number.class;
        }
    }
}
