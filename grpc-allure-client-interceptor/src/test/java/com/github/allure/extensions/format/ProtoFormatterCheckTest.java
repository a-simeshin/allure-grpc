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
package com.github.allure.extensions.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.allure.extensions.HelloReply;
import com.github.allure.extensions.ProtoFormatter;
import com.google.protobuf.Message;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * This test should check formatter`s work results with single and any proto messages with formatting to Json and Plaintext.
 */
@Feature("Formatter")
public class ProtoFormatterCheckTest {

    final HelloReply message = HelloReply.newBuilder().setMessage("test").build();
    final String expectingFormattedMessageResult = "{\n  \"message\": \"test\"\n}";
    final String expectingNonFormattedMessageResult = "message: \"test\"\n";

    final List<Message> messageList = Stream.of(
                    HelloReply.newBuilder().setMessage("test1").build(),
                    HelloReply.newBuilder().setMessage("test2").build(),
                    HelloReply.newBuilder().setMessage("test3").build())
            .collect(Collectors.toList());
    final String expectingFormattedMessageList = "[\n" + "  {\n"
            + "    \"message\": \"test1\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"message\": \"test2\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"message\": \"test3\"\n"
            + "  }\n"
            + "]";
    final String expectingNonFormattedMessageList =
            "message: \"test1\"\n" + "message: \"test2\"\n" + "message: \"test3\"\n";

    @Test
    @Description("Test checking, that single proto message could be correctly formatted to json.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Call formatter\n"
            + "3. Match with expected\n")
    public void correctlyFormattingToJsonSingleMessage() {
        Allure.addAttachment("message to format", message.toString());
        Allure.step("Formatter should form correctly format single message to Json", () -> {
            ProtoFormatter.FORMAT_PROTO_TO_JSON = true;
            final String result = ProtoFormatter.format(message);
            Allure.addAttachment("result", result);
            assertEquals(expectingFormattedMessageResult, result);
        });
    }

    @Test
    @Description("Single proto message could be correctly formatted to Plaintext.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Call formatter\n"
            + "3. Match with expected\n")
    public void correctlyFormattingToPlaintextSingleMessage() {
        Allure.addAttachment("message to format", message.toString());
        Allure.step("Formatter should form correctly format single message to Plaintext", () -> {
            ProtoFormatter.FORMAT_PROTO_TO_JSON = false;
            final String result = ProtoFormatter.format(message);
            Allure.addAttachment("result", result);
            assertEquals(expectingNonFormattedMessageResult, result);
        });
    }

    @Test
    @Description("Any proto messages could be correctly formatted to Json.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Call formatter\n"
            + "3. Match with expected\n")
    public void correctlyFormattingToJsonMessageList() {
        Allure.addAttachment("messages to format", messageList.toString());
        Allure.step("Formatter should form correctly format MessageList to Json", () -> {
            ProtoFormatter.FORMAT_PROTO_TO_JSON = true;
            final String result = ProtoFormatter.format(messageList);
            Allure.addAttachment("result", result);
            assertEquals(expectingFormattedMessageList, result);
        });
    }

    @Test
    @Description("Any proto messages could be correctly formatted to Plaintext.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Call formatter\n"
            + "3. Match with expected\n")
    public void correctlyFormattingToPlaintextMessageList() {
        Allure.addAttachment("messages to format", messageList.toString());
        Allure.step("Formatter should form correctly format MessageList to Plaintext", () -> {
            ProtoFormatter.FORMAT_PROTO_TO_JSON = false;
            final String result = ProtoFormatter.format(messageList);
            Allure.addAttachment("result", result);
            assertEquals(expectingNonFormattedMessageList, result);
        });
    }
}
