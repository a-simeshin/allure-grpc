package com.github.allure.extensions;

import com.github.underscore.U;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to pretty formatting proto messages to json or plaintext if FORMAT_PROTO_TO_JSON is false.
 */
public class ProtoFormatter {

    public static boolean FORMAT_PROTO_TO_JSON = true;

    /**
     * Converting any proto messages to Json array format with field names
     *
     * @param responses any proto messages counted from 0 to many
     * @return formatted string json or plaintext
     */
    public static String toJson(final List<Message> responses) {
        if (responses.size() == 0)
            return "";

        if (FORMAT_PROTO_TO_JSON) {
            if (responses.size() == 1) {
                try {
                    final String json = toJson(responses.get(0));
                    return U.formatJson(json);
                } catch (InvalidProtocolBufferException ignored) {
                    return responses.get(0).toString();
                }
            } else {
                return U.formatJson(
                        "[" + responses.stream().map(message -> {
                            try {
                                return toJson(message);
                            } catch (InvalidProtocolBufferException ignored) {
                                return "unable_to_format: \"" + message.toString() + "\"";
                            }
                        }).collect(Collectors.joining(",")) + "]"
                );
            }
        } else {
            if (responses.size() == 1) {
                return responses.get(0).toString();
            } else {
                final StringBuilder sb = new StringBuilder();
                responses.forEach(message -> sb.append(message.toString()).append("\n\n"));
                return sb.toString();
            }
        }
    }

    /**
     * Converting one proto message to Json format with field names
     *
     * @param protoMessage proto message
     * @return String in json format
     * @throws InvalidProtocolBufferException while formatting is broken somehow
     */
    public static String toJson(final Message protoMessage) throws InvalidProtocolBufferException {
        return JsonFormat.printer()
                .preservingProtoFieldNames()
                .print(protoMessage);
    }

}
