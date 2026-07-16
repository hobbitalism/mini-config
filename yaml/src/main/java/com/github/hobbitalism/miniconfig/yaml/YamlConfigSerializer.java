package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.ConfigSerializer;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * {@link ConfigSerializer} that writes a {@link YamlConfigSection} back to
 * YAML text using SnakeYAML.
 *
 * <p>Output uses block style with an indent of 2 spaces, which is the
 * conventional format for hand-edited config files.
 */
public class YamlConfigSerializer implements ConfigSerializer<YamlConfigSection> {

    private static final Pattern NEEDS_QUOTING = Pattern.compile(
            "[:\\[\\]{}&#*!|>'\"%@`]|^[\\-? ]|^[ \\t]|^[0-9]|\\s$");

    private final DumperOptions options;

    /**
     * Constructs a serializer with default options (block style, indent 2).
     */
    public YamlConfigSerializer() {
        options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
    }

    /**
     * Constructs a serializer with custom {@link DumperOptions}.
     *
     * @param options the SnakeYAML dumper options to use
     */
    public YamlConfigSerializer(DumperOptions options) {
        this.options = options;
    }

    @Override
    public void save(YamlConfigSection section, Writer writer) throws IOException {
        Map<String, String> comments = section.getComments();
        writeYaml(writer, section.getRawData(), comments, "", 0, true);
    }

    @SuppressWarnings("unchecked")
    private void writeYaml(Writer writer, Object value,
                            Map<String, String> comments, String prefix,
                            int indent, boolean isRoot) throws IOException {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
                String comment = comments != null ? comments.get(path) : null;
                if (comment != null) {
                    for (String line : comment.split("\n")) {
                        writeIndent(writer, indent);
                        writer.write("# ");
                        writer.write(line);
                        writer.write("\n");
                    }
                }
                writeIndent(writer, indent);
                writer.write(entry.getKey());
                writer.write(":");
                Object val = entry.getValue();
                if (val instanceof Map) {
                    writer.write("\n");
                    writeYaml(writer, val, comments, path, indent + 1, false);
                } else if (val instanceof List) {
                    writer.write("\n");
                    writeYaml(writer, val, null, null, indent + 1, false);
                } else {
                    writer.write(' ');
                    writeScalar(writer, val);
                    writer.write("\n");
                }
            }
        } else if (value instanceof List) {
            for (Object item : (List<?>) value) {
                writeIndent(writer, indent);
                writer.write("- ");
                if (item instanceof Map) {
                    writer.write("\n");
                    writeYaml(writer, item, null, null, indent + 1, false);
                } else if (item instanceof List) {
                    writer.write("\n");
                    writeYaml(writer, item, null, null, indent + 1, false);
                } else {
                    writeScalar(writer, item);
                    writer.write("\n");
                }
            }
        }
    }

    private void writeIndent(Writer writer, int indent) throws IOException {
        for (int i = 0; i < indent * options.getIndent(); i++) {
            writer.write(' ');
        }
    }

    private void writeScalar(Writer writer, Object value) throws IOException {
        if (value == null) {
            writer.write('~');
        } else if (value instanceof Boolean || value instanceof Number) {
            writer.write(value.toString());
        } else {
            String str = value.toString();
            if (str.isEmpty()) {
                writer.write("\"\"");
            } else if (NEEDS_QUOTING.matcher(str).find()) {
                writer.write('"');
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (c == '"' || c == '\\') {
                        writer.write('\\');
                    }
                    writer.write(c);
                }
                writer.write('"');
            } else {
                writer.write(str);
            }
        }
    }
}
