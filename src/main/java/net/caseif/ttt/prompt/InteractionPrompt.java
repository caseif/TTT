package net.caseif.ttt.prompt;

import net.caseif.flint.util.physical.Location3D;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class InteractionPrompt implements Prompt {

    public static final String INTERACT_PREFIX = "$INTERACT_";

    private final String text;
    private final String dataField;
    private final Prompt nextPrompt;

    public InteractionPrompt(String text, String dataField, Prompt nextPrompt) {
        this.text = text;
        this.dataField = dataField;
        this.nextPrompt = nextPrompt;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return text;
    }

    @Override
    public boolean blocksForInput(ConversationContext context) {
        return true;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.startsWith(INTERACT_PREFIX)) {
            context.setSessionData(dataField, Location3D.deserialize(input.substring(INTERACT_PREFIX.length())));
            return nextPrompt;
        }
    }
}
