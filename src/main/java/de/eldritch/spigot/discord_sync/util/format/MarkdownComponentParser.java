package de.eldritch.spigot.discord_sync.util.format;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

// TODO: finish
// TODO: Implement into MessageFormatter / replace MessageFormatter
public class MarkdownComponentParser {
    private static class Node {
        /**
         * The component held by this node.
         */
        final TextComponent component;
        /**
         * Reference to the node that precedes this node.
         */
        Node prev;
        /**
         * Reference to the node that follows this node.
         */
        Node next;
        /**
         * Indicated whether this node is already done and needs no further formatting.
         */
        boolean checked = false;

        Node(TextComponent component) {
            this.component = component;
        }
    }

    /**
     * Parses a single {@link TextComponent} containing raw Markdown-text to a {@link TextComponent} that has multiple
     * children with the correctly parsed format.
     * @param component The initial component
     * @return The parse TextComponent with formatted children or the initial component if no formatting is necessary.
     */
    public static @NotNull TextComponent parse(@NotNull TextComponent component) {
        Node head = new Node(component);
        Node current;

        // iterate through the chain at least once
        do {
            current = head;

            while (current != null) {
                // parse this text component (1 level of markdown)
                TextComponent[] parts = splitComponent(current.component);

                // check if any change has been made
                if (parts.length == 1 && parts[0].equals(current.component)) {
                    // splitComponent() returned only the initial component -> no more formatting required
                    current.checked = true;
                } else {
                    // splitComponent() returned at least 1 new component -> insert new nodes
                    Node internalPrev = current.prev;
                    Node internalNext = current.next;

                    for (TextComponent part : parts) {
                        Node newNode = new Node(part);

                        // insert node
                        newNode.prev = internalPrev;
                        newNode.next = internalNext;

                        if (internalPrev != null)
                            internalPrev.next = newNode;

                        if (internalNext != null)
                            internalNext.prev = newNode;

                        internalPrev = newNode;
                    }

                    current = internalNext;
                }
            }
        } while (!isDeepChecked(head)); // repeat until all nodes are checked (need no further formatting)

        return toTextComponent(head);
    }

    private static TextComponent[] splitComponent(@NotNull TextComponent initialComponent) {
        final String text      = initialComponent.getText();
        final char[] textChars = text.toCharArray();

        /* ----- DETERMINE MARKDOWN CHARACTER ----- */
        String mdChar  = null;
        int    mdIndex = 0;

        for (int i = 0; i < textChars.length; i++) {
            if (textChars[i] == '*') {
                // bold or italic
                mdChar  = isDoubleChar(textChars, i) ? "**" : "*";
                mdIndex = i;
                break;
            } else if (textChars[i] == '_') {
                // underscore or italic
                mdChar  = isDoubleChar(textChars, i) ? "__*" : "_";
                mdIndex = i;
                break;
            } else if (textChars[i] == '~') {
                if (isDoubleChar(textChars, i)) {
                    // strikethrough
                    mdChar  = "~~";
                    mdIndex = i;
                    break;
                }
            } else if (textChars[i] == '`') {
                if (!isDoubleChar(textChars, i)) {
                    // inline code
                    mdChar  = "`";
                    mdIndex = i;
                    break;
                } else {
                    // code block
                    if (isDoubleChar(textChars, i + 1)) {
                        mdChar  = "```";
                        mdIndex = i;
                        break;
                    }
                }
            } else if (textChars[i] == '|') {
                if (isDoubleChar(textChars, i)) {
                    // spoiler
                    mdChar  = "||";
                    mdIndex = i;
                    break;
                }
            }
        }


        // no markdown character found
        if (mdChar == null)
            return new TextComponent[]{initialComponent};


        /* ----- FIND CLOSING CHARACTER ----- */
        int firstIndex = mdIndex + mdChar.length() + 1;

        // first possibly closing index would be out of bounds
        if (text.length() <= firstIndex)
            return new TextComponent[]{initialComponent};

        int mdIndexEnd = text.indexOf(mdChar, firstIndex);

        // no end index found
        if (mdIndexEnd == -1)
            return new TextComponent[]{initialComponent};


        /* ----- PROVIDE PARTS ----- */
        TextComponent prefixComp = null;
        TextComponent formatComp = null;
        TextComponent suffixComp = null;

        if (mdIndex != 0) {
            prefixComp = new TextComponent(text.substring(0, mdIndex));
            prefixComp.copyFormatting(initialComponent);
        }

        if (mdIndexEnd + mdChar.length() < text.length()) {
            suffixComp = new TextComponent(text.substring(mdIndexEnd + mdChar.length(), text.length() - 1));
            suffixComp.copyFormatting(initialComponent);
        }

        formatComp = applyFormat(new TextComponent(text.substring(mdIndex, mdIndexEnd + mdChar.length())));

        if (prefixComp != null && suffixComp != null)
            return new TextComponent[]{prefixComp, formatComp, suffixComp};

        if (prefixComp != null)
            return new TextComponent[]{prefixComp, formatComp};

        if (suffixComp != null)
            return new TextComponent[]{formatComp, suffixComp};

        return new TextComponent[]{formatComp};
    }

    private static @NotNull TextComponent applyFormat(@NotNull TextComponent component) {
        applyFormatSpecific(component, "**", component1 -> component1.setBold(true));
        applyFormatSpecific(component, "*", component1 -> component1.setItalic(true));
        applyFormatSpecific(component, "__", component1 -> component1.setUnderlined(true));
        applyFormatSpecific(component, "_", component1 -> component1.setItalic(true));
        applyFormatSpecific(component, "~~", component1 -> component1.setStrikethrough(true));
        applyFormatSpecific(component, "```", component1 -> {
            component1.setText("\n" + component1.getText() + "\n");
            component1.setColor(ChatColor.DARK_GRAY);
        });
        applyFormatSpecific(component, "`", component1 -> component1.setColor(ChatColor.DARK_GRAY));
        applyFormatSpecific(component, "||", component1 -> {
            component1.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(component1.getText())
            ));
            component1.setObfuscated(true);
        });

        return component;
    }

    private static void applyFormatSpecific(@NotNull TextComponent component, @NotNull String mdChar, @NotNull Consumer<TextComponent> modifier) {
        if (component.getText().startsWith(mdChar) && component.getText().startsWith(mdChar)) {
            component.setText(component.getText().substring(mdChar.length(), component.getText().length() - (mdChar.length() + 1)));

            // modify component
            modifier.accept(component);
        }
    }

    /**
     * Checks whether a character (specified by index) in an array is immediately followed by the same character.
     * @param arr The char array.
     * @param i Index of the first character.
     * @return <code>true</code> if the next character is the same.
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds.
     */
    private static boolean isDoubleChar(char[] arr, int i) throws ArrayIndexOutOfBoundsException {
        // guard: char is last index of array
        if (i == arr.length - 1)
            return false;

        return arr[i] == arr[i + 1];
    }

    /**
     * Takes in a chain of {@link Node Nodes} and iterates through them to collect their components. These components
     * are then put into a new {@link TextComponent} that functions as parent.
     * @param head The head of the node chain.
     * @return TextComponent with formatted children.
     */
    private static @NotNull TextComponent toTextComponent(@NotNull Node head) {
        // determine array size
        int size = 0;
        Node current = head;
        while (current != null) {
            size++;
            current = current.next;
        }

        // allocate array
        TextComponent[] components = new TextComponent[size];

        // fill array
        current = head;
        for (int i = 0; i < size; i++) {
            components[i] = current.component;
            current = current.next;
        }

        return new TextComponent(components);
    }

    /**
     * Checks whether a chain of {@link Node Nodes} does not contain any unchecked nodes and therefore needs no further
     * formatting.
     * @param head The head of the node chain.
     * @return <code>true</code> if the node dos not contain unchecked nodes.
     */
    private static boolean isDeepChecked(Node head) {
        Node current = head;

        while (current != null) {
            // the first unchecked node will let the operation fail
            if (!current.checked)
                return false;

            current = current.next;
        }

        return true;
    }
}
