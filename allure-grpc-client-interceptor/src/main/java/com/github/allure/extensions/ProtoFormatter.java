/*
 * MIT License
 *
 * Copyright (c) 2022 a-simeshin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.allure.extensions;

import com.github.underscore.U;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to pretty formatting proto messages to json or plaintext if FORMAT_PROTO_TO_JSON is false.
 */
@SuppressWarnings("MS_SHOULD_BE_FINAL")
public class ProtoFormatter {

    public static boolean FORMAT_PROTO_TO_JSON = true;

    /**
     * Converting any proto messages to Json array format with field names
     *
     * @param responses any proto messages counted from 0 to many
     * @return formatted string json or plaintext
     */
    public static String format(final List<Message> responses) {
        if (responses.size() == 0) return "";

        if (FORMAT_PROTO_TO_JSON) {
            if (responses.size() == 1) {
                try {
                    final String json = format(responses.get(0));
                    return U.formatJson(json);
                } catch (InvalidProtocolBufferException ignored) {
                    return responses.get(0).toString();
                }
            } else {
                return U.formatJson("["
                        + responses.stream()
                                .map(message -> {
                                    try {
                                        return format(message);
                                    } catch (InvalidProtocolBufferException ignored) {
                                        return "unable_to_format: \"" + message.toString() + "\"";
                                    }
                                })
                                .collect(Collectors.joining(","))
                        + "]");
            }
        } else {
            if (responses.size() == 1) {
                return responses.get(0).toString();
            } else {
                final StringBuilder sb = new StringBuilder();
                responses.forEach(message -> sb.append(message.toString()));
                return sb.toString();
            }
        }
    }

    /**
     * Converting one proto message to Json format with field names
     *
     * @param protoMessage proto message
     * @return String in json or plaintext format
     * @throws InvalidProtocolBufferException while formatting is broken somehow
     */
    public static String format(final Message protoMessage) throws InvalidProtocolBufferException {
        if (FORMAT_PROTO_TO_JSON) {
            return JsonFormat.printer().preservingProtoFieldNames().print(protoMessage);
        } else {
            return TextFormat.printer().printToString(protoMessage);
        }
    }
}
