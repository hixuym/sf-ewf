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

package io.sunflower.ewf.support;

import java.security.SecureRandom;
import java.util.Random;


public class SecretGenerator {

    /**
     * Generates a random String of length 64. This string is suitable as secret for your application
     * (key "application.secret" in conf/application.conf).
     *
     * @return A string that can be used as "application.secret".
     */
    public static String generateSecret() {

        return generateSecret(new SecureRandom());

    }

    /**
     * !!!! Only for testing purposes !!!!
     * <p>
     * Usually you want to use {@link SecretGenerator#generateSecret()}
     *
     * @param random the random generator to use. Usually new Random(), but for testing you can use a
     *               predefined seed.
     * @return A String suitable as random secret for eg signing a session.
     */
    protected static String generateSecret(Random random) {

        String charsetForSecret = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder stringBuilder = new StringBuilder(64);

        for (int i = 0; i < 64; i++) {

            int charToPoPickFromCharset = random.nextInt(charsetForSecret.length());
            stringBuilder.append(charsetForSecret.charAt(charToPoPickFromCharset));

        }

        return stringBuilder.toString();

    }


}
