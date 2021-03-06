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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.allure.extensions.HelloReply;
import com.github.allure.extensions.ProtoFormatter;
import com.google.protobuf.Message;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * This test should check that formatter just work.
 */
@Feature("Formatter")
public class ProtoFormatterSmokeTest {

    final HelloReply message = HelloReply.newBuilder().setMessage("test").build();
    final List<Message> messageList = Stream.of(
                    HelloReply.newBuilder().setMessage("test1").build(),
                    HelloReply.newBuilder().setMessage("test2").build(),
                    HelloReply.newBuilder().setMessage("test3").build())
            .collect(Collectors.toList());

    @Test
    @Description("Formatter should work with proto message.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Call formatter\n"
            + "3. Should not throw\n")
    public void formatterWorkingAsIsForSingleMessage() {
        Allure.addAttachment("message to format", message.toString());
        Allure.step("Formatter call as is", () -> Assertions.assertDoesNotThrow(() -> ProtoFormatter.format(message)));
    }

    @Test
    @Description("Formatter should work with proto message.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Set static FORMAT_PROTO_TO_JSON = true\n"
            + "3. Call formatter\n"
            + "4. Should not throw\n")
    public void formatterWorkingWithJsonFormattingForSingleMessage() {
        Allure.addAttachment("message to format", message.toString());
        Allure.step(
                "Formatter call with FORMAT_PROTO_TO_JSON = true",
                () -> assertDoesNotThrow(() -> {
                    ProtoFormatter.FORMAT_PROTO_TO_JSON = true;
                    ProtoFormatter.format(message);
                }));
    }

    @Test
    @Description("Formatter should work with proto message.\n" + "Steps:\n"
            + "1. Create proto message\n"
            + "2. Set static FORMAT_PROTO_TO_JSON = false\n"
            + "3. Call formatter\n"
            + "4. Should not throw\n")
    public void formatterWorkingWithoutJsonFormattingForSingleMessage() {
        Allure.addAttachment("message to format", message.toString());
        Allure.step(
                "Formatter call with FORMAT_PROTO_TO_JSON = false",
                () -> assertDoesNotThrow(() -> {
                    ProtoFormatter.FORMAT_PROTO_TO_JSON = false;
                    ProtoFormatter.format(message);
                }));
    }

    @Test
    @Description("Formatter should work with proto messages.\n" + "Steps:\n"
            + "1. Create proto messages\n"
            + "2. Call formatter\n"
            + "3. Should not throw\n")
    public void formatterWorkingAsIsForMessageList() {
        Allure.addAttachment("messages to format", messageList.toString());
        Allure.step(
                "Formatter call for list as is", () -> assertDoesNotThrow(() -> ProtoFormatter.format(messageList)));
    }

    @Test
    @Description("Formatter should work with proto messages.\n" + "Steps:\n"
            + "1. Create proto messages\n"
            + "2. Set static FORMAT_PROTO_TO_JSON = true\n"
            + "3. Call formatter\n"
            + "4. Should not throw\n")
    public void formatterWorkingWithJsonFormattingForMessageList() {
        Allure.addAttachment("messages to format", messageList.toString());
        Allure.step(
                "Formatter call for list with FORMAT_PROTO_TO_JSON = true",
                () -> assertDoesNotThrow(() -> {
                    ProtoFormatter.FORMAT_PROTO_TO_JSON = true;
                    ProtoFormatter.format(messageList);
                }));
    }

    @Test
    @Description("Formatter should work with proto messages.\n" + "Steps:\n"
            + "1. Create proto messages\n"
            + "2. Set static FORMAT_PROTO_TO_JSON = false\n"
            + "3. Call formatter\n"
            + "4. Should not throw\n")
    public void formatterWorkingWithoutJsonFormattingForMessageList() {
        Allure.addAttachment("messages to format", messageList.toString());
        Allure.step(
                "Formatter call for list with FORMAT_PROTO_TO_JSON = false",
                () -> assertDoesNotThrow(() -> {
                    ProtoFormatter.FORMAT_PROTO_TO_JSON = false;
                    ProtoFormatter.format(messageList);
                }));
    }
}
