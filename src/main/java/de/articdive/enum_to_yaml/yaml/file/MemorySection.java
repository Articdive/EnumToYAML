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

import de.articdive.enum_to_yaml.yaml.file.interfaces.Configuration;
import de.articdive.enum_to_yaml.yaml.file.interfaces.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


/**
 * A type of {@link ConfigurationSection} that is stored in memory.
 */
public class MemorySection implements ConfigurationSection {
    private final Map<String, Object> map = new LinkedHashMap<>();
    private final Configuration root;
    private final ConfigurationSection parent;
    private final String path;
    private final String fullPath;

    /**
     * Creates an empty MemorySection for use as a root {@link Configuration}
     * section.
     * <p>
     * Note that calling this without being yourself a {@link Configuration}
     * will throw an exception!
     *
     * @throws IllegalStateException Thrown if this is not a {@link
     *                               Configuration} root.
     */
    MemorySection() {
        if (!(this instanceof Configuration)) {
            throw new IllegalStateException("Cannot construct a root MemorySection when not a Configuration");
        }
        this.path = "";
        this.fullPath = "";
        this.parent = null;
        this.root = (Configuration) this;
    }

    /**
     * Creates an empty MemorySection with the specified parent and path.
     *
     * @param parent Parent section that contains this own section.
     * @param path   Path that you may access this section from via the root
     *               {@link Configuration}.
     * @throws IllegalArgumentException Thrown is parent or path is null, or
     *                                  if parent contains no root Configuration.
     */
    private MemorySection(ConfigurationSection parent, String path) {
        if (parent == null) {
            throw new NullPointerException("Parent cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        this.path = path;
        this.parent = parent;
        this.root = parent.getRoot();

        if (root == null) {
            throw new NullPointerException("Path cannot be orphaned");
        }

        this.fullPath = createPath(parent, path);
    }

    /**
     * Creates a full path to the given {@link ConfigurationSection} from its
     * root {@link Configuration}.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section Section to create a path for.
     * @param key     Name of the specified section.
     * @return Full path of the section from its root.
     */
    private static String createPath(ConfigurationSection section, String key) {
        if (section == null) {
            throw new NullPointerException("Cannot create path without a section");
        } else {
            return createPath(section, key, section.getRoot());
        }
    }

    /**
     * Creates a relative path to the given {@link ConfigurationSection} from
     * the given relative section.
     * <p>
     * You may use this method for any given {@link ConfigurationSection}, not
     * only {@link MemorySection}.
     *
     * @param section    Section to create a path for.
     * @param key        Name of the specified section.
     * @param relativeTo Section to create the path relative to.
     * @return Full path of the section from its root.
     */
    private static String createPath(ConfigurationSection section, String key, ConfigurationSection relativeTo) {
        if (section == null) {
            throw new NullPointerException("Cannot create path without a section");
        }
        Configuration root = section.getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create path without a root");
        }
        char separator = section.getRoot().getSeparatorCharacter();

        StringBuilder builder = new StringBuilder();
        for (ConfigurationSection parent = section; (parent != null) && (parent != relativeTo); parent = parent.getParent()) {
            if (builder.length() > 0) {
                builder.insert(0, separator);
            }

            builder.insert(0, parent.getName());
        }

        if ((key != null) && (key.length() > 0)) {
            if (builder.length() > 0) {
                builder.append(separator);
            }

            builder.append(key);
        }

        return builder.toString();
    }

    public Set<String> getKeys(boolean deep) {
        Set<String> result = new LinkedHashSet<>();

        mapChildrenKeys(result, this, deep);

        return result;
    }

    public LinkedHashMap<String, Object> getValues(boolean deep) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();


        mapChildrenValues(result, this, deep);

        return result;
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public boolean isSet(String path) {
        return contains(path);
    }

    public String getCurrentPath() {
        return fullPath;
    }

    public String getName() {
        return path;
    }

    public Configuration getRoot() {
        return root;
    }

    public ConfigurationSection getParent() {
        return parent;
    }

    public void set(String path, Object value) {
        if (path == null) {
            throw new NullPointerException("Cannot set to an empty path");
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("Cannot set to an empty path");
        }

        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot use section without a root");
        }

        final char separator = getRoot().getSeparatorCharacter();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                if (value == null) {
                    // no need to create missing sub-sections if we want to remove the value:
                    return;
                }
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        } else {
            section.set(key, value);
        }
    }

    public Object get(String path) {
        if (path.length() == 0) {
            return this;
        }

        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot access section without a root");
        }

        final char separator = getRoot().getSeparatorCharacter();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            section = section.getConfigurationSection(path.substring(i2, i1));
            if (section == null) {
                return null;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            return map.get(key);
        }
        return section.get(key);
    }

    public ConfigurationSection createSection(String path) {
        if (path == null) {
            throw new NullPointerException("Cannot create section at empty path");
        }
        if (path.length() == 0) {
            throw new IllegalArgumentException("Cannot create section at empty path");
        }
        Configuration root = getRoot();
        if (root == null) {
            throw new IllegalStateException("Cannot create section without a root");
        }

        final char separator = getRoot().getSeparatorCharacter();
        // i1 is the leading (higher) index
        // i2 is the trailing (lower) index
        int i1 = -1, i2;
        ConfigurationSection section = this;
        while ((i1 = path.indexOf(separator, i2 = i1 + 1)) != -1) {
            String node = path.substring(i2, i1);
            ConfigurationSection subSection = section.getConfigurationSection(node);
            if (subSection == null) {
                section = section.createSection(node);
            } else {
                section = subSection;
            }
        }

        String key = path.substring(i2);
        if (section == this) {
            ConfigurationSection result = new MemorySection(this, key);
            map.put(key, result);
            return result;
        }
        return section.createSection(key);
    }

    @SuppressWarnings(value = "unchecked")
    public ConfigurationSection createSection(String path, LinkedHashMap<String, Object> map) {
        ConfigurationSection section = createSection(path);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof LinkedHashMap) {
                section.createSection(entry.getKey(), (LinkedHashMap<String, Object>) entry.getValue());
            } else {
                section.set(entry.getKey(), entry.getValue());
            }
        }

        return section;
    }

    public ConfigurationSection getConfigurationSection(String path) {
        Object val = get(path);
        if (val instanceof ConfigurationSection) {
            return (ConfigurationSection) val;
        } else {
            return null;
        }
    }

    public boolean isConfigurationSection(String path) {
        Object val = get(path);
        return val instanceof ConfigurationSection;
    }

    private void mapChildrenKeys(Set<String> output, ConfigurationSection section, boolean deep) {
        if (section instanceof MemorySection) {
            MemorySection sec = (MemorySection) section;

            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.add(createPath(section, entry.getKey(), this));

                if ((deep) && (entry.getValue() instanceof ConfigurationSection)) {
                    ConfigurationSection subsection = (ConfigurationSection) entry.getValue();
                    mapChildrenKeys(output, subsection, deep);
                }
            }
        } else {
            Set<String> keys = section.getKeys(deep);

            for (String key : keys) {
                output.add(createPath(section, key, this));
            }
        }
    }

    private void mapChildrenValues(Map<String, Object> output, ConfigurationSection section, boolean deep) {
        if (section instanceof MemorySection) {
            MemorySection sec = (MemorySection) section;

            for (Map.Entry<String, Object> entry : sec.map.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());

                if (entry.getValue() instanceof ConfigurationSection) {
                    if (deep) {
                        mapChildrenValues(output, (ConfigurationSection) entry.getValue(), deep);
                    }
                }
            }
        } else {
            LinkedHashMap<String, Object> sectionMap = section.getValues(deep);

            for (Map.Entry<String, Object> entry : sectionMap.entrySet()) {
                output.put(createPath(section, entry.getKey(), this), entry.getValue());
            }
        }
    }
}
