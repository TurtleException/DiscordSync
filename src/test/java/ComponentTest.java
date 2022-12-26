import de.turtle_exception.discordsync.util.StringUtil;
import de.turtle_exception.fancyformat.FancyFormatter;
import de.turtle_exception.fancyformat.FormatText;
import de.turtle_exception.fancyformat.formats.SpigotComponentsFormat;
import net.md_5.bungee.api.chat.TextComponent;

public class ComponentTest {
    public static void main(String[] args) {
        String format  = "3#[{\"text\":\"[\",\"color\":\"dark_gray\"},{\"text\":\"DiscordSync\",\"bold\":true,\"color\":\"gold\",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"DiscordSync\",\"color\":\"gold\"},{\"text\":\"\\nv{0}\",\"italic\":\"true\",\"color\":\"aqua\"}]}},{\"text\":\"] \",\"color\":\"dark_gray\"}]";
        String content = StringUtil.format(format, "123456");

        FancyFormatter formatter = new FancyFormatter();
        FormatText text = formatter.formNative(content);

        System.out.println(new TextComponent(text.parse(SpigotComponentsFormat.get())).toLegacyText());
    }
}
