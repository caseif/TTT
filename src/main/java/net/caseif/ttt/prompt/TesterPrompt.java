package net.caseif.ttt.prompt;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import java.util.List;
import java.util.Set;

public class TesterPrompt implements Prompt {

    private static Set<Prompt>

    @Override
    public String getPromptText(ConversationContext context) {
        return null;
    }

    @Override
    public boolean blocksForInput(ConversationContext context) {
        return false;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        return null;
    }
}
