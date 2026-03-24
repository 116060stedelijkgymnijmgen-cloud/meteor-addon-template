package com.example.plugin;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import net.minecraft.item.Items;

/**
 * Main addon entry-point.
 *
 * 1. Register this class in your fabric.mod.json under
 *    "meteor-client:addon" entrypoints, e.g.:
 *
 *    "entrypoints": {
 *      "meteor-client:addon": [
 *        "com.example.plugin.EnabledModulesHudAddon"
 *      ]
 *    }
 *
 * 2. Make sure fabric-language-java (FLJ) / quilt-loader is on the classpath.
 */
public class EnabledModulesHudAddon extends MeteorAddon {

    /** Shown inside Meteor's HUD editor as the group/category name. */
    public static final HudGroup CATEGORY = new HudGroup("Example Addon");

    @Override
    public void onInitialize() {
        // Register the HUD element so it appears in the HUD editor.
        Hud.get().register(EnabledModulesHUD.INFO);
    }

    @Override
    public String getPackage() {
        // Root package of this addon – used by Meteor for event bus discovery.
        return "com.example.plugin";
    }
}
