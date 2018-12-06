/*
 * Copyright (C) 2018 Lukas Mansour
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.articdive.enum_to_yaml.yaml.file;

import de.articdive.enum_to_yaml.yaml.EnumConfigurationConstructor;
import de.articdive.enum_to_yaml.yaml.EnumConfigurationDumperOptions;
import de.articdive.enum_to_yaml.yaml.EnumConfigurationRepresenter;
import de.articdive.enum_to_yaml.yaml.file.interfaces.Configuration;
import de.articdive.enum_to_yaml.yaml.file.interfaces.ConfigurationSection;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a base class for all File based implementations of {@link Configuration}
 */
public class YAMLConfiguration extends MemorySection implements Configuration {
    private final EnumConfigurationDumperOptions dumperOptions;
    private final Yaml yaml;
    private final File file;
    private HashMap<String, String> comments = new HashMap<>();

    public YAMLConfiguration(File file, EnumConfigurationDumperOptions dumperOptions) {
        super();
        this.file = file;
        this.dumperOptions = dumperOptions;
        yaml = new Yaml(new EnumConfigurationConstructor(), new EnumConfigurationRepresenter(), dumperOptions);

    }

    public void load() throws IOException {

        LinkedHashMap<String, Object> input = null;
        try {
            input = yaml.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (YAMLException | ClassCastException e) {
            e.printStackTrace();
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }


    @SuppressWarnings(value = "unchecked")
    private void convertMapsToSections(LinkedHashMap<String, Object> input, ConfigurationSection section) {
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof LinkedHashMap) {
                convertMapsToSections((LinkedHashMap<String, Object>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    public void save() throws IOException {
        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            return;
        }
        if (parent.mkdirs() && !parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }

        String data = yaml.dump(getValues(false));

        if (data.equals("{}\n")) {
            data = "";
        }

        String[] yamlContents = data.split(System.getProperty("line.separator"));

        // This will hold the newly formatted line
        StringBuilder newContents = new StringBuilder();
        // This holds the current path the lines are at in the config
        String currentPath = "";
        // The depth of the path. (number of words separated by periods - 1)
        int depth = 0;
        // Loop through the config lines
        for (String line : yamlContents) {
            // This flags if the line is a node or unknown text.
            boolean node = false;
            // If the line is a node (and not something like a list value)
            if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
                // This is a node so flag it as one
                node = true;

                // Grab the index of the end of the node name
                int index;
                index = line.indexOf(": ");
                if (index < 0) {
                    index = line.length() - 1;
                }
                // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                if (currentPath.isEmpty()) {
                    currentPath = line.substring(0, index);
                } else {
                    // Calculate the whitespace preceding the node name
                    int whiteSpace = 0;
                    for (int n = 0; n < line.length(); n++) {
                        if (line.charAt(n) == ' ') {
                            whiteSpace++;
                        } else {
                            break;
                        }
                    }
                    if (whiteSpace / dumperOptions.getIndent() > depth) {
                        currentPath = currentPath + (dumperOptions.getSeparatorChar() + line.substring(whiteSpace, index));
                        depth++;
                    } else if (whiteSpace / dumperOptions.getIndent() < depth) {
                        int newDepth = whiteSpace / dumperOptions.getIndent();
                        for (int i = 0; i < depth - newDepth; i++) {
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(dumperOptions.getSeparatorChar())), "");
                        }
                        int lastIndex = currentPath.lastIndexOf(dumperOptions.getSeparatorChar());
                        if (lastIndex < 0) {
                            currentPath = "";
                        } else {
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(dumperOptions.getSeparatorChar())), "");
                            currentPath = currentPath + dumperOptions.getSeparatorChar();
                        }
                        currentPath = currentPath + line.substring(whiteSpace, index);
                        depth = newDepth;
                    } else {
                        int lastIndex = currentPath.lastIndexOf(dumperOptions.getSeparatorChar());
                        if (lastIndex < 0) {
                            currentPath = "";
                        } else {
                            currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(dumperOptions.getSeparatorChar())), "");
                            currentPath = currentPath + dumperOptions.getSeparatorChar();
                        }
                        currentPath = currentPath + line.substring(whiteSpace, index);
                    }
                }
            }
            if (node && comments.get(currentPath) != null) {
                // Add the comment to the beginning of the current line
                line = comments.get(currentPath) + line;
            }
            newContents.append(line).append(System.getProperty("line.separator"));

        }
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {

            out.write(newContents.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    public void setComments(String path, String... commentLines) {

        StringBuilder commentstring = new StringBuilder();
        StringBuilder leadingSpaces = new StringBuilder();
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == dumperOptions.getSeparatorChar()) {
                for (int j = 0; j < dumperOptions.getIndent(); j++) {
                    leadingSpaces.append(" ");
                }
            }
        }
        for (String line : commentLines) {
            commentstring.append(leadingSpaces).append(line).append(System.getProperty("line.separator"));
        }
        comments.put(path, commentstring.toString());
    }

    @Override
    public char getSeparatorCharacter() {
        return dumperOptions.getSeparatorChar();
    }
}