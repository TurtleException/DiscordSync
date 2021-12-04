import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class YamlStructureTest {
    public static void main(String[] args) throws Exception {
        File file = new File("H:"
                + File.separator + "Temp"
                + File.separator + "2wre435t6z"
                + File.separator + "yamlFile.yml"
        );

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.load(file);

        printYaml(yaml);


        System.out.println("\n\n");

        printSection("root", yaml.getConfigurationSection("modules.status"));


        System.out.println("\n\n");

        System.out.println(yaml.getConfigurationSection("modules.status").getInt("errorTolerance"));


        System.out.println("\n\n");

        System.out.println(yaml.saveToString());
    }

    private static void printYaml(YamlConfiguration yaml) {
        yaml.getKeys(true).forEach(s -> System.out.println(s + ":  " + yaml.get(s)));
    }

    private static void printSection(String prefix, ConfigurationSection section) {
        if (section.getKeys(true).isEmpty()) {
            System.out.println(prefix + " >>> *empty");
        }

        section.getKeys(true).forEach(s -> {
            if (section.isConfigurationSection(s)) {
                printSection(prefix + "." + s, section.getConfigurationSection(s));
            } else {
                System.out.println(prefix + " >>> " + s + ":  " + section.get(s));
            }
        });
    }
}
