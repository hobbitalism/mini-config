package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.ConfigSerializer;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class YamlConfigSerializer implements ConfigSerializer<YamlConfigSection> {

    private static final Pattern NEEDS_QUOTING = Pattern.compile(
            "[:\\[\\]{}&#*!|>'\"%@`]|^[\\-? ]|^[ \\t]|^[0-9]|\\s$");

    private static final String INDENT = "  ";
    private static final String[] INDENTS = new String[16];

    static {
        for (int i = 0; i < INDENTS.length; i++) {
            INDENTS[i] = INDENT.repeat(i);
        }
    }

    private final DumperOptions options;

    public YamlConfigSerializer() {
        options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
    }

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
                    writeComment(writer, comment, indent);
                }
                writer.write(indent(indent));
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
                writer.write(indent(indent));
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

    private void writeComment(Writer writer, String comment, int indent) throws IOException {
        String base = indent(indent) + "# ";
        int start = 0;
        int end;
        while ((end = comment.indexOf('\n', start)) != -1) {
            writer.write(base);
            writer.write(comment, start, end - start);
            writer.write("\n");
            start = end + 1;
        }
        writer.write(base);
        writer.write(comment, start, comment.length() - start);
        writer.write("\n");
    }

    private static String indent(int depth) {
        return depth < INDENTS.length ? INDENTS[depth] : INDENT.repeat(depth);
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
