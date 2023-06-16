package eu.kennytv.mixin;

import eu.kennytv.Progress;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @ModifyArg(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"
    ), index = 0)
    private Component addMessage(final Component component, @Nullable final MessageSignature messageSignature,
                                 final int guiTick, @Nullable final GuiMessageTag guiMessageTag, final boolean refresh) {
        if (guiMessageTag != GuiMessageTag.system()) {
            // Only change system messages
            return component;
        }

        final Progress progress = new Progress();
        final Component cutComponent = replaceThing(component.copy(), progress);
        return progress.removed() > 1 ? cutComponent : component;
    }

    /**
     * Returns the filtered component, or null if no change was made.
     *
     * @param component component to filter
     * @param progress  current progress
     * @return filtered component, or null if no change was made
     */
    private @Nullable Component replaceThing(final MutableComponent component, final Progress progress) {
        // Weeeeeeeeeeeeeeee
        for (int i = 0; i < component.getSiblings().size(); i++) {
            final Component sibling = component.getSiblings().get(i);
            // More premium quality code
            final MutableComponent mutableSibling = sibling instanceof final MutableComponent mutableComponent ? mutableComponent : sibling.copy();
            final Component cutComponent = replaceThing(mutableSibling, progress);
            if (cutComponent == null) {
                continue;
            }

            component.getSiblings().set(i, cutComponent);
        }

        final ComponentContents cutContents = replaceOtherThing(progress, component.getContents());
        if (cutContents != null) {
            // This surely can't be the best way to do it
            final MutableComponent cutComponent = MutableComponent.create(cutContents);
            cutComponent.getSiblings().addAll(component.getSiblings());
            cutComponent.setStyle(component.getStyle());
            return cutComponent;
        }
        return null;
    }

    /**
     * Returns the filtered component contents, or null if no change was made.
     *
     * @param progress current progress
     * @param contents component contents to filter
     * @return filtered component contents, or null if no change was made
     */
    private ComponentContents replaceOtherThing(final Progress progress, final ComponentContents contents) {
        // Wooooooooooooo
        if (contents instanceof final LiteralContents literalContents) {
            // MMMMMMMM
            final String cutContents = literalContents.text().replace("[", "").replace("]", "");
            final int removed = literalContents.text().length() - cutContents.length();
            if (removed != 0) {
                progress.addRemoved(removed);
                return new LiteralContents(cutContents);
            }
        } else if (contents instanceof final TranslatableContents translatableContents) {
            // ????????????????????????????
            final Object[] args = translatableContents.getArgs();
            boolean changed = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof final MutableComponent mutableComponent) {
                    final Component cutComponent = replaceThing(mutableComponent, progress);
                    if (cutComponent == null) {
                        continue;
                    }

                    changed = true;
                    args[i] = cutComponent;
                } else if (args[i] instanceof final ComponentContents componentContents) {
                    final ComponentContents cutContents = replaceOtherThing(progress, componentContents);
                    if (cutContents == null) {
                        continue;
                    }

                    changed = true;
                    args[i] = cutContents;
                }
            }

            return changed ? contents : null;
        }
        return contents;
    }
}