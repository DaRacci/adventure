package net.kyori.adventure.text.minimessage.transformation;

import net.kyori.adventure.text.Component;

public class TranslatableTransformation extends Transformation {

    // TODO translatable

    @Override
    public Component apply(Component component) {
        return null;
    }

    public static boolean isApplicable(String name) {
        return false;
    }
}
