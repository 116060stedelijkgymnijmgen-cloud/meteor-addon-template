package com.example.plugin;

import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;

import java.util.Comparator;
import java.util.List;

/**
 * EnabledModulesHUD - A Meteor Client HUD element that lists all active modules.
 *
 * Installation:
 *  1. Place this file (and the MeteorAddonEntry below) in your addon's source tree.
 *  2. Register the HUD element in your MeteorAddon#onInitialize:
 *       Hud.get().register(EnabledModulesHUD.INFO);
 *  3. Build and drop the jar into .minecraft/mods/.
 *  4. In-game: open the HUD editor (default: Right Shift) and add "Enabled Modules".
 */
public class EnabledModulesHUD extends HudElement {

    // ── HUD registration info ──────────────────────────────────────────────────
    public static final HudElementInfo<EnabledModulesHUD> INFO =
            new HudElementInfo<>(
                    EnabledModulesHudAddon.CATEGORY,   // your addon's HUD category
                    "enabled-modules",
                    "Shows all currently enabled modules.",
                    EnabledModulesHUD::new
            );

    // ── Settings ───────────────────────────────────────────────────────────────
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Text scale.")
            .defaultValue(1.0)
            .min(0.25).max(3.0)
            .sliderMin(0.25).sliderMax(2.0)
            .build()
    );

    private final Setting<Boolean> sortAlpha = sgGeneral.add(new BoolSetting.Builder()
            .name("sort-alphabetically")
            .description("Sort module names A–Z.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> showCategory = sgGeneral.add(new BoolSetting.Builder()
            .name("show-category")
            .description("Append the module's category name after each entry.")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> titleColor = sgGeneral.add(new ColorSetting.Builder()
            .name("title-color")
            .description("Color of the header text.")
            .defaultValue(new SettingColor(255, 165, 0, 255))   // orange
            .build()
    );

    private final Setting<SettingColor> moduleColor = sgGeneral.add(new ColorSetting.Builder()
            .name("module-color")
            .description("Color of each module name.")
            .defaultValue(new SettingColor(255, 255, 255, 220))
            .build()
    );

    private final Setting<SettingColor> categoryColor = sgGeneral.add(new ColorSetting.Builder()
            .name("category-color")
            .description("Color of the category suffix (if enabled).")
            .defaultValue(new SettingColor(150, 150, 150, 200))
            .build()
    );

    private final Setting<SettingColor> bgColor = sgGeneral.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Background fill color.")
            .defaultValue(new SettingColor(0, 0, 0, 100))
            .build()
    );

    // ── Constants ──────────────────────────────────────────────────────────────
    private static final double PADDING   = 4;
    private static final double LINE_GAP  = 2;
    private static final String TITLE     = "Enabled Modules";

    // ── Constructor ────────────────────────────────────────────────────────────
    public EnabledModulesHUD() {
        super(INFO);
    }

    // ── Render ─────────────────────────────────────────────────────────────────
    @Override
    public void render(HudRenderer renderer) {
        double s       = scale.get();
        double lineH   = TextRenderer.get().getHeight(s) + LINE_GAP;

        // Collect active modules
        List<Module> active = Modules.get().getAll().stream()
                .filter(Module::isActive)
                .sorted(sortAlpha.get()
                        ? Comparator.comparing(m -> m.name)
                        : Comparator.comparing(m -> m.category.name()))
                .toList();

        // Measure widths
        double titleW = TextRenderer.get().getWidth(TITLE, s);
        double maxW   = titleW;

        if (showCategory.get()) {
            for (Module m : active) {
                double w = TextRenderer.get().getWidth(m.name + "  " + m.category.name(), s);
                if (w > maxW) maxW = w;
            }
        } else {
            for (Module m : active) {
                double w = TextRenderer.get().getWidth(m.name, s);
                if (w > maxW) maxW = w;
            }
        }

        double boxW = maxW + PADDING * 2;
        double boxH = PADDING + lineH                          // title row
                    + (active.isEmpty() ? lineH : active.size() * lineH)
                    + PADDING;

        setSize(boxW, boxH);

        double x = this.x;
        double y = this.y;

        // Background
        Renderer2D.COLOR.begin();
        Renderer2D.COLOR.quad(x, y, boxW, boxH, bgColor.get());
        Renderer2D.COLOR.render(null);

        // Title
        double cx  = x + PADDING;
        double cy  = y + PADDING;
        renderer.text(TITLE, cx, cy, titleColor.get(), s);
        cy += lineH;

        // Module list
        if (active.isEmpty()) {
            renderer.text("(none)", cx, cy, categoryColor.get(), s);
        } else {
            for (Module m : active) {
                if (showCategory.get()) {
                    renderer.text(m.name, cx, cy, moduleColor.get(), s);
                    double nameW = TextRenderer.get().getWidth(m.name + "  ", s);
                    renderer.text(m.category.name(), cx + nameW, cy, categoryColor.get(), s);
                } else {
                    renderer.text(m.name, cx, cy, moduleColor.get(), s);
                }
                cy += lineH;
            }
        }
    }
}
